package com.example.bughisweeper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.bughisweeper.Score;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages score data and database operations.
 */
public class ScoreManager {

    private final DatabaseHelper dbHelper;

    /**
     * Constructor for ScoreManager
     * @param context Application context
     */
    public ScoreManager(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * Save a new score to the database
     * @param playerName Name of the player
     * @param difficulty Difficulty level
     * @param timeSeconds Time in seconds
     * @param gridCleared Whether the grid was fully cleared (win)
     * @return ID of the newly inserted score, or -1 if failed
     */
    public long saveScore(String playerName, String difficulty, int timeSeconds, boolean gridCleared) {
        SQLiteDatabase db = null;
        long scoreId = -1;

        try {
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();

            // First, check if player exists
            long playerId = findOrCreatePlayer(db, playerName);

            if (playerId != -1) {
                // Create score entry
                ContentValues scoreValues = new ContentValues();
                scoreValues.put(DatabaseHelper.COLUMN_PLAYER_ID, playerId);
                scoreValues.put(DatabaseHelper.COLUMN_DIFFICULTY, difficulty);
                scoreValues.put(DatabaseHelper.COLUMN_TIME_SECONDS, timeSeconds);
                scoreValues.put(DatabaseHelper.COLUMN_GRID_CLEARED, gridCleared ? 1 : 0);

                // Insert score
                scoreId = db.insert(DatabaseHelper.TABLE_SCORE, null, scoreValues);

                db.setTransactionSuccessful();
            }
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }

        return scoreId;
    }

    /**
     * Find a player by name or create a new player
     * @param db Database instance
     * @param playerName Name of the player
     * @return Player ID
     */
    private long findOrCreatePlayer(SQLiteDatabase db, String playerName) {
        // Look for existing player
        String[] columns = {DatabaseHelper.COLUMN_ID};
        String selection = DatabaseHelper.COLUMN_PLAYER_NAME + " = ?";
        String[] selectionArgs = {playerName};

        try (Cursor cursor = db.query(
                DatabaseHelper.TABLE_PLAYER,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null)) {

            // Return player ID if found
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
            }
        }

        // Create new player if not found
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PLAYER_NAME, playerName);

        return db.insert(DatabaseHelper.TABLE_PLAYER, null, values);
    }

    /**
     * Get high scores for a specific difficulty
     * @param difficulty Difficulty level (null for all difficulties)
     * @param limit Maximum number of scores to return (0 for no limit)
     * @return List of Score objects
     */
    public List<Score> getHighScores(String difficulty, int limit) {
        List<Score> scores = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();

            String query = "SELECT s." + DatabaseHelper.COLUMN_ID + ", " +
                    "p." + DatabaseHelper.COLUMN_PLAYER_NAME + ", " +
                    "s." + DatabaseHelper.COLUMN_DIFFICULTY + ", " +
                    "s." + DatabaseHelper.COLUMN_TIME_SECONDS + ", " +
                    "s." + DatabaseHelper.COLUMN_GRID_CLEARED + ", " +
                    "s." + DatabaseHelper.COLUMN_DATE +
                    " FROM " + DatabaseHelper.TABLE_SCORE + " s" +
                    " JOIN " + DatabaseHelper.TABLE_PLAYER + " p" +
                    " ON s." + DatabaseHelper.COLUMN_PLAYER_ID + " = p." + DatabaseHelper.COLUMN_ID;

            // Add difficulty filter if specified
            if (difficulty != null) {
                query += " WHERE s." + DatabaseHelper.COLUMN_DIFFICULTY + " = '" + difficulty + "'";
            }

            // Sort by best times (winning games first, then fastest times)
            query += " ORDER BY s." + DatabaseHelper.COLUMN_GRID_CLEARED + " DESC, " +
                    "s." + DatabaseHelper.COLUMN_TIME_SECONDS + " ASC";

            // Add limit if specified
            if (limit > 0) {
                query += " LIMIT " + limit;
            }

            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Score score = new Score();
                    score.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
                    score.setPlayerName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PLAYER_NAME)));
                    score.setDifficulty(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DIFFICULTY)));
                    score.setTimeSeconds(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIME_SECONDS)));
                    score.setGridCleared(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GRID_CLEARED)) == 1);
                    score.setDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE)));

                    scores.add(score);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return scores;
    }

    /**
     * Get the best score for a specific difficulty
     * @param difficulty Difficulty level
     * @return The best Score or null if no scores
     */
    public Score getBestScore(String difficulty) {
        List<Score> scores = getHighScores(difficulty, 1);
        return scores.isEmpty() ? null : scores.get(0);
    }

    /**
     * Delete all scores
     * @return Number of scores deleted
     */
    public int deleteAllScores() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_SCORE, null, null);
    }

    /**
     * Delete scores for a specific difficulty
     * @param difficulty Difficulty level
     * @return Number of scores deleted
     */
    public int deleteScores(String difficulty) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String whereClause = DatabaseHelper.COLUMN_DIFFICULTY + " = ?";
        String[] whereArgs = {difficulty};
        return db.delete(DatabaseHelper.TABLE_SCORE, whereClause, whereArgs);
    }

    /**
     * Get the count of scores
     * @param difficulty Difficulty level (null for all difficulties)
     * @return Number of scores
     */
    public int getScoreCount(String difficulty) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String countQuery = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_SCORE;

        if (difficulty != null) {
            countQuery += " WHERE " + DatabaseHelper.COLUMN_DIFFICULTY + " = '" + difficulty + "'";
        }

        Cursor cursor = db.rawQuery(countQuery, null);
        int count = 0;

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }

        return count;
    }

    /**
     * Check if a score is a high score
     * @param difficulty Difficulty level
     * @param timeSeconds Time in seconds
     * @param gridCleared Whether the grid was fully cleared (win)
     * @return True if it's a high score, false otherwise
     */
    public boolean isHighScore(String difficulty, int timeSeconds, boolean gridCleared) {
        // If the player didn't win, it's not a high score
        if (!gridCleared) {
            return false;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Count better scores
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_SCORE +
                " WHERE " + DatabaseHelper.COLUMN_DIFFICULTY + " = ?" +
                " AND " + DatabaseHelper.COLUMN_GRID_CLEARED + " = 1" +
                " AND " + DatabaseHelper.COLUMN_TIME_SECONDS + " < ?";

        String[] args = {difficulty, String.valueOf(timeSeconds)};

        try (Cursor cursor = db.rawQuery(query, args)) {
            if (cursor != null && cursor.moveToFirst()) {
                // If there are fewer than 10 better scores, it's a high score
                return cursor.getInt(0) < 10;
            }
        }

        return true;
    }
}