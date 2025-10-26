// Relative Path: Sudoku-App/app/src/main/java/com/example/sudoku/LeaderboardActivity.java
package com.example.sudoku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sudoku.data.local.SessionManager;
import com.example.sudoku.data.model.FullLeaderboardData;
import com.example.sudoku.data.model.LeaderboardCategoryData;
import com.example.sudoku.data.model.LeaderboardEntryResponse;
import com.example.sudoku.data.model.LeaderboardResponse;
import com.example.sudoku.data.model.UserRankCategoryData;
import com.example.sudoku.data.model.UserRankEntry;
import com.example.sudoku.data.network.ApiService;
import com.example.sudoku.data.network.RetrofitClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeaderboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LeaderboardAdapter adapter;
    // Time Span Buttons
    private Button dailyButton, weeklyButton, allTimeButton;
    // Difficulty Buttons
    private Button easyButton, mediumButton, hardButton;
    // Optional: Add TextView for displaying user's rank if needed
    private TextView userRankTextView;

    private ApiService apiService;
    private SessionManager sessionManager;
    private FullLeaderboardData leaderboardDataCache = null; // Cache the full response
    private String currentDifficultyKey = "easy"; // Default difficulty
    private String currentTimespanKey = "daily"; // Default timespan

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        // Initialize Views
        recyclerView = findViewById(R.id.leaderboard_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Time Span Buttons
        dailyButton = findViewById(R.id.daily_button);
        weeklyButton = findViewById(R.id.weekly_button);
        allTimeButton = findViewById(R.id.all_time_button);

        // Difficulty Buttons
        easyButton = findViewById(R.id.easy_button);
        mediumButton = findViewById(R.id.medium_button);
        hardButton = findViewById(R.id.hard_button);

        // Optional: Initialize userRankTextView
        userRankTextView = findViewById(R.id.user_rank_text);

        // Initialize Network and Session
        apiService = RetrofitClient.getApiService(this);
        sessionManager = new SessionManager(this);

        // Initialize Adapter
        adapter = new LeaderboardAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        // Set up Listeners
        setupTimeSpanButtonListeners();
        setupDifficultyButtonListeners();

        // Fetch data and setup navigation
        fetchLeaderboardData();
        setupBottomNavigation();
    }

    private void setupTimeSpanButtonListeners() {
        dailyButton.setOnClickListener(v -> {
            currentTimespanKey = "daily";
            updateDisplayedList();
            updateTimeSpanButtonStyles(dailyButton);
            updateRankUI(); // Update user rank display
        });
        weeklyButton.setOnClickListener(v -> {
            currentTimespanKey = "weekly";
            updateDisplayedList();
            updateTimeSpanButtonStyles(weeklyButton);
            updateRankUI();
        });
        allTimeButton.setOnClickListener(v -> {
            currentTimespanKey = "allTime"; // Match POJO getter name
            updateDisplayedList();
            updateTimeSpanButtonStyles(allTimeButton);
            updateRankUI();
        });
    }

    private void setupDifficultyButtonListeners() {
        easyButton.setOnClickListener(v -> {
            currentDifficultyKey = "easy";
            updateDisplayedList();
            updateDifficultyButtonStyles(easyButton);
            updateRankUI();
        });
        mediumButton.setOnClickListener(v -> {
            currentDifficultyKey = "medium";
            updateDisplayedList();
            updateDifficultyButtonStyles(mediumButton);
            updateRankUI();
        });
        hardButton.setOnClickListener(v -> {
            currentDifficultyKey = "hard";
            updateDisplayedList();
            updateDifficultyButtonStyles(hardButton);
            updateRankUI();
        });
    }


    private void fetchLeaderboardData() {
        if (apiService == null) {
            Log.e("LeaderboardActivity", "ApiService is null.");
            Toast.makeText(this, "Error initializing network service.", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getLeaderboard().enqueue(new Callback<LeaderboardResponse>() {
            @Override
            public void onResponse(@NonNull Call<LeaderboardResponse> call, @NonNull Response<LeaderboardResponse> response) {
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    leaderboardDataCache = response.body().getData(); // Store the full data

                    if (leaderboardDataCache != null) {
                        // Display the initial list (e.g., Daily Easy)
                        updateDisplayedList(); // Use defaults (easy, daily)
                        updateTimeSpanButtonStyles(dailyButton); // Set Daily as selected initially
                        updateDifficultyButtonStyles(easyButton); // Set Easy as selected initially
                        updateRankUI(); // Display initial user rank
                    } else {
                        Log.w("LeaderboardActivity", "Leaderboard data received but was null.");
                        Toast.makeText(LeaderboardActivity.this, "No leaderboard data available.", Toast.LENGTH_SHORT).show();
                        adapter.updateData(new ArrayList<>()); // Show empty list
                        userRankTextView.setVisibility(View.GONE);
                    }
                } else {
                    handleApiError(response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<LeaderboardResponse> call, @NonNull Throwable t) {
                Log.e("LeaderboardActivity", "Network error fetching leaderboard: " + t.getMessage(), t);
                Toast.makeText(LeaderboardActivity.this, "Network error. Please check connection.", Toast.LENGTH_SHORT).show();
                adapter.updateData(new ArrayList<>()); // Show empty list on network error
                userRankTextView.setVisibility(View.GONE);
            }
        });
    }

    // Method to update the RecyclerView based on selected difficulty and timespan
    private void updateDisplayedList() {
        List<LeaderboardEntryResponse> entriesToShow = new ArrayList<>();

        if (leaderboardDataCache != null && leaderboardDataCache.getTopPlayers() != null) {
            Map<String, LeaderboardCategoryData> topPlayersMap = leaderboardDataCache.getTopPlayers();

            if (topPlayersMap.containsKey(currentDifficultyKey)) {
                LeaderboardCategoryData category = topPlayersMap.get(currentDifficultyKey);
                if (category != null) {
                    switch (currentTimespanKey) {
                        case "daily":
                            entriesToShow = category.getDaily();
                            break;
                        case "weekly":
                            entriesToShow = category.getWeekly();
                            break;
                        case "allTime":
                            entriesToShow = category.getAllTime();
                            break;
                    }
                } else {
                    Log.w("LeaderboardActivity", "Category data for difficulty '" + currentDifficultyKey + "' is null.");
                }
            } else {
                Log.w("LeaderboardActivity", "Difficulty key '" + currentDifficultyKey + "' not found in topPlayersMap.");
            }
        } else {
            Log.w("LeaderboardActivity", "Leaderboard cache is null or empty when trying to update list for " + currentDifficultyKey + " - " + currentTimespanKey);
        }

        if (entriesToShow == null) {
            entriesToShow = Collections.emptyList();
            Log.w("LeaderboardActivity", "Extracted list for "+ currentDifficultyKey + " - " + currentTimespanKey + " was null, showing empty.");
        } else {
            Log.d("LeaderboardActivity", "Displaying " + entriesToShow.size() + " entries for " + currentDifficultyKey + " - " + currentTimespanKey);
        }

        adapter.updateData(entriesToShow);
    }

    // Method to update styles for Time Span buttons
    private void updateTimeSpanButtonStyles(Button selectedButton) {
        Button[] buttons = {dailyButton, weeklyButton, allTimeButton};
        for (Button button : buttons) {
            if (button == selectedButton) {
                button.setBackgroundResource(R.drawable.tab_selected_background);
                button.setTextColor(ContextCompat.getColor(this, R.color.primary_background)); // Dark text on light background
            } else {
                button.setBackgroundResource(android.R.color.transparent);
                button.setTextColor(ContextCompat.getColor(this, R.color.text_secondary)); // Lighter text
            }
        }
    }

    // Method to update styles for Difficulty buttons
    private void updateDifficultyButtonStyles(Button selectedButton) {
        Button[] buttons = {easyButton, mediumButton, hardButton};
        for (Button button : buttons) {
            if (button == selectedButton) {
                button.setBackgroundResource(R.drawable.tab_selected_background);
                button.setTextColor(ContextCompat.getColor(this, R.color.primary_background));
            } else {
                button.setBackgroundResource(android.R.color.transparent);
                button.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            }
        }
    }

    // --- Method to update UI with user's rank ---
    private void updateRankUI() {
        if (userRankTextView == null || leaderboardDataCache == null || leaderboardDataCache.getUserRanks() == null) {
            if (userRankTextView != null) userRankTextView.setVisibility(View.GONE);
            return; // Nothing to update or data not ready
        }

        UserRankEntry currentUserRank = null;
        Map<String, UserRankCategoryData> userRanksMap = leaderboardDataCache.getUserRanks();

        if (userRanksMap != null && userRanksMap.containsKey(currentDifficultyKey)) {
            UserRankCategoryData userRanksForDifficulty = userRanksMap.get(currentDifficultyKey);
            if (userRanksForDifficulty != null) {
                switch (currentTimespanKey) {
                    case "daily":
                        currentUserRank = userRanksForDifficulty.getDaily();
                        break;
                    case "weekly":
                        currentUserRank = userRanksForDifficulty.getWeekly();
                        break;
                    case "allTime":
                        currentUserRank = userRanksForDifficulty.getAllTime();
                        break;
                }
            }
        }

        if (currentUserRank != null) {
            String timespanDisplay = "allTime".equals(currentTimespanKey) ? "All-Time" : capitalize(currentTimespanKey);
            userRankTextView.setText(String.format(Locale.getDefault(),
                    "Your Rank (%s - %s): #%d (Score: %d)",
                    capitalize(currentDifficultyKey), timespanDisplay,
                    currentUserRank.getRank(), currentUserRank.getTotalScore()));
            userRankTextView.setVisibility(View.VISIBLE);
        } else {
            String timespanDisplay = "allTime".equals(currentTimespanKey) ? "All-Time" : capitalize(currentTimespanKey);
            userRankTextView.setText(String.format("Your rank for %s - %s is not available.", capitalize(currentDifficultyKey), timespanDisplay));
            userRankTextView.setVisibility(View.VISIBLE); // Keep visible to show 'not available'
        }
    }

    // Helper to capitalize strings for display
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }


    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_ranks);

        if (leaderboardDataCache == null) {
            fetchLeaderboardData();
        } else {
            // Refresh display and button states with cached data
            updateDisplayedList();
            updateTimeSpanButtonStyles(getSelectedButton(currentTimespanKey, dailyButton, weeklyButton, allTimeButton));
            updateDifficultyButtonStyles(getSelectedButton(currentDifficultyKey, easyButton, mediumButton, hardButton));
            updateRankUI(); // Also refresh rank display
        }
    }

    // Helper to find the correct button based on the current key
    private Button getSelectedButton(String key, Button button1, Button button2, Button button3) {
        switch (key) {
            case "daily":
            case "easy":
                return button1;
            case "weekly":
            case "medium":
                return button2;
            case "allTime":
            case "hard":
                return button3;
            default:
                return button1; // Default fallback
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Intent intent = null;

            if (itemId == R.id.navigation_home) {
                intent = new Intent(LeaderboardActivity.this, HomeActivity.class);
            } else if (itemId == R.id.navigation_ranks) {
                return true; // Already on this screen
            } else if (itemId == R.id.navigation_challenges) {
                intent = new Intent(LeaderboardActivity.this, ChallengeActivity.class);
            } else if (itemId == R.id.navigation_profile) {
                intent = new Intent(LeaderboardActivity.this, ProfileActivity.class);
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void handleApiError(Response<?> response) {
        Log.e("LeaderboardActivity", "API Error: " + response.code() + " - " + response.message());
        if (response.code() == 401) {
            handleUnauthorizedError();
        } else {
            Toast.makeText(LeaderboardActivity.this, "Failed to load leaderboard: " + response.message(), Toast.LENGTH_SHORT).show();
            adapter.updateData(new ArrayList<>());
            userRankTextView.setVisibility(View.GONE);
        }
    }

    private void handleUnauthorizedError() {
        sessionManager.clear();
        RetrofitClient.clearInstance();
        Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

