// Matches LeaderboardCategoryData in backend leaderboard_schema.py
package com.example.sudoku.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class LeaderboardCategoryData {

    @SerializedName("daily")
    private List<LeaderboardEntryResponse> daily;

    @SerializedName("weekly")
    private List<LeaderboardEntryResponse> weekly;

    @SerializedName("all_time")
    private List<LeaderboardEntryResponse> allTime;

    // Getters
    public List<LeaderboardEntryResponse> getDaily() { return daily; }
    public List<LeaderboardEntryResponse> getWeekly() { return weekly; }
    public List<LeaderboardEntryResponse> getAllTime() { return allTime; }

    // Optional: Add setters if needed
}
