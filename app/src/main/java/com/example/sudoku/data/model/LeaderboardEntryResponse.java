// Matches LeaderboardEntry in backend leaderboard_schema.py
package com.example.sudoku.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.UUID;

public class LeaderboardEntryResponse {

    @SerializedName("user_id")
    private UUID userId; // Keep as UUID if backend sends it, or String

    @SerializedName("username")
    private String username;

    @SerializedName("total_score")
    private int totalScore;

    @SerializedName("rank")
    private int rank;

    // Getters
    public UUID getUserId() { return userId; }
    public String getUsername() { return username; }
    public int getTotalScore() { return totalScore; }
    public int getRank() { return rank; }

    // Optional: Add setters if needed
}
