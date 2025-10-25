package com.example.sudoku.data.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

// Make it Serializable to pass between Activities
public class PuzzleResponse implements Serializable {

    @SerializedName("id") // Puzzle ID
    private String id;

    @SerializedName("gameId") // Add this field
    private String gameId;

    @SerializedName("difficulty")
    private String difficulty;

    @SerializedName("board_string")
    private String boardString;

    @SerializedName("solution_string")
    private String solutionString;

    // --- Getters ---
    public String getId() {
        return id;
    }

    // Add getter for gameId
    public String getGameId() {
        return gameId;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getBoardString() {
        return boardString;
    }

    public String getSolutionString() {
        return solutionString;
    }

    // --- Setters (Optional, but good practice if needed) ---
    public void setId(String id) {
        this.id = id;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
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

