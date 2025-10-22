package com.example.sudoku;

import java.util.ArrayList;
import java.util.List;

public class GameHistoryManager {

    public static List<GameHistoryEntry> getHistory() {
        List<GameHistoryEntry> entries = new ArrayList<>();
        entries.add(new GameHistoryEntry("Medium", "12:45", true, 1050));
        entries.add(new GameHistoryEntry("Hard", "08:22", true, 1800));
        entries.add(new GameHistoryEntry("Easy", "05:30", false, 0));
        entries.add(new GameHistoryEntry("Medium", "14:10", true, 950));
        entries.add(new GameHistoryEntry("Easy", "04:55", true, 500));
        return entries;
    }
}