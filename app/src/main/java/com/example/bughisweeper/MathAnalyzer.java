package com.example.bughisweeper;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Core mathematical analysis engine for Bughisweeper
 * Implements probability theory, Bayesian inference, information theory, and statistical analysis
 */
public class MathAnalyzer {

    private final Context context;
    private final SharedPreferences mathPrefs;

    // Mathematical analysis data
    private double[][] probabilityGrid;
    private double[][] entropyGrid;
    private double[][] informationGrid;
    private int[][] safetyScores;
    private int[][] riskLevels;

    // Game state
    private BughisBoard board;
    private int totalBugs;
    private int rows;
    private int cols;

    // Statistics tracking
    private GameStatistics currentGameStats;
    private List<MoveAnalysis> moveHistory;

    // Mathematical constants
    private static final double LOG_2 = Math.log(2);
    private static final double EPSILON = 1e-10; // Small value to avoid log(0)

    public MathAnalyzer(Context context) {
        this.context = context;
        this.mathPrefs = context.getSharedPreferences("math_analysis", Context.MODE_PRIVATE);
        this.moveHistory = new ArrayList<>();
        this.currentGameStats = new GameStatistics();
    }

    /**
     * Initialize mathematical analysis for a new game
     */
    public void initializeGame(BughisBoard board) {
        this.board = board;
        this.rows = board.getRows();
        this.cols = board.getCols();
        this.totalBugs = board.getTotalBugs();

        // Initialize grids
        probabilityGrid = new double[rows][cols];
        entropyGrid = new double[rows][cols];
        informationGrid = new double[rows][cols];
        safetyScores = new int[rows][cols];
        riskLevels = new int[rows][cols];

        // Reset statistics
        currentGameStats = new GameStatistics();
        moveHistory.clear();

        // Perform initial analysis
        updateCompleteAnalysis();
    }

    /**
     * Update all mathematical analysis after a move
     */
    public void updateCompleteAnalysis() {
        calculateBaseProbabilities();
        applyBayesianInference();
        calculateInformationTheory();
        calculateSafetyScores();
        calculateRiskLevels();
        updateGameStatistics();
    }

    /**
     * Calculate base probabilities using basic probability theory
     */
    private void calculateBaseProbabilities() {
        int revealedCells = 0;
        int flaggedCells = 0;

        // Count revealed and flagged cells
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = board.getCell(r, c);
                if (cell.isRevealed()) {
                    revealedCells++;
                    probabilityGrid[r][c] = cell.hasBug() ? 1.0 : 0.0;
                } else if (cell.isFlagged()) {
                    flaggedCells++;
                    probabilityGrid[r][c] = 1.0; // Assume flagged cells have bugs
                }
            }
        }

        // Calculate base probability for unrevealed, unflagged cells
        int totalCells = rows * cols;
        int unrevealedCells = totalCells - revealedCells;
        int remainingBugs = totalBugs - flaggedCells;

        double baseProbability = unrevealedCells > 0 ? (double) remainingBugs / unrevealedCells : 0.0;

        // Set base probability for unrevealed, unflagged cells
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = board.getCell(r, c);
                if (!cell.isRevealed() && !cell.isFlagged()) {
                    probabilityGrid[r][c] = baseProbability;
                }
            }
        }
    }

    /**
     * Apply Bayesian inference using revealed cell constraints
     */
    private void applyBayesianInference() {
        boolean changed = true;
        int iterations = 0;
        final int MAX_ITERATIONS = 10;

        // Iteratively refine probabilities using constraint propagation
        while (changed && iterations < MAX_ITERATIONS) {
            changed = false;
            iterations++;

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    Cell cell = board.getCell(r, c);

                    if (cell.isRevealed() && !cell.hasBug()) {
                        double oldProb = updateCellProbabilityUsingConstraints(r, c);
                        if (Math.abs(oldProb) > EPSILON) {
                            changed = true;
                        }
                    }
                }
            }
        }
    }

    /**
     * Update probability for neighbors of a revealed cell using constraint satisfaction
     */
    private double updateCellProbabilityUsingConstraints(int row, int col) {
        Cell revealedCell = board.getCell(row, col);
        int requiredBugs = revealedCell.getAdjacentBugs();

        List<Cell> neighbors = board.getNeighbors(row, col);
        List<Cell> unrevealedNeighbors = new ArrayList<>();
        int currentFlags = 0;
        double totalProbability = 0;

        // Analyze neighbors
        for (Cell neighbor : neighbors) {
            if (neighbor.isFlagged()) {
                currentFlags++;
            } else if (!neighbor.isRevealed()) {
                unrevealedNeighbors.add(neighbor);
                totalProbability += probabilityGrid[neighbor.getRow()][neighbor.getCol()];
            }
        }

        int remainingBugs = requiredBugs - currentFlags;
        double maxChange = 0;

        if (unrevealedNeighbors.size() > 0 && remainingBugs >= 0) {
            if (remainingBugs == 0) {
                // All remaining neighbors are safe
                for (Cell neighbor : unrevealedNeighbors) {
                    double oldProb = probabilityGrid[neighbor.getRow()][neighbor.getCol()];
                    probabilityGrid[neighbor.getRow()][neighbor.getCol()] = 0.0;
                    maxChange = Math.max(maxChange, Math.abs(oldProb));
                }
            } else if (remainingBugs == unrevealedNeighbors.size()) {
                // All remaining neighbors are mines
                for (Cell neighbor : unrevealedNeighbors) {
                    double oldProb = probabilityGrid[neighbor.getRow()][neighbor.getCol()];
                    probabilityGrid[neighbor.getRow()][neighbor.getCol()] = 1.0;
                    maxChange = Math.max(maxChange, Math.abs(1.0 - oldProb));
                }
            } else {
                // Apply proportional probability adjustment
                double targetProbability = (double) remainingBugs / unrevealedNeighbors.size();
                double adjustment = (targetProbability * unrevealedNeighbors.size() - totalProbability) / unrevealedNeighbors.size();

                for (Cell neighbor : unrevealedNeighbors) {
                    double oldProb = probabilityGrid[neighbor.getRow()][neighbor.getCol()];
                    double newProb = Math.max(0.0, Math.min(1.0, oldProb + adjustment));
                    probabilityGrid[neighbor.getRow()][neighbor.getCol()] = newProb;
                    maxChange = Math.max(maxChange, Math.abs(newProb - oldProb));
                }
            }
        }

        return maxChange;
    }

    /**
     * Calculate information theory metrics (entropy and information content)
     */
    private void calculateInformationTheory() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                double p = probabilityGrid[r][c];

                // Shannon entropy: H = -p*log2(p) - (1-p)*log2(1-p)
                if (p <= EPSILON) {
                    entropyGrid[r][c] = 0;
                } else if (p >= 1.0 - EPSILON) {
                    entropyGrid[r][c] = 0;
                } else {
                    entropyGrid[r][c] = -(p * log2(p) + (1 - p) * log2(1 - p));
                }

                // Information content: I = -log2(p)
                if (p <= EPSILON) {
                    informationGrid[r][c] = Double.MAX_VALUE;
                } else {
                    informationGrid[r][c] = -log2(p);
                }
            }
        }
    }

    /**
     * Calculate safety scores (0-100) for each cell
     */
    private void calculateSafetyScores() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = board.getCell(r, c);

                if (cell.isRevealed()) {
                    safetyScores[r][c] = cell.hasBug() ? 0 : 100;
                } else {
                    // Safety score = (1 - probability) * 100
                    safetyScores[r][c] = (int) ((1.0 - probabilityGrid[r][c]) * 100);
                }
            }
        }
    }

    /**
     * Calculate risk levels (1-5 scale) for each cell
     */
    private void calculateRiskLevels() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                double probability = probabilityGrid[r][c];

                if (probability < 0.1) {
                    riskLevels[r][c] = 1; // Very Low Risk
                } else if (probability < 0.3) {
                    riskLevels[r][c] = 2; // Low Risk
                } else if (probability < 0.5) {
                    riskLevels[r][c] = 3; // Medium Risk
                } else if (probability < 0.8) {
                    riskLevels[r][c] = 4; // High Risk
                } else {
                    riskLevels[r][c] = 5; // Very High Risk
                }
            }
        }
    }

    /**
     * Update game statistics
     */
    private void updateGameStatistics() {
        currentGameStats.totalCells = rows * cols;
        currentGameStats.totalBugs = totalBugs;
        currentGameStats.revealedCells = 0;
        currentGameStats.flaggedCells = 0;
        currentGameStats.correctFlags = 0;
        currentGameStats.averageProbability = 0;
        currentGameStats.totalEntropy = 0;

        int unrevealedCount = 0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = board.getCell(r, c);

                if (cell.isRevealed()) {
                    currentGameStats.revealedCells++;
                } else if (cell.isFlagged()) {
                    currentGameStats.flaggedCells++;
                    if (cell.hasBug()) {
                        currentGameStats.correctFlags++;
                    }
                } else {
                    currentGameStats.averageProbability += probabilityGrid[r][c];
                    unrevealedCount++;
                }

                currentGameStats.totalEntropy += entropyGrid[r][c];
            }
        }

        if (unrevealedCount > 0) {
            currentGameStats.averageProbability /= unrevealedCount;
        }

        // Calculate progress percentage
        int targetCells = currentGameStats.totalCells - currentGameStats.totalBugs;
        currentGameStats.progressPercentage = (currentGameStats.revealedCells * 100) / targetCells;

        // Calculate flag accuracy
        if (currentGameStats.flaggedCells > 0) {
            currentGameStats.flagAccuracy = (double) currentGameStats.correctFlags / currentGameStats.flaggedCells;
        }
    }

    /**
     * Analyze a specific move and record statistics
     */
    public MoveAnalysis analyzeMove(int row, int col, MoveType moveType) {
        MoveAnalysis analysis = new MoveAnalysis();
        analysis.row = row;
        analysis.col = col;
        analysis.moveType = moveType;
        analysis.preMoveProb = probabilityGrid[row][col];
        analysis.entropy = entropyGrid[row][col];
        analysis.informationGain = informationGrid[row][col];
        analysis.safetyScore = safetyScores[row][col];
        analysis.riskLevel = riskLevels[row][col];
        analysis.timestamp = System.currentTimeMillis();

        // Calculate expected value for the move
        if (moveType == MoveType.REVEAL) {
            // E(move) = P(safe) * benefit - P(mine) * cost
            double safeProbability = 1.0 - analysis.preMoveProb;
            analysis.expectedValue = safeProbability * 10 - analysis.preMoveProb * 100;
        }

        moveHistory.add(analysis);
        return analysis;
    }

    /**
     * Get optimal move suggestion using mathematical analysis
     */
    public OptimalMove getOptimalMove() {
        OptimalMove bestMove = new OptimalMove();
        bestMove.score = Double.NEGATIVE_INFINITY;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = board.getCell(r, c);

                if (!cell.isRevealed() && !cell.isFlagged()) {
                    double score = calculateMoveScore(r, c);

                    if (score > bestMove.score) {
                        bestMove.row = r;
                        bestMove.col = c;
                        bestMove.score = score;
                        bestMove.probability = probabilityGrid[r][c];
                        bestMove.entropy = entropyGrid[r][c];
                        bestMove.safetyScore = safetyScores[r][c];
                        bestMove.reasoning = generateMoveReasoning(r, c, score);
                    }
                }
            }
        }

        return bestMove;
    }

    /**
     * Calculate comprehensive move score combining safety and information gain
     */
    private double calculateMoveScore(int row, int col) {
        double probability = probabilityGrid[row][col];
        double entropy = entropyGrid[row][col];
        double safety = 1.0 - probability;

        // Weighted score: 70% safety, 30% information gain
        return safety * 0.7 + entropy * 0.3;
    }

    /**
     * Generate human-readable reasoning for move suggestion
     */
    private String generateMoveReasoning(int row, int col, double score) {
        double probability = probabilityGrid[row][col];
        double entropy = entropyGrid[row][col];
        int safetyScore = safetyScores[row][col];

        StringBuilder reasoning = new StringBuilder();

        if (probability < 0.1) {
            reasoning.append("Very safe choice (").append(String.format("%.1f%% risk", probability * 100)).append("). ");
        } else if (probability < 0.3) {
            reasoning.append("Relatively safe (").append(String.format("%.1f%% risk", probability * 100)).append("). ");
        } else {
            reasoning.append("Higher risk (").append(String.format("%.1f%% risk", probability * 100)).append("). ");
        }

        if (entropy > 0.8) {
            reasoning.append("High information gain expected.");
        } else if (entropy > 0.5) {
            reasoning.append("Moderate information gain.");
        } else {
            reasoning.append("Limited new information expected.");
        }

        return reasoning.toString();
    }

    /**
     * Calculate win probability using current game state
     */
    public double calculateWinProbability() {
        int remainingCells = 0;
        double totalRisk = 0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = board.getCell(r, c);
                if (!cell.isRevealed() && !cell.isFlagged()) {
                    remainingCells++;
                    totalRisk += probabilityGrid[r][c];
                }
            }
        }

        if (remainingCells == 0) {
            return 1.0; // Game won
        }

        double averageRisk = totalRisk / remainingCells;

        // Simplified win probability: (1 - average_risk)^remaining_moves
        return Math.pow(1 - averageRisk, remainingCells);
    }

    /**
     * Get mathematical insights for educational purposes
     */
    public MathematicalInsights getEducationalInsights() {
        MathematicalInsights insights = new MathematicalInsights();

        insights.conceptsApplied = new ArrayList<>();
        insights.conceptsApplied.add("Probability Theory: Base probability = remaining_mines / unrevealed_cells");
        insights.conceptsApplied.add("Bayesian Inference: P(mine|evidence) ∝ P(evidence|mine) × P(mine)");
        insights.conceptsApplied.add("Information Theory: Entropy = -Σ(p × log₂(p))");
        insights.conceptsApplied.add("Decision Theory: Expected Value = P(success) × benefit - P(failure) × cost");

        insights.currentProbabilityRange = String.format("%.1f%% - %.1f%%",
                getMinProbability() * 100, getMaxProbability() * 100);
        insights.totalEntropy = currentGameStats.totalEntropy;
        insights.averageRisk = currentGameStats.averageProbability;
        insights.flagAccuracy = currentGameStats.flagAccuracy * 100;

        return insights;
    }

    // Utility methods
    private double log2(double x) {
        return Math.log(x) / LOG_2;
    }

    private double getMinProbability() {
        double min = 1.0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!board.getCell(r, c).isRevealed() && !board.getCell(r, c).isFlagged()) {
                    min = Math.min(min, probabilityGrid[r][c]);
                }
            }
        }
        return min;
    }

    private double getMaxProbability() {
        double max = 0.0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!board.getCell(r, c).isRevealed() && !board.getCell(r, c).isFlagged()) {
                    max = Math.max(max, probabilityGrid[r][c]);
                }
            }
        }
        return max;
    }

    // Getter methods
    public double[][] getProbabilityGrid() { return probabilityGrid; }
    public double[][] getEntropyGrid() { return entropyGrid; }
    public int[][] getSafetyScores() { return safetyScores; }
    public int[][] getRiskLevels() { return riskLevels; }
    public GameStatistics getCurrentStats() { return currentGameStats; }
    public List<MoveAnalysis> getMoveHistory() { return moveHistory; }

    // Data classes
    public static class GameStatistics {
        public int totalCells;
        public int totalBugs;
        public int revealedCells;
        public int flaggedCells;
        public int correctFlags;
        public double averageProbability;
        public double totalEntropy;
        public int progressPercentage;
        public double flagAccuracy;
    }

    public static class MoveAnalysis {
        public int row, col;
        public MoveType moveType;
        public double preMoveProb;
        public double entropy;
        public double informationGain;
        public int safetyScore;
        public int riskLevel;
        public double expectedValue;
        public long timestamp;
    }

    public static class OptimalMove {
        public int row, col;
        public double score;
        public double probability;
        public double entropy;
        public int safetyScore;
        public String reasoning;
    }

    public static class MathematicalInsights {
        public List<String> conceptsApplied;
        public String currentProbabilityRange;
        public double totalEntropy;
        public double averageRisk;
        public double flagAccuracy;
    }

    public enum MoveType {
        REVEAL, FLAG, UNFLAG, SUPERPOWER
    }
}