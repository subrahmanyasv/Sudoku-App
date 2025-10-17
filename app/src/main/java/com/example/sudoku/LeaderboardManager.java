package com.example.sudoku;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardManager {

    public static List<LeaderboardEntry> getDailyLeaderboard() {
        List<LeaderboardEntry> entries = new ArrayList<>();
        entries.add(new LeaderboardEntry(1, "Warrior_1", 15800, "W"));
        entries.add(new LeaderboardEntry(2, "PuzzlerPro", 14250, "P"));
        entries.add(new LeaderboardEntry(3, "SudokuMaster", 13900, "S"));
        entries.add(new LeaderboardEntry(4, "Alex", 12100, "A"));
        entries.add(new LeaderboardEntry(5, "Gamer2025", 11500, "G"));
        return entries;
    }

    public static List<LeaderboardEntry> getWeeklyLeaderboard() {
        List<LeaderboardEntry> entries = new ArrayList<>();
        entries.add(new LeaderboardEntry(1, "SudokuMaster", 98500, "S"));
        entries.add(new LeaderboardEntry(2, "Warrior_1", 95100, "W"));
        entries.add(new LeaderboardEntry(3, "Alex", 89000, "A"));
        entries.add(new LeaderboardEntry(4, "PuzzlerPro", 87250, "P"));
        entries.add(new LeaderboardEntry(5, "Newbie", 85000, "N"));
        return entries;
    }

    public static List<LeaderboardEntry> getAllTimeLeaderboard() {
        List<LeaderboardEntry> entries = new ArrayList<>();
        entries.add(new LeaderboardEntry(1, "SudokuKing", 1500200, "S"));
        entries.add(new LeaderboardEntry(2, "Legend27", 1450000, "L"));
        entries.add(new LeaderboardEntry(3, "SudokuMaster", 1390000, "S"));
        entries.add(new LeaderboardEntry(4, "Warrior_1", 1210000, "W"));
        entries.add(new LeaderboardEntry(5, "Alex", 1110000, "A"));
        return entries;
    }
}

