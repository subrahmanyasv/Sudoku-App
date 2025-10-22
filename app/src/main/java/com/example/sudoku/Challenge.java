package com.example.sudoku;

/**
 * Data model for a single challenge entry.
 */
public class Challenge {
    private String challengerName;
    private int scoreToBeat;
    private String initial;
    private boolean isIncoming; // true for Incoming, false for Outgoing

    public Challenge(String challengerName, int scoreToBeat, String initial, boolean isIncoming) {
        this.challengerName = challengerName;
        this.scoreToBeat = scoreToBeat;
        this.initial = initial;
        this.isIncoming = isIncoming;
    }

    public String getChallengerName() {
        return challengerName;
    }

    public int getScoreToBeat() {
        return scoreToBeat;
    }

    public String getInitial() {
        return initial;
    }

    public boolean isIncoming() {
        return isIncoming;
    }
}