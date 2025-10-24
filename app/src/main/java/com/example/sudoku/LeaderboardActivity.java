// Relative Path: Sudoku-App/app/src/main/java/com/example/sudoku/LeaderboardActivity.java
package com.example.sudoku;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent; // Import this
import android.os.Bundle;
import android.widget.Button;
import com.google.android.material.bottomnavigation.BottomNavigationView; // Import this

import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    private LeaderboardAdapter adapter;
    private Button dailyButton, weeklyButton, allTimeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        RecyclerView recyclerView = findViewById(R.id.leaderboard_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dailyButton = findViewById(R.id.daily_button);
        weeklyButton = findViewById(R.id.weekly_button);
        allTimeButton = findViewById(R.id.all_time_button);

        // Initial setup with daily data
        List<LeaderboardEntry> dailyEntries = LeaderboardManager.getDailyLeaderboard();
        adapter = new LeaderboardAdapter(dailyEntries, this);
        recyclerView.setAdapter(adapter);
        updateButtonStyles(dailyButton); // Set Daily as selected initially

        dailyButton.setOnClickListener(v -> {
            List<LeaderboardEntry> entries = LeaderboardManager.getDailyLeaderboard();
            adapter.updateData(entries);
            updateButtonStyles(dailyButton);
        });

        weeklyButton.setOnClickListener(v -> {
            List<LeaderboardEntry> entries = LeaderboardManager.getWeeklyLeaderboard();
            adapter.updateData(entries);
            updateButtonStyles(weeklyButton);
        });

        allTimeButton.setOnClickListener(v -> {
            List<LeaderboardEntry> entries = LeaderboardManager.getAllTimeLeaderboard();
            adapter.updateData(entries);
            updateButtonStyles(allTimeButton);
        });

        // --- ADDED NAVIGATION LOGIC ---
        setupBottomNavigation();
    }

    private void updateButtonStyles(Button selectedButton) {
        Button[] buttons = {dailyButton, weeklyButton, allTimeButton};
        for (Button button : buttons) {
            if (button == selectedButton) {
                // Apply selected style
                button.setBackgroundResource(R.drawable.tab_selected_background);
                button.setTextColor(ContextCompat.getColor(this, R.color.background_dark));
            } else {
                // Apply unselected style
                button.setBackgroundResource(android.R.color.transparent);
                button.setTextColor(ContextCompat.getColor(this, R.color.white));
            }
        }
    }

    // --- ADDED onResume ---
    @Override
    protected void onResume() {
        super.onResume();
        // Ensure the "Ranks" item is selected when returning to this activity
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_ranks);
    }

    // --- ADDED setupBottomNavigation ---
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Intent intent = null;

            if (itemId == R.id.navigation_home) {
                intent = new Intent(LeaderboardActivity.this, HomeActivity.class);
            } else if (itemId == R.id.navigation_ranks) {
                return true; // Already on this screen, do nothing
            } else if (itemId == R.id.navigation_challenges) {
                intent = new Intent(LeaderboardActivity.this, ChallengeActivity.class);
            } else if (itemId == R.id.navigation_profile) {
                intent = new Intent(LeaderboardActivity.this, ProfileActivity.class); // Fixed typo here
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }
}

