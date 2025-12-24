package com.example.bughisweeper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLite database helper for Bughisweeper game.
 * Handles database creation and version management.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Database version and name
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "bughisweeper.db";

    // Table names
    public static final String TABLE_PLAYER = "player";
    public static final String TABLE_SCORE = "score";
    public static final String TABLE_SETTINGS = "settings";

    // Common column names
    public static final String COLUMN_ID = "id";

    // Player table columns
    public static final String COLUMN_PLAYER_NAME = "name";
    public static final String COLUMN_CREATED_AT = "created_at";

    // Score table columns
    public static final String COLUMN_PLAYER_ID = "player_id";
    public static final String COLUMN_DIFFICULTY = "difficulty";
    public static final String COLUMN_TIME_SECONDS = "time_seconds";
    public static final String COLUMN_GRID_CLEARED = "grid_cleared";
    public static final String COLUMN_DATE = "date";

    // Settings table columns
    public static final String COLUMN_THEME = "theme";
    public static final String COLUMN_SOUND_ENABLED = "sound_enabled";
    public static final String COLUMN_VIBRATION_ENABLED = "vibration_enabled";

    // Create table statements
    private static final String CREATE_TABLE_PLAYER = "CREATE TABLE " + TABLE_PLAYER + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_PLAYER_NAME + " TEXT NOT NULL,"
            + COLUMN_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
            + ")";

    private static final String CREATE_TABLE_SCORE = "CREATE TABLE " + TABLE_SCORE + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_PLAYER_ID + " INTEGER,"
            + COLUMN_DIFFICULTY + " TEXT NOT NULL,"
            + COLUMN_TIME_SECONDS + " INTEGER NOT NULL,"
            + COLUMN_GRID_CLEARED + " BOOLEAN NOT NULL,"
            + COLUMN_DATE + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
            + "FOREIGN KEY (" + COLUMN_PLAYER_ID + ") REFERENCES " + TABLE_PLAYER + "(" + COLUMN_ID + ")"
            + ")";

    private static final String CREATE_TABLE_SETTINGS = "CREATE TABLE " + TABLE_SETTINGS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_PLAYER_ID + " INTEGER,"
            + COLUMN_THEME + " TEXT NOT NULL,"
            + COLUMN_SOUND_ENABLED + " BOOLEAN DEFAULT 1,"
            + COLUMN_VIBRATION_ENABLED + " BOOLEAN DEFAULT 1,"
            + "FOREIGN KEY (" + COLUMN_PLAYER_ID + ") REFERENCES " + TABLE_PLAYER + "(" + COLUMN_ID + ")"
            + ")";

    // Singleton instance
    private static DatabaseHelper instance;

    /**
     * Get singleton instance of DatabaseHelper
     */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Private constructor to prevent direct instantiation
     */
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create all tables
        db.execSQL(CREATE_TABLE_PLAYER);
        db.execSQL(CREATE_TABLE_SCORE);
        db.execSQL(CREATE_TABLE_SETTINGS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if they exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCORE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYER);

        // Create tables again
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        // Enable foreign key constraints
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }
}