// Relative Path: Sudoku-App/app/src/main/java/com/example/sudoku/data/model/UpdateResponse.java
package com.example.sudoku.data.model;

import com.google.gson.annotations.SerializedName;

// Corresponds to UpdateResponse schema in backend
public class UpdateResponse {

    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private String status; // "success" or "error"

    // Getters
    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }
}
