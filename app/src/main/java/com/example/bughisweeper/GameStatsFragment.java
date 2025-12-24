package com.example.bughisweeper;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

/**
 * Fragment for displaying real-time game statistics and mathematical analysis
 */
public class GameStatsFragment extends Fragment {

    private TextView tvProbabilityAnalysis;
    private TextView tvSafetyScore;
    private TextView tvInformationGain;
    private TextView tvOptimalMove;
    private TextView tvWinProbability;
    private TextView tvExpectedMoves;
    private TextView tvEntropyMeasure;

    private GridLayout glProbabilityGrid;
    private ProgressBar pbOverallProgress;

    private BughisBoard board;
    private SuperpowerManager superpowerManager;
    private Handler updateHandler;
    private Runnable updateRunnable;

    private DecimalFormat probabilityFormat = new DecimalFormat("0.00%");
    private DecimalFormat scoreFormat = new DecimalFormat("0.0");

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_stats, container, false);

        initializeViews(view);
        setupUpdateLoop();

        return view;
    }

    private void initializeViews(View view) {
        tvProbabilityAnalysis = view.findViewById(R.id.tvProbabilityAnalysis);
        tvSafetyScore = view.findViewById(R.id.tvSafetyScore);
        tvInformationGain = view.findViewById(R.id.tvInformationGain);
        tvOptimalMove = view.findViewById(R.id.tvOptimalMove);
        tvWinProbability = view.findViewById(R.id.tvWinProbability);
        tvExpectedMoves = view.findViewById(R.id.tvExpectedMoves);
        tvEntropyMeasure = view.findViewById(R.id.tvEntropyMeasure);

        glProbabilityGrid = view.findViewById(R.id.glProbabilityGrid);
        pbOverallProgress = view.findViewById(R.id.pbOverallProgress);
    }

    private void setupUpdateLoop() {
        updateHandler = new Handler();
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateStatistics();
                updateHandler.postDelayed(this, 1000); // Update every second
            }
        };
    }

    public void setGameComponents(BughisBoard board, SuperpowerManager superpowerManager) {
        this.board = board;
        this.superpowerManager = superpowerManager;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (updateHandler != null && updateRunnable != null) {
            updateHandler.post(updateRunnable);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (updateHandler != null && updateRunnable != null) {
            updateHandler.removeCallbacks(updateRunnable);
        }
    }

    /**
     * Update all mathematical statistics and analysis
     */
    private void updateStatistics() {
        if (board == null || superpowerManager == null) return;

        // Update probability analysis
        updateProbabilityAnalysis();

        // Update safety scores
        updateSafetyScores();

        // Update information theory metrics
        updateInformationMetrics();

        // Update optimal move suggestion
        updateOptimalMove();

        // Update win probability
        updateWinProbability();

        // Update expected moves remaining
        updateExpectedMoves();

        // Update overall progress
        updateOverallProgress();
    }

    private void updateProbabilityAnalysis() {
        double[][] probabilities = superpowerManager.getProbabilityGrid();
        if (probabilities == null) return;

        double averageProbability = 0;
        double maxProbability = 0;
        double minProbability = 1;
        int cellCount = 0;

        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Cell cell = board.getCell(r, c);
                if (!cell.isRevealed() && !cell.isFlagged()) {
                    double prob = probabilities[r][c];
                    averageProbability += prob;
                    maxProbability = Math.max(maxProbability, prob);
                    minProbability = Math.min(minProbability, prob);
                    cellCount++;
                }
            }
        }

        if (cellCount > 0) {
            averageProbability /= cellCount;
        }

        String analysisText = String.format(Locale.getDefault(),
                "📊 Probability Analysis\n" +
                        "Average: %s\n" +
                        "Range: %s - %s\n" +
                        "Unrevealed: %d cells",
                probabilityFormat.format(averageProbability),
                probabilityFormat.format(minProbability),
                probabilityFormat.format(maxProbability),
                cellCount
        );

        tvProbabilityAnalysis.setText(analysisText);
    }

    private void updateSafetyScores() {
        double safestScore = 0;
        double averageScore = 0;
        int scoreCount = 0;

        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Cell cell = board.getCell(r, c);
                if (!cell.isRevealed() && !cell.isFlagged()) {
                    double prob = superpowerManager.getProbabilityGrid()[r][c];
                    double safety = (1 - prob) * 100;
                    safestScore = Math.max(safestScore, safety);
                    averageScore += safety;
                    scoreCount++;
                }
            }
        }

        if (scoreCount > 0) {
            averageScore /= scoreCount;
        }

        String safetyText = String.format(Locale.getDefault(),
                "🛡️ Safety Analysis\n" +
                        "Safest Cell: %s\n" +
                        "Average Safety: %s",
                scoreFormat.format(safestScore) + "%",
                scoreFormat.format(averageScore) + "%"
        );

        tvSafetyScore.setText(safetyText);
    }

    private void updateInformationMetrics() {
        double totalEntropy = 0;
        double averageInformation = 0;
        int cellCount = 0;

        double[][] probabilities = superpowerManager.getProbabilityGrid();

        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Cell cell = board.getCell(r, c);
                if (!cell.isRevealed() && !cell.isFlagged()) {
                    double p = probabilities[r][c];

                    // Calculate Shannon entropy
                    double entropy = 0;
                    if (p > 0 && p < 1) {
                        entropy = -(p * log2(p) + (1 - p) * log2(1 - p));
                    }

                    totalEntropy += entropy;

                    // Information content = -log₂(p)
                    if (p > 0) {
                        averageInformation += -log2(p);
                    }

                    cellCount++;
                }
            }
        }

        if (cellCount > 0) {
            totalEntropy /= cellCount;
            averageInformation /= cellCount;
        }

        String informationText = String.format(Locale.getDefault(),
                "📈 Information Theory\n" +
                        "Average Entropy: %s bits\n" +
                        "Information Content: %s bits",
                scoreFormat.format(totalEntropy),
                scoreFormat.format(averageInformation)
        );

        tvInformationGain.setText(informationText);
        tvEntropyMeasure.setText("Total Entropy: " + scoreFormat.format(totalEntropy * cellCount) + " bits");
    }

    private double log2(double x) {
        return Math.log(x) / Math.log(2);
    }

    private void updateOptimalMove() {
        // Find the cell with the best risk/reward ratio
        int bestRow = -1, bestCol = -1;
        double bestScore = -1;

        double[][] probabilities = superpowerManager.getProbabilityGrid();

        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Cell cell = board.getCell(r, c);
                if (!cell.isRevealed() && !cell.isFlagged()) {
                    double prob = probabilities[r][c];

                    // Calculate expected information gain
                    double entropy = 0;
                    if (prob > 0 && prob < 1) {
                        entropy = -(prob * log2(prob) + (1 - prob) * log2(1 - prob));
                    }

                    // Score = safety + information gain
                    double score = (1 - prob) * 0.7 + entropy * 0.3;

                    if (score > bestScore) {
                        bestScore = score;
                        bestRow = r;
                        bestCol = c;
                    }
                }
            }
        }

        String optimalText;
        if (bestRow >= 0 && bestCol >= 0) {
            optimalText = String.format(Locale.getDefault(),
                    "🎯 Optimal Move\n" +
                            "Position: (%d, %d)\n" +
                            "Score: %s\n" +
                            "Risk: %s",
                    bestRow + 1, bestCol + 1,
                    scoreFormat.format(bestScore),
                    probabilityFormat.format(probabilities[bestRow][bestCol])
            );
        } else {
            optimalText = "🎯 Optimal Move\nCalculating...";
        }

        tvOptimalMove.setText(optimalText);
    }

    private void updateWinProbability() {
        // Estimate win probability based on current state
        int remainingCells = 0;
        int safeCells = 0;
        double averageRisk = 0;

        double[][] probabilities = superpowerManager.getProbabilityGrid();

        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Cell cell = board.getCell(r, c);
                if (!cell.isRevealed() && !cell.isFlagged()) {
                    remainingCells++;
                    double prob = probabilities[r][c];
                    averageRisk += prob;

                    if (prob < 0.1) { // Consider < 10% risk as "safe"
                        safeCells++;
                    }
                }
            }
        }

        double winProbability = 0;
        if (remainingCells > 0) {
            averageRisk /= remainingCells;
            // Simplified win probability estimation
            winProbability = Math.pow(1 - averageRisk, remainingCells) * 100;
        }

        String winText = String.format(Locale.getDefault(),
                "🏆 Win Analysis\n" +
                        "Probability: %s\n" +
                        "Safe Cells: %d/%d\n" +
                        "Average Risk: %s",
                probabilityFormat.format(winProbability / 100),
                safeCells, remainingCells,
                probabilityFormat.format(averageRisk)
        );

        tvWinProbability.setText(winText);
    }

    private void updateExpectedMoves() {
        int remainingCells = 0;
        int revealedCells = 0;

        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Cell cell = board.getCell(r, c);
                if (cell.isRevealed()) {
                    revealedCells++;
                } else if (!cell.isFlagged()) {
                    remainingCells++;
                }
            }
        }

        int totalCells = board.getRows() * board.getCols();
        int expectedMoves = totalCells - board.getTotalBugs() - revealedCells;

        String movesText = String.format(Locale.getDefault(),
                "⏳ Progress Analysis\n" +
                        "Moves Remaining: ~%d\n" +
                        "Progress: %d%%\n" +
                        "Cells Left: %d",
                expectedMoves,
                (revealedCells * 100) / (totalCells - board.getTotalBugs()),
                remainingCells
        );

        tvExpectedMoves.setText(movesText);
    }

    private void updateOverallProgress() {
        int totalCells = board.getRows() * board.getCols();
        int targetCells = totalCells - board.getTotalBugs();
        int revealedCells = 0;

        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                if (board.getCell(r, c).isRevealed()) {
                    revealedCells++;
                }
            }
        }

        int progress = (revealedCells * 100) / targetCells;

        // Animate progress bar
        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(pbOverallProgress, "progress", progress);
        progressAnimator.setDuration(500);
        progressAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        progressAnimator.start();
    }
}