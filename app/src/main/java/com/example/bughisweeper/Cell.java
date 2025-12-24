package com.example.bughisweeper;

/**
 * Represents a single cell in the Bughisweeper game board.
 * Enhanced with mathematical analysis properties while keeping all existing functionality.
 */
public class Cell {

    // Cell state (YOUR EXISTING CODE)
    private boolean hasBug;
    private boolean isRevealed;
    private boolean isFlagged;

    // Cell position (YOUR EXISTING CODE)
    private final int row;
    private final int col;

    // Adjacent bug count (YOUR EXISTING CODE)
    private int adjacentBugs;

    // NEW: Mathematical analysis properties
    private double probability;
    private double entropy;
    private int safetyScore;
    private int riskLevel;

    /**
     * Constructor for a new cell (YOUR EXISTING CODE + math initialization)
     * @param row Row position
     * @param col Column position
     */
    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.hasBug = false;
        this.isRevealed = false;
        this.isFlagged = false;
        this.adjacentBugs = 0;

        // NEW: Initialize mathematical properties
        this.probability = 0.0;
        this.entropy = 0.0;
        this.safetyScore = 50;
        this.riskLevel = 3;
    }

    // ALL YOUR EXISTING METHODS - KEEPING EXACTLY AS IS

    /**
     * Check if the cell has a bug
     * @return True if the cell has a bug, false otherwise
     */
    public boolean hasBug() {
        return hasBug;
    }

    /**
     * Set whether the cell has a bug
     * @param hasBug True to place a bug, false to remove
     */
    public void setHasBug(boolean hasBug) {
        this.hasBug = hasBug;
    }

    // NEW: Alternative setter for compatibility with math analyzer
    public void setBug(boolean hasBug) {
        this.hasBug = hasBug;
    }

    /**
     * Check if the cell is revealed
     * @return True if revealed, false otherwise
     */
    public boolean isRevealed() {
        return isRevealed;
    }

    /**
     * Reveal the cell
     * @return True if the cell was newly revealed, false if it was already revealed
     */
    public boolean reveal() {
        if (!isRevealed && !isFlagged) {
            isRevealed = true;
            return true;
        }
        return false;
    }

    // NEW: Alternative setter for compatibility with math analyzer
    public void setRevealed(boolean revealed) {
        this.isRevealed = revealed;
    }

    /**
     * Check if the cell is flagged
     * @return True if flagged, false otherwise
     */
    public boolean isFlagged() {
        return isFlagged;
    }

    /**
     * Toggle the flag state of the cell
     * @return True if the cell is now flagged, false if the flag was removed
     */
    public boolean toggleFlag() {
        if (!isRevealed) {
            isFlagged = !isFlagged;
            return isFlagged;
        }
        return false;
    }

    // NEW: Alternative setter for compatibility with math analyzer
    public void setFlagged(boolean flagged) {
        this.isFlagged = flagged;
    }

    /**
     * Get the row position
     * @return Row index
     */
    public int getRow() {
        return row;
    }

    /**
     * Get the column position
     * @return Column index
     */
    public int getCol() {
        return col;
    }

    /**
     * Get the number of adjacent bugs
     * @return Count of bugs in neighboring cells
     */
    public int getAdjacentBugs() {
        return adjacentBugs;
    }

    /**
     * Set the number of adjacent bugs
     * @param count Count of bugs in neighboring cells
     */
    public void setAdjacentBugs(int count) {
        this.adjacentBugs = count;
    }

    /**
     * Increment the adjacent bugs counter
     */
    public void incrementAdjacentBugs() {
        this.adjacentBugs++;
    }

    /**
     * Reset the cell to initial state (ENHANCED with math properties)
     */
    public void reset() {
        this.hasBug = false;
        this.isRevealed = false;
        this.isFlagged = false;
        this.adjacentBugs = 0;

        // NEW: Reset mathematical properties
        this.probability = 0.0;
        this.entropy = 0.0;
        this.safetyScore = 50;
        this.riskLevel = 3;
    }

    // NEW: Mathematical analysis getters and setters
    public double getProbability() { return probability; }
    public void setProbability(double probability) { this.probability = probability; }

    public double getEntropy() { return entropy; }
    public void setEntropy(double entropy) { this.entropy = entropy; }

    public int getSafetyScore() { return safetyScore; }
    public void setSafetyScore(int safetyScore) { this.safetyScore = safetyScore; }

    public int getRiskLevel() { return riskLevel; }
    public void setRiskLevel(int riskLevel) { this.riskLevel = riskLevel; }
}