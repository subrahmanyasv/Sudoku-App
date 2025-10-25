// Relative Path: Sudoku-App/app/src/main/java/com/example/sudoku/data/model/GameResponse.java
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

    @SerializedName("puzzle")
    private PuzzleResponse puzzle; // Nested puzzle details

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

    // No Setters needed if only used for receiving data
}
