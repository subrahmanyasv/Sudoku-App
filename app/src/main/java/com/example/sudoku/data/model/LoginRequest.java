// Relative Path: Sudoku-App/app/src/main/java/com/example/sudoku/data/model/LoginRequest.java
package com.example.sudoku.data.model;

// This class matches the backend's UserLogin schema
public class LoginRequest {
    private String email;
    private String password;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getters and setters (or public fields) are needed for GSON
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

