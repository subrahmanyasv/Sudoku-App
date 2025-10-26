// Sudoku-App/app/src/main/java/com/example/sudoku/data/model/GameResponse.java
package com.example.sudoku.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable; // Import Serializable

// Represents the response from /api/user/in_progress_game
// Implements Serializable to be passed via Intent extras
public class GameResponse implements Serializable {

    @SerializedName("id")
    private String id; // Game ID

    @SerializedName("difficulty")
    private String difficulty;

    @SerializedName("was_completed")
    private boolean wasCompleted;

    @SerializedName("duration_seconds")
    private int durationSeconds;

    @SerializedName("errors_made")
    private int errorsMade;

    @SerializedName("hints_used")
    private int hintsUsed;

    @SerializedName("final_score")
    private int finalScore;

    @SerializedName("completed_at")
    private String completedAt; // Will likely be null for in-progress games

    // *** ADDED: Field to hold the saved board state ***
    @SerializedName("current_state")
    private String currentState;

    @SerializedName("puzzle")
    private PuzzleResponse puzzle; // Nested puzzle details

    // --- Fields Specific to Challenges (Expected from modified backend) ---
    @SerializedName("is_challenge") // Flag to distinguish
    private boolean isChallenge;

    @SerializedName("opponent_username")
    private String opponentUsername; // Username of the opponent

    @SerializedName("challenger_username") // Username of the challenger (useful if user was opponent)
    private String challengerUsername;

    @SerializedName("winner_id") // ID of the winner (can be null if backend doesn't provide easily)
    private String winnerId;

    @SerializedName("opponent_duration") // Opponent's time (if available)
    private Integer opponentDuration;

    @SerializedName("challenger_duration") // Challenger's time (if available)
    private Integer challengerDuration;

    // *** ADDED IDs ***
    @SerializedName("challenger_id")
    private String challengerId;

    @SerializedName("opponent_id")
    private String opponentId;


    // Getters
    public String getId() { return id; }
    public String getDifficulty() { return difficulty; }
    public boolean wasCompleted() { return wasCompleted; }
    public int getDurationSeconds() { return durationSeconds; }
    public int getErrorsMade() { return errorsMade; }
    public int getHintsUsed() { return hintsUsed; }
    public int getFinalScore() { return finalScore; }
    public String getCompletedAt() { return completedAt; }
    public PuzzleResponse getPuzzle() { return puzzle; }

    // *** ADDED: Getter for current state ***
    public String getCurrentState() { return currentState; }

    // Getters for new challenge fields
    public boolean isChallenge() { return isChallenge; }
    public String getOpponentUsername() { return opponentUsername; }
    public String getChallengerUsername() { return challengerUsername; }
    public String getWinnerId() { return winnerId; }
    public Integer getOpponentDuration() { return opponentDuration; }
    public Integer getChallengerDuration() { return challengerDuration; }

    // *** ADDED Getters for IDs ***
    public String getChallengerId() { return challengerId; }
    public String getOpponentId() { return opponentId; }

    // No Setters needed if only used for receiving data
}
