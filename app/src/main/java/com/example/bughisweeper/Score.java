package com.example.bughisweeper;

/**
 * Model class representing a player's score
 */
public class Score {
    private long id;
    private String playerName;
    private String difficulty;
    private int timeSeconds;
    private boolean gridCleared;
    private String date;

    public Score() {
        // Default constructor
    }

    public Score(long id, String playerName, String difficulty, int timeSeconds, boolean gridCleared, String date) {
        this.id = id;
        this.playerName = playerName;
        this.difficulty = difficulty;
        this.timeSeconds = timeSeconds;
        this.gridCleared = gridCleared;
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public int getTimeSeconds() {
        return timeSeconds;
    }

    public void setTimeSeconds(int timeSeconds) {
        this.timeSeconds = timeSeconds;
    }

    public boolean isGridCleared() {
        return gridCleared;
    }

    public void setGridCleared(boolean gridCleared) {
        this.gridCleared = gridCleared;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Format the time in mm:ss format
     * @return Formatted time string
     */
    public String getFormattedTime() {
        int minutes = timeSeconds / 60;
        int seconds = timeSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Get a display name for the difficulty
     * @return Formatted difficulty name
     */
    public String getFormattedDifficulty() {
        switch (difficulty) {
            case "easy":
                return "Easy";
            case "medium":
                return "Medium";
            case "hard":
                return "Hard";
            case "custom":
                return "Custom";
            default:
                return difficulty;
        }
    }
}