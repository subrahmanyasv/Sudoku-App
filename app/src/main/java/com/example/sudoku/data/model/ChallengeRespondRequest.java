package com.example.sudoku.data.model;

import com.google.gson.annotations.SerializedName;

public class ChallengeRespondRequest {

    @SerializedName("action")
    private String action; // "accept" or "reject"

    public ChallengeRespondRequest(String action) {
        this.action = action;
    }
}
