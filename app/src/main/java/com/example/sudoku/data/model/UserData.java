// Relative Path: Sudoku-App/app/src/main/java/com/example/sudoku/data/model/UserData.java
package com.example.sudoku.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * POJO for the "message" object inside UserResponse.
 * This class MUST match the backend's src/Schemas/user_schema.py -> UserData
 */
public class UserData {

    @SerializedName("id")
    private String id;

    @SerializedName("username")
    private String username;

    @SerializedName("email")
    private String email;

    @SerializedName("total_games_played")
    private int total_games_played;

    @SerializedName("total_score")
    private int total_score;

    // --- ADDED MISSING FIELDS ---
    @SerializedName("best_score_easy")
    private int best_score_easy;

    @SerializedName("best_score_medium")
    private int best_score_medium;

    @SerializedName("best_score_hard")
    private int best_score_hard;

    // --- GETTERS ---
    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public int getTotal_games_played() {
        return total_games_played;
    }

    public int getTotal_score() {
        return total_score;
    }

    // --- ADDED MISSING GETTERS ---
    public int getBest_score_easy() {
        return best_score_easy;
    }

    public int getBest_score_medium() {
        return best_score_medium;
    }

    public int getBest_score_hard() {
        return best_score_hard;
    }
}

