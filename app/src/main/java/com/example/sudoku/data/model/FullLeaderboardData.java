// Matches FullLeaderboardData in backend leaderboard_schema.py
package com.example.sudoku.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class FullLeaderboardData {

    @SerializedName("top_players")
    private Map<String, LeaderboardCategoryData> topPlayers; // Keys: "easy", "medium", "hard"

    @SerializedName("user_ranks")
    private Map<String, UserRankCategoryData> userRanks; // Keys: "easy", "medium", "hard"

    // Getters
    public Map<String, LeaderboardCategoryData> getTopPlayers() { return topPlayers; }
    public Map<String, UserRankCategoryData> getUserRanks() { return userRanks; }

    // Optional: Add setters if needed
}
