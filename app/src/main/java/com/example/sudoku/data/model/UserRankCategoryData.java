// Matches UserRankCategoryData in backend leaderboard_schema.py
package com.example.sudoku.data.model;

import com.google.gson.annotations.SerializedName;

public class UserRankCategoryData {

    @SerializedName("daily")
    private UserRankEntry daily;

    @SerializedName("weekly")
    private UserRankEntry weekly;

    @SerializedName("all_time")
    private UserRankEntry allTime;

    // Getters
    public UserRankEntry getDaily() { return daily; }
    public UserRankEntry getWeekly() { return weekly; }
    public UserRankEntry getAllTime() { return allTime; }

    // Optional: Add setters if needed
}
