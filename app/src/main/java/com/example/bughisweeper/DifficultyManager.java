package com.example.bughisweeper;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages difficulty levels for the Bughisweeper game.
 */
public class DifficultyManager {

    // Difficulty constants
    public static final String DIFFICULTY_EASY = "easy";
    public static final String DIFFICULTY_MEDIUM = "medium";
    public static final String DIFFICULTY_HARD = "hard";
    public static final String DIFFICULTY_CUSTOM = "custom";

    // Default difficulty settings
    private static final int EASY_ROWS = 8;
    private static final int EASY_COLS = 8;
    private static final int EASY_BUGS = 10;

    private static final int MEDIUM_ROWS = 16;
    private static final int MEDIUM_COLS = 16;
    private static final int MEDIUM_BUGS = 40;

    private static final int HARD_ROWS = 24;
    private static final int HARD_COLS = 24;
    private static final int HARD_BUGS = 99;

    // Minimum and maximum values for custom difficulty
    private static final int MIN_ROWS = 4;
    private static final int MAX_ROWS = 50;
    private static final int MIN_COLS = 4;
    private static final int MAX_COLS = 50;
    private static final int MIN_BUGS = 1;

    // Shared preferences
    private static final String PREFS_NAME = "bughisweeper_prefs";
    private static final String PREF_DIFFICULTY = "difficulty";
    private static final String PREF_CUSTOM_ROWS = "custom_rows";
    private static final String PREF_CUSTOM_COLS = "custom_cols";
    private static final String PREF_CUSTOM_BUGS = "custom_bugs";

    // Default difficulty
    private static final String DEFAULT_DIFFICULTY = DIFFICULTY_EASY;

    private final Context context;
    private final SharedPreferences prefs;
    private String currentDifficulty;
    private int customRows;
    private int customCols;
    private int customBugs;

    // Singleton instance
    private static DifficultyManager instance;

    /**
     * Get the singleton instance of DifficultyManager
     * @param context Application context
     * @return DifficultyManager instance
     */
    public static synchronized DifficultyManager getInstance(Context context) {
        if (instance == null) {
            instance = new DifficultyManager(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Private constructor to prevent direct instantiation
     * @param context Application context
     */
    private DifficultyManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Load saved settings
        this.currentDifficulty = prefs.getString(PREF_DIFFICULTY, DEFAULT_DIFFICULTY);
        this.customRows = prefs.getInt(PREF_CUSTOM_ROWS, MEDIUM_ROWS);
        this.customCols = prefs.getInt(PREF_CUSTOM_COLS, MEDIUM_COLS);
        this.customBugs = prefs.getInt(PREF_CUSTOM_BUGS, Math.min(40, (customRows * customCols) / 4));
    }

    /**
     * Get the current difficulty level
     * @return Current difficulty name
     */
    public String getCurrentDifficulty() {
        return currentDifficulty;
    }

    /**
     * Set the current difficulty
     * @param difficulty Difficulty name
     */
    public void setDifficulty(String difficulty) {
        if (!isValidDifficulty(difficulty)) {
            difficulty = DEFAULT_DIFFICULTY;
        }

        currentDifficulty = difficulty;

        // Save to preferences
        prefs.edit().putString(PREF_DIFFICULTY, difficulty).apply();
    }

    /**
     * Set custom difficulty settings
     * @param rows Number of rows
     * @param cols Number of columns
     * @param bugs Number of bugs
     */
    public void setCustomSettings(int rows, int cols, int bugs) {
        // Clamp values to valid ranges
        rows = Math.max(MIN_ROWS, Math.min(MAX_ROWS, rows));
        cols = Math.max(MIN_COLS, Math.min(MAX_COLS, cols));

        // Maximum bugs is 1/3 of total cells
        int maxBugs = (rows * cols) / 3;
        bugs = Math.max(MIN_BUGS, Math.min(maxBugs, bugs));

        customRows = rows;
        customCols = cols;
        customBugs = bugs;

        // Save to preferences
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(PREF_CUSTOM_ROWS, rows);
        editor.putInt(PREF_CUSTOM_COLS, cols);
        editor.putInt(PREF_CUSTOM_BUGS, bugs);
        editor.apply();

        // Set difficulty to custom
        setDifficulty(DIFFICULTY_CUSTOM);
    }

    /**
     * Get the number of rows for the current difficulty
     * @return Row count
     */
    public int getRows() {
        switch (currentDifficulty) {
            case DIFFICULTY_EASY:
                return EASY_ROWS;
            case DIFFICULTY_MEDIUM:
                return MEDIUM_ROWS;
            case DIFFICULTY_HARD:
                return HARD_ROWS;
            case DIFFICULTY_CUSTOM:
                return customRows;
            default:
                return EASY_ROWS;
        }
    }

    /**
     * Get the number of columns for the current difficulty
     * @return Column count
     */
    public int getCols() {
        switch (currentDifficulty) {
            case DIFFICULTY_EASY:
                return EASY_COLS;
            case DIFFICULTY_MEDIUM:
                return MEDIUM_COLS;
            case DIFFICULTY_HARD:
                return HARD_COLS;
            case DIFFICULTY_CUSTOM:
                return customCols;
            default:
                return EASY_COLS;
        }
    }

    /**
     * Get the number of bugs for the current difficulty
     * @return Bug count
     */
    public int getBugs() {
        switch (currentDifficulty) {
            case DIFFICULTY_EASY:
                return EASY_BUGS;
            case DIFFICULTY_MEDIUM:
                return MEDIUM_BUGS;
            case DIFFICULTY_HARD:
                return HARD_BUGS;
            case DIFFICULTY_CUSTOM:
                return customBugs;
            default:
                return EASY_BUGS;
        }
    }

    /**
     * Get custom rows setting
     * @return Custom rows
     */
    public int getCustomRows() {
        return customRows;
    }

    /**
     * Get custom columns setting
     * @return Custom columns
     */
    public int getCustomCols() {
        return customCols;
    }

    /**
     * Get custom bugs setting
     * @return Custom bugs
     */
    public int getCustomBugs() {
        return customBugs;
    }

    /**
     * Check if the given difficulty is valid
     * @param difficulty Difficulty name
     * @return True if valid, false otherwise
     */
    public boolean isValidDifficulty(String difficulty) {
        return DIFFICULTY_EASY.equals(difficulty) ||
                DIFFICULTY_MEDIUM.equals(difficulty) ||
                DIFFICULTY_HARD.equals(difficulty) ||
                DIFFICULTY_CUSTOM.equals(difficulty);
    }

    /**
     * Get all available difficulty names
     * @return Array of difficulty names
     */
    public String[] getAvailableDifficulties() {
        return new String[] {
                DIFFICULTY_EASY,
                DIFFICULTY_MEDIUM,
                DIFFICULTY_HARD,
                DIFFICULTY_CUSTOM
        };
    }

    /**
     * Create a new BughisBoard with the current difficulty settings
     * @return A new game board
     */
    public BughisBoard createBoard() {
        return new BughisBoard(getRows(), getCols(), getBugs());
    }
}