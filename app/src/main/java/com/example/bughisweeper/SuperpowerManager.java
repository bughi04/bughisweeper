package com.example.bughisweeper;

import android.content.Context;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Crash-resistant SuperpowerManager with minimal dependencies
 */
public class SuperpowerManager {

    private final Context context;
    private final BughisBoard board;
    private final BoardView boardView; // Can be null
    private final Handler handler;
    private final Random random;

    // Superpower cooldowns (in milliseconds)
    private static final long FREEZE_COOLDOWN = 60000; // 60 seconds
    private static final long XRAY_COOLDOWN = 45000;   // 45 seconds
    private static final long SONAR_COOLDOWN = 30000;  // 30 seconds
    private static final long LIGHTNING_COOLDOWN = 90000; // 90 seconds
    private static final long SHIELD_COOLDOWN = 0; // One per game
    private static final long SMART_SWEEP_COOLDOWN = 20000; // 20 seconds

    // Superpower durations
    private static final long FREEZE_DURATION = 10000; // 10 seconds
    private static final long XRAY_DURATION = 5000;    // 5 seconds

    private long lastFreezeUse = 0;
    private long lastXrayUse = 0;
    private long lastSonarUse = 0;
    private long lastLightningUse = 0;
    private long lastSmartSweepUse = 0;
    private boolean shieldUsed = false;
    private boolean shieldActive = false;

    private boolean freezeActive = false;
    private boolean xrayActive = false;
    private List<Cell> xrayRevealedCells = new ArrayList<>();

    // Mathematical analysis data - simplified
    private double[][] probabilityGrid;
    private int[][] safetyScores;

    private OnSuperpowerListener listener;

    public interface OnSuperpowerListener {
        void onSuperpowerActivated(SuperpowerType type);
        void onSuperpowerDeactivated(SuperpowerType type);
        void onCooldownUpdate(SuperpowerType type, long remainingMs);
        void onMathematicalAnalysisUpdate(double[][] probabilities);
    }

    public enum SuperpowerType {
        FREEZE("🧊 Freeze Time", "Pause timer for 10 seconds"),
        XRAY("🔍 X-Ray Vision", "Reveal 3 adjacent cells for 5 seconds"),
        SONAR("🌊 Sonar Pulse", "Show mine count in 5x5 area"),
        LIGHTNING("⚡ Lightning Strike", "Auto-reveal safest cell"),
        SHIELD("🛡️ Shield Mode", "Survive one mine hit"),
        SMART_SWEEP("🎯 Smart Sweep", "Auto-flag obvious mines");

        private final String displayName;
        private final String description;

        SuperpowerType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }

    public SuperpowerManager(Context context, BughisBoard board, BoardView boardView) {
        this.context = context;
        this.board = board;
        this.boardView = boardView; // Can be null
        this.handler = new Handler(Looper.getMainLooper());
        this.random = new Random();

        initializeMathematicalAnalysis();
    }

    public void setOnSuperpowerListener(OnSuperpowerListener listener) {
        this.listener = listener;
    }

    /**
     * Initialize mathematical probability analysis safely
     */
    private void initializeMathematicalAnalysis() {
        try {
            if (board == null) return;

            int rows = board.getRows();
            int cols = board.getCols();
            probabilityGrid = new double[rows][cols];
            safetyScores = new int[rows][cols];

            updateProbabilityAnalysis();
        } catch (Exception e) {
            // Mathematical analysis failed - continue without it
            probabilityGrid = null;
            safetyScores = null;
        }
    }

    /**
     * Update probability calculations safely
     */
    public void updateProbabilityAnalysis() {
        try {
            if (board == null || probabilityGrid == null) return;

            int rows = board.getRows();
            int cols = board.getCols();
            int totalBugs = board.getTotalBugs();
            int flaggedCount = board.getFlaggedCells();
            int revealedCount = 0;

            // Count revealed cells
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (board.getCell(r, c).isRevealed()) {
                        revealedCount++;
                    }
                }
            }

            int unrevealedCount = (rows * cols) - revealedCount;
            int remainingBugs = totalBugs - flaggedCount;

            // Base probability for unrevealed cells
            double baseProbability = unrevealedCount > 0 ? (double) remainingBugs / unrevealedCount : 0;

            // Calculate probabilities with simple logic
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    Cell cell = board.getCell(r, c);

                    if (cell.isRevealed()) {
                        probabilityGrid[r][c] = cell.hasBug() ? 1.0 : 0.0;
                        safetyScores[r][c] = cell.hasBug() ? 0 : 100;
                    } else if (cell.isFlagged()) {
                        probabilityGrid[r][c] = 1.0; // Assume flagged cells have bugs
                        safetyScores[r][c] = 0;
                    } else {
                        probabilityGrid[r][c] = baseProbability;
                        safetyScores[r][c] = (int) ((1.0 - baseProbability) * 100);
                    }
                }
            }

            if (listener != null) {
                listener.onMathematicalAnalysisUpdate(probabilityGrid);
            }
        } catch (Exception e) {
            // Probability analysis failed - not critical
        }
    }

    /**
     * Activate Freeze Time superpower
     */
    public boolean activateFreeze() {
        try {
            if (!canUseFreeze()) return false;

            freezeActive = true;
            lastFreezeUse = System.currentTimeMillis();

            // Deactivate after duration
            handler.postDelayed(() -> {
                freezeActive = false;
                if (listener != null) {
                    listener.onSuperpowerDeactivated(SuperpowerType.FREEZE);
                }
            }, FREEZE_DURATION);

            if (listener != null) {
                listener.onSuperpowerActivated(SuperpowerType.FREEZE);
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Activate X-Ray Vision superpower
     */
    public boolean activateXRay(int centerRow, int centerCol) {
        try {
            if (!canUseXRay()) return false;

            xrayActive = true;
            lastXrayUse = System.currentTimeMillis();
            xrayRevealedCells.clear();

            // Select up to 3 adjacent cells to reveal
            List<Cell> neighbors = board.getNeighbors(centerRow, centerCol);
            int revealed = 0;
            for (Cell neighbor : neighbors) {
                if (!neighbor.isRevealed() && !neighbor.isFlagged() && revealed < 3) {
                    xrayRevealedCells.add(neighbor);
                    revealed++;
                }
            }

            // Deactivate after duration
            handler.postDelayed(() -> {
                xrayActive = false;
                xrayRevealedCells.clear();
                if (boardView != null) {
                    boardView.invalidate();
                }
                if (listener != null) {
                    listener.onSuperpowerDeactivated(SuperpowerType.XRAY);
                }
            }, XRAY_DURATION);

            if (listener != null) {
                listener.onSuperpowerActivated(SuperpowerType.XRAY);
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Activate Sonar Pulse superpower
     */
    public boolean activateSonar(int centerRow, int centerCol) {
        try {
            if (!canUseSonar()) return false;

            lastSonarUse = System.currentTimeMillis();

            // Calculate mine count in 5x5 area
            int mineCount = calculateSonarReading(centerRow, centerCol);

            // Show result to user (implementation depends on UI)

            if (listener != null) {
                listener.onSuperpowerActivated(SuperpowerType.SONAR);
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Calculate sonar reading in area
     */
    private int calculateSonarReading(int centerRow, int centerCol) {
        try {
            int mineCount = 0;

            // Check 5x5 area around center
            for (int r = centerRow - 2; r <= centerRow + 2; r++) {
                for (int c = centerCol - 2; c <= centerCol + 2; c++) {
                    if (r >= 0 && r < board.getRows() && c >= 0 && c < board.getCols()) {
                        Cell cell = board.getCell(r, c);
                        if (cell.hasBug()) {
                            mineCount++;
                        }
                    }
                }
            }

            return mineCount;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Activate Lightning Strike superpower
     */
    public boolean activateLightning() {
        try {
            if (!canUseLightning()) return false;

            lastLightningUse = System.currentTimeMillis();

            // Find safest cell
            PointF safestCell = findSafestCell();

            if (safestCell != null) {
                // Auto-reveal the safest cell after short delay
                handler.postDelayed(() -> {
                    try {
                        board.revealCell((int) safestCell.x, (int) safestCell.y);
                        if (boardView != null) {
                            boardView.invalidate();
                        }
                        updateProbabilityAnalysis();
                    } catch (Exception e) {
                        // Cell reveal failed
                    }
                }, 500);
            }

            if (listener != null) {
                listener.onSuperpowerActivated(SuperpowerType.LIGHTNING);
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Find safest cell using simple analysis
     */
    private PointF findSafestCell() {
        try {
            if (probabilityGrid == null) return null;

            double minRisk = 1.0;
            PointF safestCell = null;

            for (int r = 0; r < board.getRows(); r++) {
                for (int c = 0; c < board.getCols(); c++) {
                    Cell cell = board.getCell(r, c);

                    if (!cell.isRevealed() && !cell.isFlagged()) {
                        double risk = probabilityGrid[r][c];
                        if (risk < minRisk) {
                            minRisk = risk;
                            safestCell = new PointF(r, c);
                        }
                    }
                }
            }

            return safestCell;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Activate Shield Mode superpower
     */
    public boolean activateShield() {
        try {
            if (!canUseShield()) return false;

            shieldActive = true;
            shieldUsed = true;

            if (listener != null) {
                listener.onSuperpowerActivated(SuperpowerType.SHIELD);
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Activate Smart Sweep superpower
     */
    public boolean activateSmartSweep() {
        try {
            if (!canUseSmartSweep()) return false;

            lastSmartSweepUse = System.currentTimeMillis();

            // Find cells where we can automatically flag mines
            List<PointF> autoFlags = findAutoFlagCells();

            // Apply flags
            for (PointF pos : autoFlags) {
                try {
                    board.toggleFlag((int) pos.x, (int) pos.y);
                } catch (Exception e) {
                    // Flag failed - continue with others
                }
            }

            if (boardView != null) {
                boardView.invalidate();
            }
            updateProbabilityAnalysis();

            if (listener != null) {
                listener.onSuperpowerActivated(SuperpowerType.SMART_SWEEP);
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Find cells that can be automatically flagged
     */
    private List<PointF> findAutoFlagCells() {
        List<PointF> autoFlags = new ArrayList<>();

        try {
            for (int r = 0; r < board.getRows(); r++) {
                for (int c = 0; c < board.getCols(); c++) {
                    Cell cell = board.getCell(r, c);

                    if (cell.isRevealed() && !cell.hasBug()) {
                        int requiredBugs = cell.getAdjacentBugs();
                        int currentFlags = 0;
                        List<Cell> unrevealedNeighbors = new ArrayList<>();

                        // Count current flags and unrevealed neighbors
                        List<Cell> neighbors = board.getNeighbors(r, c);
                        for (Cell neighbor : neighbors) {
                            if (neighbor.isFlagged()) {
                                currentFlags++;
                            } else if (!neighbor.isRevealed()) {
                                unrevealedNeighbors.add(neighbor);
                            }
                        }

                        // If unrevealed count equals remaining bugs needed, flag them all
                        int remainingBugs = requiredBugs - currentFlags;
                        if (remainingBugs > 0 && unrevealedNeighbors.size() == remainingBugs) {
                            for (Cell neighbor : unrevealedNeighbors) {
                                if (!neighbor.isFlagged()) {
                                    autoFlags.add(new PointF(neighbor.getRow(), neighbor.getCol()));
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Auto-flag search failed
        }

        return autoFlags;
    }

    /**
     * Handle mine hit when shield is active
     */
    public boolean handleMineHitWithShield() {
        if (shieldActive) {
            shieldActive = false;

            if (listener != null) {
                listener.onSuperpowerDeactivated(SuperpowerType.SHIELD);
            }

            return true; // Mine hit absorbed
        }
        return false; // No shield protection
    }

    // Cooldown check methods
    public boolean canUseFreeze() {
        return System.currentTimeMillis() - lastFreezeUse >= FREEZE_COOLDOWN;
    }

    public boolean canUseXRay() {
        return System.currentTimeMillis() - lastXrayUse >= XRAY_COOLDOWN;
    }

    public boolean canUseSonar() {
        return System.currentTimeMillis() - lastSonarUse >= SONAR_COOLDOWN;
    }

    public boolean canUseLightning() {
        return System.currentTimeMillis() - lastLightningUse >= LIGHTNING_COOLDOWN;
    }

    public boolean canUseShield() {
        return !shieldUsed;
    }

    public boolean canUseSmartSweep() {
        return System.currentTimeMillis() - lastSmartSweepUse >= SMART_SWEEP_COOLDOWN;
    }

    // Getters for current state
    public boolean isFreezeActive() { return freezeActive; }
    public boolean isXRayActive() { return xrayActive; }
    public boolean isShieldActive() { return shieldActive; }
    public List<Cell> getXRayRevealedCells() { return xrayRevealedCells; }
    public double[][] getProbabilityGrid() { return probabilityGrid; }

    // Remaining cooldown methods
    public long getRemainingFreezeCooldown() {
        return Math.max(0, FREEZE_COOLDOWN - (System.currentTimeMillis() - lastFreezeUse));
    }

    public long getRemainingXRayCooldown() {
        return Math.max(0, XRAY_COOLDOWN - (System.currentTimeMillis() - lastXrayUse));
    }

    public long getRemainingSonarCooldown() {
        return Math.max(0, SONAR_COOLDOWN - (System.currentTimeMillis() - lastSonarUse));
    }

    public long getRemainingLightningCooldown() {
        return Math.max(0, LIGHTNING_COOLDOWN - (System.currentTimeMillis() - lastLightningUse));
    }

    public long getRemainingSmartSweepCooldown() {
        return Math.max(0, SMART_SWEEP_COOLDOWN - (System.currentTimeMillis() - lastSmartSweepUse));
    }

    /**
     * Reset all superpowers for new game
     */
    public void reset() {
        try {
            lastFreezeUse = 0;
            lastXrayUse = 0;
            lastSonarUse = 0;
            lastLightningUse = 0;
            lastSmartSweepUse = 0;
            shieldUsed = false;
            shieldActive = false;
            freezeActive = false;
            xrayActive = false;
            xrayRevealedCells.clear();

            initializeMathematicalAnalysis();
        } catch (Exception e) {
            // Reset failed - not critical
        }
    }
}