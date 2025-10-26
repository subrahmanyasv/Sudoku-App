package com.example.sudoku.data.model;

import com.google.gson.annotations.SerializedName;

public class ChallengeCreateRequest {

    @SerializedName("puzzle_id")
    private String puzzleId;

    @SerializedName("opponent_id")
    private String opponentId;

    @SerializedName("challenger_duration")
    private int challengerDuration;

    public ChallengeCreateRequest(String puzzleId, String opponentId, int challengerDuration) {
        this.puzzleId = puzzleId;
        this.opponentId = opponentId;
        this.challengerDuration = challengerDuration;
    }
}
