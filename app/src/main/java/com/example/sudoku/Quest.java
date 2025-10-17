package com.example.sudoku;

public class Quest {
    private String title;
    private String description;
    private String difficulty;
    private boolean isCompleted;

    public Quest(String title, String description, String difficulty, boolean isCompleted) {
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.isCompleted = isCompleted;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public boolean isCompleted() {
        return isCompleted;
    }
}
