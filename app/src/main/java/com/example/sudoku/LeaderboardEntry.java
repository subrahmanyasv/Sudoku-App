package com.example.sudoku;

public class LeaderboardEntry {
    private int rank;
    private String username;
    private int score;
    private String initial;

    public LeaderboardEntry(int rank, String username, int score, String initial) {
        this.rank = rank;
        this.username = username;
        this.score = score;
        this.initial = initial;
    }

    public int getRank() {
        return rank;
    }

    public String getUsername() {
        return username;
    }

    public int getScore() {
        return score;
    }

    // This is the missing method that caused the error
    public String getInitial() {
        return initial;
    }
}

