// Relative Path: app/src/main/java/com/example/sudoku/data/model/ChallengePuzzle.java
package com.example.sudoku.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable; // Import Serializable

// Make Serializable if passed via Intents (though it's nested in ChallengeResponse which is)
public class ChallengePuzzle implements Serializable { // Implement Serializable
    @SerializedName("id")
    private String id;

    @SerializedName("difficulty")
    private String difficulty;

    @SerializedName("board_string")
    private String boardString;

    // *** ADDED solutionString field ***
    @SerializedName("solution_string")
    private String solutionString;

    // --- Getters ---
    public String getId() {
        return id;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getBoardString() {
        return boardString;
    }

    // *** ADDED getter for solutionString ***
    public String getSolutionString() {
        return solutionString;
    }

    // --- Optional Setters ---
    public void setId(String id) {
        this.id = id;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public void setBoardString(String boardString) {
        this.boardString = boardString;
    }

    public void setSolutionString(String solutionString) {
        this.solutionString = solutionString;
    }
}
