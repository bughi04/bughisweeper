package com.example.bughisweeper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Represents the game board for Bughisweeper
 */
public class BughisBoard {
    private Cell[][] grid;
    private int rows;
    private int cols;
    private int totalBugs;
    private int flaggedCells;
    private int revealedCells;
    private boolean gameStarted;
    private Random random;

    public BughisBoard(int rows, int cols, int totalBugs) {
        this.rows = rows;
        this.cols = cols;
        this.totalBugs = totalBugs;
        this.flaggedCells = 0;
        this.revealedCells = 0;
        this.gameStarted = false;
        this.random = new Random();

        initializeGrid();
    }

    private void initializeGrid() {
        grid = new Cell[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = new Cell(r, c);
            }
        }
    }

    public void placeBugs(int firstClickRow, int firstClickCol) {
        if (gameStarted) return;

        int bugsPlaced = 0;
        while (bugsPlaced < totalBugs) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);

            // Don't place bug on first click or if already has bug
            if (!grid[r][c].hasBug() && !(r == firstClickRow && c == firstClickCol)) {
                grid[r][c].setBug(true);
                bugsPlaced++;
            }
        }

        calculateAdjacentBugs();
        gameStarted = true;
    }

    private void calculateAdjacentBugs() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!grid[r][c].hasBug()) {
                    int count = 0;
                    List<Cell> neighbors = getNeighbors(r, c);
                    for (Cell neighbor : neighbors) {
                        if (neighbor.hasBug()) {
                            count++;
                        }
                    }
                    grid[r][c].setAdjacentBugs(count);
                }
            }
        }
    }

    public List<Cell> getNeighbors(int row, int col) {
        List<Cell> neighbors = new ArrayList<>();

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue; // Skip the cell itself

                int newRow = row + dr;
                int newCol = col + dc;

                if (isValidPosition(newRow, newCol)) {
                    neighbors.add(grid[newRow][newCol]);
                }
            }
        }

        return neighbors;
    }

    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    public RevealResult revealCell(int row, int col) {
        if (!isValidPosition(row, col)) {
            return RevealResult.INVALID;
        }

        Cell cell = grid[row][col];

        if (cell.isRevealed() || cell.isFlagged()) {
            return RevealResult.ALREADY_PROCESSED;
        }

        // Place bugs on first click
        if (!gameStarted) {
            placeBugs(row, col);
        }

        cell.setRevealed(true);
        revealedCells++;

        if (cell.hasBug()) {
            return RevealResult.BUG_HIT;
        }

        // Auto-reveal adjacent cells if no adjacent bugs
        if (cell.getAdjacentBugs() == 0) {
            autoRevealAdjacent(row, col);
        }

        return RevealResult.SAFE;
    }

    private void autoRevealAdjacent(int row, int col) {
        List<Cell> neighbors = getNeighbors(row, col);
        for (Cell neighbor : neighbors) {
            if (!neighbor.isRevealed() && !neighbor.isFlagged()) {
                revealCell(neighbor.getRow(), neighbor.getCol());
            }
        }
    }

    public void toggleFlag(int row, int col) {
        if (!isValidPosition(row, col)) return;

        Cell cell = grid[row][col];
        if (cell.isRevealed()) return;

        if (cell.isFlagged()) {
            cell.setFlagged(false);
            flaggedCells--;
        } else {
            cell.setFlagged(true);
            flaggedCells++;
        }
    }

    public GameState getGameState() {
        // Check if all bugs are flagged and all safe cells are revealed
        int safeCellsToReveal = (rows * cols) - totalBugs;
        if (revealedCells >= safeCellsToReveal) {
            return GameState.WON;
        }

        // Check if any revealed cell has a bug
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = grid[r][c];
                if (cell.isRevealed() && cell.hasBug()) {
                    return GameState.LOST;
                }
            }
        }

        return GameState.PLAYING;
    }

    public void reset() {
        flaggedCells = 0;
        revealedCells = 0;
        gameStarted = false;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c].reset();
            }
        }
    }

    // Getters
    public Cell getCell(int row, int col) {
        if (isValidPosition(row, col)) {
            return grid[row][col];
        }
        return null;
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public int getTotalBugs() { return totalBugs; }
    public int getFlaggedCells() { return flaggedCells; }
    public int getFlaggedCount() { return flaggedCells; } // Alias for compatibility
    public int getRevealedCells() { return revealedCells; }
    public boolean isGameStarted() { return gameStarted; }

    // Enums
    public enum RevealResult {
        SAFE, BUG_HIT, ALREADY_PROCESSED, INVALID
    }

    public enum GameState {
        PLAYING, WON, LOST
    }
}