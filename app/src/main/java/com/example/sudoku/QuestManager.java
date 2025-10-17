package com.example.sudoku;

import java.util.ArrayList;
import java.util.List;

public class QuestManager {

    // In a real app, this would fetch quests from a server based on the date.
    // For this example, it returns a static list to simulate dynamic quests.
    public static List<Quest> getDailyQuests() {
        List<Quest> quests = new ArrayList<>();

        quests.add(new Quest("Speed Runner", "Complete a Medium puzzle in under 10 mins", "Medium", false));
        quests.add(new Quest("Flawless Victory", "Win a puzzle with 0 errors (Completed)", "Easy", true));
        // You could easily add more quests here for different days
        // quests.add(new Quest("Master's Challenge", "Complete a Hard puzzle", "Hard", false));

        return quests;
    }
}
