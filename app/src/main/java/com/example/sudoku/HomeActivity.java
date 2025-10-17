package com.example.sudoku;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button newGameButton = findViewById(R.id.new_game_button);
        newGameButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, DifficultyActivity.class);
            startActivity(intent);
        });

        // Dynamic quest loading logic
        populateDailyQuests();

        // Setup Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                // Already on Home screen
                return true;
            } else if (itemId == R.id.navigation_ranks) {
                // Navigate to Leaderboard
                Intent ranksIntent = new Intent(HomeActivity.this, LeaderboardActivity.class);
                // Use this flag to avoid creating a new instance if it already exists
                ranksIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(ranksIntent);
                return true;
            }
            // Handle other items like Challenges and Profile if you add them
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // This ensures the "Home" item is selected when you navigate back to this screen
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
    }

    private void populateDailyQuests() {
        LinearLayout questsContainer = findViewById(R.id.quests_container);
        List<Quest> dailyQuests = QuestManager.getDailyQuests();
        LayoutInflater inflater = LayoutInflater.from(this);

        // Clear any old quests before adding new ones
        questsContainer.removeAllViews();

        for (Quest quest : dailyQuests) {
            // Inflate the reusable quest card layout
            View questView = inflater.inflate(R.layout.list_item_quest, questsContainer, false);

            TextView questTitle = questView.findViewById(R.id.quest_title);
            TextView questDescription = questView.findViewById(R.id.quest_description);

            // Set the data for this specific quest
            questTitle.setText(quest.getTitle());
            questDescription.setText(quest.getDescription());

            // Handle clicks for each quest
            questView.setOnClickListener(v -> {
                if (quest.isCompleted()) {
                    Toast.makeText(this, "You have already completed this quest!", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(HomeActivity.this, GameActivity.class);
                    intent.putExtra("DIFFICULTY", quest.getDifficulty());
                    startActivity(intent);
                }
            });

            // Visually mark completed quests
            if (quest.isCompleted()) {
                questView.setAlpha(0.5f); // Make it slightly transparent
            }

            // Add the new quest card to the container
            questsContainer.addView(questView);
        }
    }
}

