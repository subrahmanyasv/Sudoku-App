// Matches LeaderboardResponse in backend leaderboard_schema.py
package com.example.sudoku.data.model;

import com.google.gson.annotations.SerializedName;

public class LeaderboardResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private FullLeaderboardData data;

    // Getters
    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public FullLeaderboardData getData() { return data; }

    // Optional: Add setters if needed
}
