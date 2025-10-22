package com.example.sudoku;

/**
 * Data model for a single game history entry.
 */
public class GameHistoryEntry {
    private String difficulty;
    private String time;
    private boolean won;
    private int score;

    public GameHistoryEntry(String difficulty, String time, boolean won, int score) {
        this.difficulty = difficulty;
        this.time = time;
        this.won = won;
        this.score = score;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getTime() {
        return time;
    }

    public boolean isWon() {
        return won;
    }

    public int getScore() {
        return score;
    }
}