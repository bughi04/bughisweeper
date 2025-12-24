package com.example.bughisweeper;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

/**
 * Custom view for rendering the Bughisweeper game board.
 */
public class BoardView extends View {

    private static final String TAG = "BoardView";

    // Default dimensions and scaling
    private static final float DEFAULT_CELL_SIZE_DP = 32f;
    private static final float MIN_SCALE = 0.5f;
    private static final float MAX_SCALE = 3.0f;

    // Cell size in pixels
    private float cellSize;
    private float defaultCellSize;

    // Paint objects for drawing
    private Paint cellPaint;
    private Paint textPaint;
    private Paint linePaint;

    // Board and cell state
    private BughisBoard board;
    private float scale = 1.0f;

    // Drawing coordinates
    private float offsetX = 0;
    private float offsetY = 0;

    // Touch handling
    private ScaleGestureDetector scaleDetector;
    private GestureDetector gestureDetector;

    // Drawables for bugs and flags
    private Drawable bugDrawable;
    private Drawable flagDrawable;

    // Theme colors
    private int revealedCellColor;
    private int unrevealedCellColor;
    private int bugColor;
    private int flagColor;
    private int[] numberColors;

    // Callback for cell interactions
    private OnCellActionListener cellActionListener;

    /**
     * Interface for cell interaction callbacks
     */
    public interface OnCellActionListener {
        void onCellRevealed(int row, int col);
        void onCellFlagged(int row, int col);
    }

    public BoardView(Context context) {
        super(context);
        init(null);
    }

    public BoardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public BoardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    /**
     * Initialize the view
     */
    private void init(@Nullable AttributeSet attrs) {
        try {
            // Convert default cell size from dp to pixels
            float density = getResources().getDisplayMetrics().density;
            defaultCellSize = DEFAULT_CELL_SIZE_DP * density;
            cellSize = defaultCellSize;

            // Initialize paints
            cellPaint = new Paint();
            cellPaint.setAntiAlias(true);

            textPaint = new Paint();
            textPaint.setAntiAlias(true);
            textPaint.setTextAlign(Paint.Align.CENTER);

            linePaint = new Paint();
            linePaint.setAntiAlias(true);
            linePaint.setColor(0xFF888888);
            linePaint.setStrokeWidth(1 * density);

            // Initialize theme colors
            loadThemeColors();

            // Initialize gesture detectors
            scaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
            gestureDetector = new GestureDetector(getContext(), new GestureListener());

            // Make view focusable to receive touch events
            setFocusable(true);
            setFocusableInTouchMode(true);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing BoardView", e);
        }
    }

    /**
     * Load theme colors and drawables
     */
    private void loadThemeColors() {
        Context context = getContext();

        // Set default values first
        revealedCellColor = ContextCompat.getColor(context, R.color.classic_cell_revealed);
        unrevealedCellColor = ContextCompat.getColor(context, R.color.classic_cell_unrevealed);
        bugColor = ContextCompat.getColor(context, R.color.classic_bug);
        flagColor = ContextCompat.getColor(context, R.color.classic_flag);

        try {
            // Get colors from theme attributes
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    new int[] {
                            R.attr.cellRevealedBackground,
                            R.attr.cellUnrevealedBackground,
                            R.attr.bugColor,
                            R.attr.flagColor
                    });

            if (a != null) {
                revealedCellColor = a.getColor(0, revealedCellColor);
                unrevealedCellColor = a.getColor(1, unrevealedCellColor);
                bugColor = a.getColor(2, bugColor);
                flagColor = a.getColor(3, flagColor);
                a.recycle();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading theme attributes", e);
        }

        // Number colors (1-8)
        numberColors = new int[8];
        try {
            numberColors[0] = ContextCompat.getColor(context, R.color.number_1);
            numberColors[1] = ContextCompat.getColor(context, R.color.number_2);
            numberColors[2] = ContextCompat.getColor(context, R.color.number_3);
            numberColors[3] = ContextCompat.getColor(context, R.color.number_4);
            numberColors[4] = ContextCompat.getColor(context, R.color.number_5);
            numberColors[5] = ContextCompat.getColor(context, R.color.number_6);
            numberColors[6] = ContextCompat.getColor(context, R.color.number_7);
            numberColors[7] = ContextCompat.getColor(context, R.color.number_8);
        } catch (Exception e) {
            Log.e(TAG, "Error loading number colors", e);
            // Fill with default colors if there's an error
            for (int i = 0; i < 8; i++) {
                numberColors[i] = 0xFF000000 + (0x333333 * i);
            }
        }

        // Load drawables - using try-catch for safety
        try {
            // Try to load classic drawables first as fallback
            bugDrawable = ContextCompat.getDrawable(context, R.drawable.bug_classic);
            flagDrawable = ContextCompat.getDrawable(context, R.drawable.flag_classic);

            // Try to get themed versions
            try {
                ThemeManager themeManager = ThemeManager.getInstance(context);
                Drawable themeBugDrawable = themeManager.getThemeDrawable("bug");
                Drawable themeFlagDrawable = themeManager.getThemeDrawable("flag");

                // Only use themed drawables if not null
                if (themeBugDrawable != null) {
                    bugDrawable = themeBugDrawable;
                }
                if (themeFlagDrawable != null) {
                    flagDrawable = themeFlagDrawable;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading themed drawables, using classics", e);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading classic drawables, creating fallbacks", e);
        }

        // Create fallback drawables if needed
        if (bugDrawable == null) {
            ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());
            shapeDrawable.getPaint().setColor(bugColor);
            bugDrawable = shapeDrawable;
        }

        if (flagDrawable == null) {
            ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());
            shapeDrawable.getPaint().setColor(flagColor);
            flagDrawable = shapeDrawable;
        }
    }

    /**
     * Set the game board
     * @param board BughisBoard instance
     */
    public void setBoard(BughisBoard board) {
        this.board = board;
        resetViewport();
        invalidate();
    }

    /**
     * Set cell action listener
     * @param listener OnCellActionListener instance
     */
    public void setCellActionListener(OnCellActionListener listener) {
        this.cellActionListener = listener;
    }

    /**
     * Reset viewport to show the entire board centered
     */
    public void resetViewport() {
        if (board == null) return;

        try {
            // Calculate the scale to fit the board
            float boardWidth = board.getCols() * cellSize;
            float boardHeight = board.getRows() * cellSize;

            // Guard against division by zero
            float scaleX = getWidth() > 0 ? getWidth() / boardWidth : 1.0f;
            float scaleY = getHeight() > 0 ? getHeight() / boardHeight : 1.0f;

            // Use the smaller scale to fit the entire board
            scale = Math.min(scaleX, scaleY) * 0.9f;
            scale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale));

            // Center the board
            offsetX = (getWidth() - (boardWidth * scale)) / 2;
            offsetY = (getHeight() - (boardHeight * scale)) / 2;

            invalidate();
        } catch (Exception e) {
            Log.e(TAG, "Error in resetViewport", e);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        resetViewport();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (board == null) return;

        try {
            // Calculate the visible portion of the board
            int startRow = Math.max(0, (int)(-offsetY / (cellSize * scale)));
            int startCol = Math.max(0, (int)(-offsetX / (cellSize * scale)));

            int endRow = Math.min(board.getRows(), (int)((-offsetY + getHeight()) / (cellSize * scale)) + 1);
            int endCol = Math.min(board.getCols(), (int)((-offsetX + getWidth()) / (cellSize * scale)) + 1);

            // Draw each visible cell
            for (int row = startRow; row < endRow; row++) {
                for (int col = startCol; col < endCol; col++) {
                    Cell cell = board.getCell(row, col);
                    if (cell != null) {
                        drawCell(canvas, cell);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onDraw", e);
        }
    }

    /**
     * Draw a single cell
     * @param canvas Canvas to draw on
     * @param cell Cell to draw
     */
    private void drawCell(Canvas canvas, Cell cell) {
        try {
            float x = offsetX + (cell.getCol() * cellSize * scale);
            float y = offsetY + (cell.getRow() * cellSize * scale);

            // Cell rectangle
            Rect cellRect = new Rect(
                    (int)x,
                    (int)y,
                    (int)(x + cellSize * scale),
                    (int)(y + cellSize * scale)
            );

            // Draw cell background
            if (cell.isRevealed()) {
                cellPaint.setColor(revealedCellColor);
            } else {
                cellPaint.setColor(unrevealedCellColor);
            }
            canvas.drawRect(cellRect, cellPaint);

            // Draw cell content
            if (cell.isRevealed()) {
                if (cell.hasBug()) {
                    // Draw bug
                    drawableBounds(bugDrawable, cellRect);
                    bugDrawable.draw(canvas);
                } else if (cell.getAdjacentBugs() > 0) {
                    // Draw number
                    int number = cell.getAdjacentBugs();
                    textPaint.setColor(numberColors[Math.min(number - 1, numberColors.length - 1)]);
                    textPaint.setTextSize(cellSize * scale * 0.6f);

                    float textX = x + (cellSize * scale / 2);
                    float textY = y + (cellSize * scale / 2) - ((textPaint.descent() + textPaint.ascent()) / 2);

                    canvas.drawText(String.valueOf(number), textX, textY, textPaint);
                }
            } else if (cell.isFlagged()) {
                // Draw flag
                drawableBounds(flagDrawable, cellRect);
                flagDrawable.draw(canvas);
            }

            // Draw cell border
            canvas.drawLine(x, y, x + cellSize * scale, y, linePaint);
            canvas.drawLine(x, y, x, y + cellSize * scale, linePaint);
            canvas.drawLine(x + cellSize * scale, y, x + cellSize * scale, y + cellSize * scale, linePaint);
            canvas.drawLine(x, y + cellSize * scale, x + cellSize * scale, y + cellSize * scale, linePaint);
        } catch (Exception e) {
            Log.e(TAG, "Error drawing cell", e);
        }
    }

    /**
     * Set drawable bounds to fit in cell
     * @param drawable Drawable to set bounds for
     * @param cellRect Cell rectangle
     */
    private void drawableBounds(Drawable drawable, Rect cellRect) {
        if (drawable != null) {
            int padding = (int)(cellSize * scale * 0.2f);
            drawable.setBounds(
                    cellRect.left + padding,
                    cellRect.top + padding,
                    cellRect.right - padding,
                    cellRect.bottom - padding
            );
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            boolean scaleHandled = scaleDetector.onTouchEvent(event);
            boolean gestureHandled = gestureDetector.onTouchEvent(event);

            return scaleHandled || gestureHandled || super.onTouchEvent(event);
        } catch (Exception e) {
            Log.e(TAG, "Error handling touch event", e);
            return false;
        }
    }

    /**
     * Scale gesture listener for pinch-to-zoom
     */
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            try {
                // Get scale factor
                float scaleFactor = detector.getScaleFactor();

                // Apply scale factor
                scale *= scaleFactor;
                scale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale));

                // Keep focus point stationary
                float focusX = detector.getFocusX();
                float focusY = detector.getFocusY();

                // Adjust offset to keep focus point stationary
                offsetX += (offsetX - focusX) * (scaleFactor - 1);
                offsetY += (offsetY - focusY) * (scaleFactor - 1);

                invalidate();
                return true;
            } catch (Exception e) {
                Log.e(TAG, "Error scaling", e);
                return false;
            }
        }
    }

    /**
     * Gesture listener for panning and tapping
     */
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            try {
                // Pan the view
                offsetX -= distanceX;
                offsetY -= distanceY;

                invalidate();
                return true;
            } catch (Exception ex) {
                Log.e(TAG, "Error scrolling", ex);
                return false;
            }
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            try {
                if (board == null || cellActionListener == null) return false;

                // Convert touch coordinates to board coordinates
                int col = (int)((e.getX() - offsetX) / (cellSize * scale));
                int row = (int)((e.getY() - offsetY) / (cellSize * scale));

                // Check if coordinates are valid
                if (row >= 0 && row < board.getRows() && col >= 0 && col < board.getCols()) {
                    cellActionListener.onCellRevealed(row, col);
                    return true;
                }

                return false;
            } catch (Exception ex) {
                Log.e(TAG, "Error handling tap", ex);
                return false;
            }
        }

        @Override
        public void onLongPress(MotionEvent e) {
            try {
                if (board == null || cellActionListener == null) return;

                // Convert touch coordinates to board coordinates
                int col = (int)((e.getX() - offsetX) / (cellSize * scale));
                int row = (int)((e.getY() - offsetY) / (cellSize * scale));

                // Check if coordinates are valid
                if (row >= 0 && row < board.getRows() && col >= 0 && col < board.getCols()) {
                    cellActionListener.onCellFlagged(row, col);
                }
            } catch (Exception ex) {
                Log.e(TAG, "Error handling long press", ex);
            }
        }
    }
}