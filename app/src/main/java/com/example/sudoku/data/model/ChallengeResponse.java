package com.example.sudoku.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class ChallengeResponse {
    @SerializedName("id")
    private String id;

    @SerializedName("puzzle_id")
    private String puzzleId;

    @SerializedName("challenger_id")
    private String challengerId;

    @SerializedName("opponent_id")
    private String opponentId;

    @SerializedName("status")
    private String status;

    @SerializedName("challenger_duration")
    private int challengerDuration;

    @SerializedName("opponent_duration")
    private Integer opponentDuration;

    @SerializedName("winner_id")
    private String winnerId;

    @SerializedName("created_at")
    private Date createdAt;

    @SerializedName("expires_at")
    private Date expiresAt;

    @SerializedName("completed_at")
    private Date completedAt;

    @SerializedName("puzzle")
    private ChallengePuzzle puzzle;

    @SerializedName("challenger")
    private ChallengeUser challenger;

    @SerializedName("opponent")
    private ChallengeUser opponent;

    @SerializedName("winner")
    private ChallengeUser winner;

    // Getters
    public String getId() { return id; }
    public String getPuzzleId() { return puzzleId; }
    public String getChallengerId() { return challengerId; }
    public String getOpponentId() { return opponentId; }
    public String getStatus() { return status; }
    public int getChallengerDuration() { return challengerDuration; }
    public Integer getOpponentDuration() { return opponentDuration; }
    public String getWinnerId() { return winnerId; }
    public Date getCreatedAt() { return createdAt; }
    public Date getExpiresAt() { return expiresAt; }
    public Date getCompletedAt() { return completedAt; }
    public ChallengePuzzle getPuzzle() { return puzzle; }
    public ChallengeUser getChallenger() { return challenger; }
    public ChallengeUser getOpponent() { return opponent; }
    public ChallengeUser getWinner() { return winner; }
}
