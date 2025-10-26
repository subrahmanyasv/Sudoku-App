// Relative Path: Sudoku-App/app/src/main/java/com/example/sudoku/data/model/AuthResponse.java
package com.example.sudoku.data.model;

import com.google.gson.annotations.SerializedName;

// This class matches the backend's AuthResponse schema
public class AuthResponse {
    private String status;
    private String message;
    private String token;

    // *** ADDED userId field ***
    @SerializedName("userId") // Ensure this matches the JSON key from the backend
    private String userId;

    // Getters
    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    // Setters (if needed)
    public void setStatus(String status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setToken(String token) {
        this.token = token;
    }
    // *** ADDED getter for userId ***
    public String getUserId() {
        return userId;
    }
}

