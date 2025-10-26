// Matches UserRankEntry in backend leaderboard_schema.py
package com.example.sudoku.data.model;

import com.google.gson.annotations.SerializedName;

public class UserRankEntry {

    @SerializedName("total_score")
    private int totalScore;

    @SerializedName("rank")
    private int rank;

    // Getters
    public int getTotalScore() { return totalScore; }
    public int getRank() { return rank; }

    // Optional: Add setters if needed
}
