package com.example.bughisweeper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Custom view to visualize mathematical analysis data
 */
public class MathVisualizationView extends View {

    private Paint probabilityPaint;
    private Paint textPaint;
    private Paint gridPaint;
    private Paint overlayPaint;

    private MathAnalyzer mathAnalyzer;
    private boolean showProbabilities = true;
    private boolean showEntropy = false;
    private boolean showSafetyScores = false;

    private int cellSize = 40;
    private int gridRows = 8;
    private int gridCols = 8;

    public MathVisualizationView(Context context) {
        super(context);
        init();
    }

    public MathVisualizationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        probabilityPaint = new Paint();
        probabilityPaint.setAntiAlias(true);
        probabilityPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(12);
        textPaint.setTextAlign(Paint.Align.CENTER);

        gridPaint = new Paint();
        gridPaint.setAntiAlias(true);
        gridPaint.setColor(Color.GRAY);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1);

        overlayPaint = new Paint();
        overlayPaint.setAntiAlias(true);
        overlayPaint.setStyle(Paint.Style.STROKE);
        overlayPaint.setStrokeWidth(3);
    }

    public void setMathAnalyzer(MathAnalyzer analyzer) {
        this.mathAnalyzer = analyzer;
        if (analyzer != null) {
            double[][] probGrid = analyzer.getProbabilityGrid();
            if (probGrid != null) {
                gridRows = probGrid.length;
                gridCols = probGrid[0].length;
            }
        }
        invalidate();
    }

    public void setVisualizationMode(String mode) {
        showProbabilities = false;
        showEntropy = false;
        showSafetyScores = false;

        switch (mode) {
            case "probability":
                showProbabilities = true;
                break;
            case "entropy":
                showEntropy = true;
                break;
            case "safety":
                showSafetyScores = true;
                break;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mathAnalyzer == null) {
            drawPlaceholder(canvas);
            return;
        }

        calculateCellSize();

        if (showProbabilities) {
            drawProbabilityHeatmap(canvas);
        } else if (showEntropy) {
            drawEntropyVisualization(canvas);
        } else if (showSafetyScores) {
            drawSafetyVisualization(canvas);
        }

        drawGrid(canvas);
        drawLegend(canvas);
    }

    private void calculateCellSize() {
        int availableWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int availableHeight = getHeight() - getPaddingTop() - getPaddingBottom() - 100; // Reserve space for legend

        cellSize = Math.min(availableWidth / gridCols, availableHeight / gridRows);
        cellSize = Math.max(cellSize, 20); // Minimum size
    }

    private void drawProbabilityHeatmap(Canvas canvas) {
        double[][] probabilities = mathAnalyzer.getProbabilityGrid();
        if (probabilities == null) return;

        for (int r = 0; r < gridRows; r++) {
            for (int c = 0; c < gridCols; c++) {
                double probability = probabilities[r][c];

                // Color based on probability: green (safe) to red (dangerous)
                int color = getProbabilityColor(probability);
                probabilityPaint.setColor(color);

                RectF cellRect = getCellRect(r, c);
                canvas.drawRect(cellRect, probabilityPaint);

                // Draw probability percentage
                String probText = String.format("%.0f%%", probability * 100);
                float textX = cellRect.centerX();
                float textY = cellRect.centerY() + textPaint.getTextSize() / 3;
                canvas.drawText(probText, textX, textY, textPaint);
            }
        }
    }

    private void drawEntropyVisualization(Canvas canvas) {
        double[][] entropy = mathAnalyzer.getEntropyGrid();
        if (entropy == null) return;

        // Find max entropy for normalization
        double maxEntropy = 0;
        for (int r = 0; r < gridRows; r++) {
            for (int c = 0; c < gridCols; c++) {
                maxEntropy = Math.max(maxEntropy, entropy[r][c]);
            }
        }

        for (int r = 0; r < gridRows; r++) {
            for (int c = 0; c < gridCols; c++) {
                double entropyValue = entropy[r][c];
                double normalizedEntropy = maxEntropy > 0 ? entropyValue / maxEntropy : 0;

                // Color based on entropy: blue (low) to purple (high)
                int color = getEntropyColor(normalizedEntropy);
                probabilityPaint.setColor(color);

                RectF cellRect = getCellRect(r, c);
                canvas.drawRect(cellRect, probabilityPaint);

                // Draw entropy value
                String entropyText = String.format("%.2f", entropyValue);
                float textX = cellRect.centerX();
                float textY = cellRect.centerY() + textPaint.getTextSize() / 3;
                canvas.drawText(entropyText, textX, textY, textPaint);
            }
        }
    }

    private void drawSafetyVisualization(Canvas canvas) {
        int[][] safetyScores = mathAnalyzer.getSafetyScores();
        if (safetyScores == null) return;

        for (int r = 0; r < gridRows; r++) {
            for (int c = 0; c < gridCols; c++) {
                int safety = safetyScores[r][c];

                // Color based on safety: red (dangerous) to green (safe)
                int color = getSafetyColor(safety);
                probabilityPaint.setColor(color);

                RectF cellRect = getCellRect(r, c);
                canvas.drawRect(cellRect, probabilityPaint);

                // Draw safety score
                String safetyText = String.valueOf(safety);
                float textX = cellRect.centerX();
                float textY = cellRect.centerY() + textPaint.getTextSize() / 3;
                canvas.drawText(safetyText, textX, textY, textPaint);
            }
        }
    }

    private void drawGrid(Canvas canvas) {
        for (int r = 0; r <= gridRows; r++) {
            float y = getPaddingTop() + r * cellSize;
            canvas.drawLine(getPaddingLeft(), y,
                    getPaddingLeft() + gridCols * cellSize, y, gridPaint);
        }

        for (int c = 0; c <= gridCols; c++) {
            float x = getPaddingLeft() + c * cellSize;
            canvas.drawLine(x, getPaddingTop(),
                    x, getPaddingTop() + gridRows * cellSize, gridPaint);
        }
    }

    private void drawLegend(Canvas canvas) {
        float legendY = getPaddingTop() + gridRows * cellSize + 20;

        if (showProbabilities) {
            drawProbabilityLegend(canvas, legendY);
        } else if (showEntropy) {
            drawEntropyLegend(canvas, legendY);
        } else if (showSafetyScores) {
            drawSafetyLegend(canvas, legendY);
        }
    }

    private void drawProbabilityLegend(Canvas canvas, float y) {
        textPaint.setColor(Color.BLACK);
        canvas.drawText("Probability Legend:", getPaddingLeft(), y, textPaint);

        float legendItemWidth = 50;
        float startX = getPaddingLeft();

        // Draw color gradients with labels
        String[] labels = {"0%", "25%", "50%", "75%", "100%"};
        double[] values = {0.0, 0.25, 0.5, 0.75, 1.0};

        for (int i = 0; i < labels.length; i++) {
            probabilityPaint.setColor(getProbabilityColor(values[i]));
            RectF rect = new RectF(startX + i * legendItemWidth, y + 15,
                    startX + (i + 1) * legendItemWidth, y + 35);
            canvas.drawRect(rect, probabilityPaint);

            textPaint.setColor(Color.WHITE);
            canvas.drawText(labels[i], rect.centerX(), rect.centerY() + 5, textPaint);
        }

        textPaint.setColor(Color.BLACK);
        canvas.drawText("Green = Safe, Red = Dangerous", startX, y + 55, textPaint);
    }

    private void drawEntropyLegend(Canvas canvas, float y) {
        textPaint.setColor(Color.BLACK);
        canvas.drawText("Information Entropy Legend:", getPaddingLeft(), y, textPaint);

        float legendItemWidth = 50;
        float startX = getPaddingLeft();

        String[] labels = {"Low", "Med", "High"};
        double[] values = {0.0, 0.5, 1.0};

        for (int i = 0; i < labels.length; i++) {
            probabilityPaint.setColor(getEntropyColor(values[i]));
            RectF rect = new RectF(startX + i * legendItemWidth, y + 15,
                    startX + (i + 1) * legendItemWidth, y + 35);
            canvas.drawRect(rect, probabilityPaint);

            textPaint.setColor(Color.WHITE);
            canvas.drawText(labels[i], rect.centerX(), rect.centerY() + 5, textPaint);
        }

        textPaint.setColor(Color.BLACK);
        canvas.drawText("Purple = High Information Gain", startX, y + 55, textPaint);
    }

    private void drawSafetyLegend(Canvas canvas, float y) {
        textPaint.setColor(Color.BLACK);
        canvas.drawText("Safety Score Legend:", getPaddingLeft(), y, textPaint);

        float legendItemWidth = 40;
        float startX = getPaddingLeft();

        String[] labels = {"0", "25", "50", "75", "100"};
        int[] values = {0, 25, 50, 75, 100};

        for (int i = 0; i < labels.length; i++) {
            probabilityPaint.setColor(getSafetyColor(values[i]));
            RectF rect = new RectF(startX + i * legendItemWidth, y + 15,
                    startX + (i + 1) * legendItemWidth, y + 35);
            canvas.drawRect(rect, probabilityPaint);

            textPaint.setColor(Color.WHITE);
            canvas.drawText(labels[i], rect.centerX(), rect.centerY() + 5, textPaint);
        }

        textPaint.setColor(Color.BLACK);
        canvas.drawText("0 = Dangerous, 100 = Safe", startX, y + 55, textPaint);
    }

    private void drawPlaceholder(Canvas canvas) {
        textPaint.setColor(Color.GRAY);
        textPaint.setTextSize(20);
        String message = "Mathematical Analysis\nWill appear here during gameplay";
        float x = getWidth() / 2f;
        float y = getHeight() / 2f;

        String[] lines = message.split("\n");
        for (int i = 0; i < lines.length; i++) {
            canvas.drawText(lines[i], x, y + i * 30, textPaint);
        }
    }

    private RectF getCellRect(int row, int col) {
        float left = getPaddingLeft() + col * cellSize;
        float top = getPaddingTop() + row * cellSize;
        float right = left + cellSize;
        float bottom = top + cellSize;
        return new RectF(left, top, right, bottom);
    }

    private int getProbabilityColor(double probability) {
        // Interpolate between green (safe) and red (dangerous)
        if (probability < 0.2) {
            // Green to yellow
            float factor = (float) (probability / 0.2);
            return Color.rgb((int) (factor * 255), 255, 0);
        } else if (probability < 0.5) {
            // Yellow to orange
            float factor = (float) ((probability - 0.2) / 0.3);
            return Color.rgb(255, (int) (255 - factor * 100), 0);
        } else {
            // Orange to red
            float factor = (float) ((probability - 0.5) / 0.5);
            return Color.rgb(255, (int) (155 - factor * 155), 0);
        }
    }

    private int getEntropyColor(double normalizedEntropy) {
        // Interpolate from blue (low entropy) to purple (high entropy)
        int blue = 255;
        int red = (int) (normalizedEntropy * 255);
        int green = (int) (50 + normalizedEntropy * 100);
        return Color.rgb(red, green, blue);
    }

    private int getSafetyColor(int safety) {
        // Interpolate from red (0) to green (100)
        float factor = safety / 100f;
        int red = (int) (255 * (1 - factor));
        int green = (int) (255 * factor);
        return Color.rgb(red, green, 0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = gridCols * cellSize + getPaddingLeft() + getPaddingRight();
        int desiredHeight = gridRows * cellSize + getPaddingTop() + getPaddingBottom() + 100;

        int width = resolveSize(desiredWidth, widthMeasureSpec);
        int height = resolveSize(desiredHeight, heightMeasureSpec);

        setMeasuredDimension(width, height);
    }
}