// Sudoku-App/app/src/main/java/com/example/sudoku/data/model/GameUpdateRequest.java
package com.example.sudoku.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.UUID; // Import UUID

// Corresponds to GameBase schema in backend, BUT only fields needed for update
public class GameUpdateRequest {

    // Include the game ID to identify which game to update
    @SerializedName("id")
    private String gameId; // Assuming backend expects game UUID as string

    // Add difficulty to match backend GameBase schema expected in controller
    @SerializedName("difficulty")
    private String difficulty;

    @SerializedName("was_completed")
    private boolean wasCompleted;

    @SerializedName("duration_seconds")
    private int durationSeconds;

    @SerializedName("errors_made")
    private int errorsMade;

    @SerializedName("hints_used")
    private int hintsUsed; // Keep even if not implemented yet

    @SerializedName("final_score")
    private int finalScore;

    @SerializedName("completed_at")
    private String completedAt; // Send timestamp as ISO 8601 String e.g., "2025-10-24T18:30:00Z"

    // *** ADDED: Field to send the current board state ***
    @SerializedName("current_state")
    private String currentState;

    // --- Empty Constructor (Added) ---
    public GameUpdateRequest() {
        // Default constructor
    }


    // --- Setters (Added) ---
    public void setId(String id) {
        this.gameId = id;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public void setWasCompleted(boolean wasCompleted) {
        this.wasCompleted = wasCompleted;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public void setErrorsMade(int errorsMade) {
        this.errorsMade = errorsMade;
    }

    public void setHintsUsed(int hintsUsed) {
        this.hintsUsed = hintsUsed;
    }

    public void setFinalScore(int finalScore) {
        this.finalScore = finalScore;
    }

    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }

    // *** ADDED: Setter for current state ***
    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }

    // Constructor (Updated)
    public GameUpdateRequest(String gameId, String difficulty, boolean wasCompleted, int durationSeconds, int errorsMade, int hintsUsed, int finalScore, String completedAt, String currentState) {
        this.gameId = gameId;
        this.difficulty = difficulty;
        this.wasCompleted = wasCompleted;
        this.durationSeconds = durationSeconds;
        this.errorsMade = errorsMade;
        this.hintsUsed = hintsUsed;
        this.finalScore = finalScore;
        this.completedAt = completedAt;
        this.currentState = currentState;
    }

    // Getters (and potentially setters if needed)
    public String getGameId() { return gameId; }
    public String getDifficulty() { return difficulty; }
    public boolean wasCompleted() { return wasCompleted; }
    public int getDurationSeconds() { return durationSeconds; }
    public int getErrorsMade() { return errorsMade; }
    public int getHintsUsed() { return hintsUsed; }
    public int getFinalScore() { return finalScore; }
    public String getCompletedAt() { return completedAt; }
    // *** ADDED: Getter for current state ***
    public String getCurrentState() { return currentState; }

}
