package com.example.sudoku.data.model;

import com.google.gson.annotations.SerializedName;

public class ChallengeUser {
    @SerializedName("id")
    private String id;

    @SerializedName("username")
    private String username;

    // Getters
    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }
}
