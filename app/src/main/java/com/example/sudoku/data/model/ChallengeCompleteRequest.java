package com.example.sudoku.data.model;

import com.google.gson.annotations.SerializedName;

public class ChallengeCompleteRequest {

    @SerializedName("opponent_duration")
    private int opponentDuration;

    public ChallengeCompleteRequest(int opponentDuration) {
        this.opponentDuration = opponentDuration;
    }
}
