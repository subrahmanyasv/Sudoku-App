// Relative Path: Sudoku-App/app/src/main/java/com/example/sudoku/data/model/UserResponse.java
package com.example.sudoku.data.model;

// This POJO matches the UserResponse schema in user_schema.py
public class UserResponse {
    private String status;
    private UserData message; // The backend schema nests the UserData under "message"

    // Getters
    public String getStatus() {
        return status;
    }

    public UserData getMessage() {
        return message;
    }
}
