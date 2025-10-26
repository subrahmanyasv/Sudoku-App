// Relative Path: Sudoku-App/app/src/main/java/com/example/sudoku/ProfileActivity.java
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
import android.widget.FrameLayout; // Keep FrameLayout if used, otherwise remove
import android.widget.GridLayout; // For achievements
import android.widget.ImageView; // For achievements
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sudoku.data.local.SessionManager;
import com.example.sudoku.data.model.GameResponse; // Use GameResponse for history
import com.example.sudoku.data.model.UserData;
import com.example.sudoku.data.model.UserResponse;
import com.example.sudoku.data.network.ApiService;
import com.example.sudoku.data.network.RetrofitClient;
import com.example.sudoku.utils.ProfileColorUtil; // Ensure this exists and works
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout; // Use TabLayout

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    // Views based on your original activity_profile.xml
    private TextView usernameText, emailText, profileInitialText;
    private TextView textTotalScore, textGamesPlayed, textBestEasy, textBestMedium, textBestHard, textWinRate;
    private TabLayout tabLayout; // Use TabLayout
    private LinearLayout statsLayout; // Stats content area
    private GridLayout achievementsGrid; // Achievements content area (assuming GridLayout)
    private LinearLayout historyLayout; // History content area
    private Button logoutButton;

    // History RecyclerView and Adapter
    private RecyclerView historyRecyclerView;
    private GameHistoryAdapter gameHistoryAdapter; // Adapter uses GameResponse now
    private List<GameResponse> gameHistoryList = new ArrayList<>(); // Use GameResponse list
    private boolean historyLoaded = false;

    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Views based on your activity_profile.xml IDs
        usernameText = findViewById(R.id.username_text); // Correct ID
        emailText = findViewById(R.id.email_text);       // Correct ID
        profileInitialText = findViewById(R.id.profile_initial_text); // Correct ID

        textTotalScore = findViewById(R.id.text_total_score); // Correct ID
        textGamesPlayed = findViewById(R.id.text_games_played); // Correct ID
        textBestEasy = findViewById(R.id.text_best_easy);      // Correct ID
        textBestMedium = findViewById(R.id.text_best_medium);    // Correct ID
        textBestHard = findViewById(R.id.text_best_hard);      // Correct ID
        textWinRate = findViewById(R.id.text_win_rate);        // Correct ID

        tabLayout = findViewById(R.id.tab_layout); // Correct ID for TabLayout
        statsLayout = findViewById(R.id.stats_layout);       // Correct ID
        achievementsGrid = findViewById(R.id.achievements_grid); // Correct ID
        historyLayout = findViewById(R.id.history_layout);     // Correct ID
        logoutButton = findViewById(R.id.logout_button);     // Correct ID

        // Initialize History RecyclerView
        historyRecyclerView = findViewById(R.id.history_recycler_view); // Correct ID
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // *** CORRECTED: Initialize GameHistoryAdapter with List<GameResponse> ***
        gameHistoryAdapter = new GameHistoryAdapter(gameHistoryList, this);
        historyRecyclerView.setAdapter(gameHistoryAdapter);

        apiService = RetrofitClient.getApiService(this);
        sessionManager = new SessionManager(this);

        setupTabLayoutListener(); // Setup listener for TabLayout
        setupLogoutButton();
        setupBottomNavigation();

        fetchUserProfile(); // Load user profile data initially

        // Set initial tab state (selects first tab and shows its content)
        TabLayout.Tab initialTab = tabLayout.getTabAt(0);
        if (initialTab != null) {
            initialTab.select();
            showTabContent(0); // Show content for the first tab
        } else {
            Log.e("ProfileActivity", "Could not get initial tab at position 0");
            statsLayout.setVisibility(View.VISIBLE); // Fallback to showing stats
        }
    }

    private void fetchUserProfile() {
        if (apiService == null) return;

        // *** CORRECTED: Use getUserProfile() instead of getUser() ***
        apiService.getUser().enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    UserData user = response.body().getMessage();
                    if (user != null) {
                        populateUI(user);
                    } else {
                        Log.w("ProfileActivity", "User data in response message is null");
                        Toast.makeText(ProfileActivity.this, "Could not load profile details.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("ProfileActivity", "Failed to fetch user profile: " + response.code() + " - " + response.message());
                    handleApiError(response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                Log.e("ProfileActivity", "Network error fetching profile: " + t.getMessage(), t);
                Toast.makeText(ProfileActivity.this, "Network error. Please check connection.", Toast.LENGTH_SHORT).show();
                // Consider how to handle UI in case of network failure (e.g., show empty state)
            }
        });
    }

    private void fetchGameHistory() {
        if (historyLoaded) return;
        if (apiService == null) return;

        Log.d("ProfileActivity", "Fetching game history...");
        apiService.getGameHistory().enqueue(new Callback<List<GameResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<GameResponse>> call, @NonNull Response<List<GameResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    gameHistoryList = response.body();
                    Log.d("ProfileActivity", "Game history fetched successfully. Count: " + gameHistoryList.size());
                    // *** CORRECTED: Update adapter with List<GameResponse> ***
                    runOnUiThread(() -> gameHistoryAdapter.updateData(gameHistoryList));
                    historyLoaded = true;
                } else {
                    Log.e("ProfileActivity", "Failed to fetch game history: " + response.code() + " - " + response.message());
                    handleApiError(response);
                    runOnUiThread(() -> gameHistoryAdapter.updateData(new ArrayList<>())); // Clear list on error
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<GameResponse>> call, @NonNull Throwable t) {
                Log.e("ProfileActivity", "Network error fetching game history: " + t.getMessage(), t);
                Toast.makeText(ProfileActivity.this, "Network error fetching history.", Toast.LENGTH_SHORT).show();
                runOnUiThread(() -> gameHistoryAdapter.updateData(new ArrayList<>())); // Clear list on error
            }
        });
    }

    private void populateUI(UserData user) {
        usernameText.setText(user.getUsername());
        emailText.setText(user.getEmail());

        // Use ProfileColorUtil for the initial background
        if (user.getUsername() != null && !user.getUsername().isEmpty()) {
            String initial = user.getUsername().substring(0, 1).toUpperCase();
            profileInitialText.setText(initial);
            // Assuming ProfileColorUtil provides a method like this
            profileInitialText.setBackground(ProfileColorUtil.getProfileDrawable(this, initial.charAt(0)));
        }

        // Populate Stats Tab using correct IDs
        textGamesPlayed.setText(String.valueOf(user.getTotal_games_played()));
        textTotalScore.setText(String.format(Locale.getDefault(), "%,d", user.getTotal_score()));
        textBestEasy.setText(String.format(Locale.getDefault(), "%,d", user.getBest_score_easy()));
        textBestMedium.setText(String.format(Locale.getDefault(), "%,d", user.getBest_score_medium()));
        textBestHard.setText(String.format(Locale.getDefault(), "%,d", user.getBest_score_hard()));

        // TODO: Calculate and display Win Rate (requires backend support or local calculation)
        textWinRate.setText("N/A"); // Placeholder remains

        // TODO: Populate Achievements Tab (requires data fetching and UI updates)
        // Example: Setup achievement icon clicks if needed
        setupAchievementIconClicks();
    }

    // Example of setting up achievement icon clicks (adapt as needed)
    private void setupAchievementIconClicks() {
        ImageView perfectionistIcon = findViewById(R.id.ach_perfectionist_icon);
        if (perfectionistIcon != null) {
            perfectionistIcon.setOnClickListener(v -> {
                // Check if achieved from data and show appropriate message
                Toast.makeText(ProfileActivity.this, "Achievement: Perfectionist!", Toast.LENGTH_SHORT).show();
            });
        }
        ImageView speedDemonIcon = findViewById(R.id.ach_speed_demon_icon);
        if (speedDemonIcon != null) {
            speedDemonIcon.setOnClickListener(v -> {
                // Check if achieved from data and show appropriate message
                Toast.makeText(ProfileActivity.this, "Achievement is Locked! Keep Playing.", Toast.LENGTH_SHORT).show();
            });
        }
        // Add listeners for other achievement icons...
    }


    private void setupTabLayoutListener() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showTabContent(tab.getPosition());
                if (tab.getPosition() == 2) { // History tab is at index 2
                    fetchGameHistory();
                }
                // Add logic for achievements tab if needed (e.g., fetchAchievements())
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Optional: actions when a tab is deselected
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Optional: actions if the currently selected tab is tapped again
                if (tab.getPosition() == 2) { // History tab is at index 2
                    fetchGameHistory(); // Re-fetch or refresh history
                }
            }
        });
    }

    // Method to show/hide content based on selected tab position
    private void showTabContent(int position) {
        statsLayout.setVisibility(View.GONE);
        achievementsGrid.setVisibility(View.GONE);
        historyLayout.setVisibility(View.GONE);

        switch (position) {
            case 0: // Stats
                statsLayout.setVisibility(View.VISIBLE);
                break;
            case 1: // Achievements
                achievementsGrid.setVisibility(View.VISIBLE);
                // TODO: Fetch/update achievements data here if needed
                break;
            case 2: // History
                historyLayout.setVisibility(View.VISIBLE);
                // fetchGameHistory() is called in onTabSelected
                break;
        }
    }


    private void setupLogoutButton() {
        logoutButton.setOnClickListener(v -> {
            sessionManager.clear(); // Use the unified clear method
            RetrofitClient.clearInstance();
            Toast.makeText(ProfileActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }


    private void setupBottomNavigation() {
        // Use the correct ID from your activity_profile.xml
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_bar);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Intent intent = null;

            if (itemId == R.id.navigation_home) {
                intent = new Intent(ProfileActivity.this, HomeActivity.class);
            } else if (itemId == R.id.navigation_ranks) {
                intent = new Intent(ProfileActivity.this, LeaderboardActivity.class);
            } else if (itemId == R.id.navigation_challenges) {
                intent = new Intent(ProfileActivity.this, ChallengeActivity.class);
            } else if (itemId == R.id.navigation_profile) {
                return true; // Already here
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ensure correct bottom nav item is selected
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_bar); // Correct ID
        bottomNavigationView.setSelectedItemId(R.id.navigation_profile);
        // Optionally re-fetch profile data if it might have changed,
        // but be mindful of reloading history unnecessarily if the tab isn't selected.
        // fetchUserProfile();
    }


    private void handleApiError(Response<?> response) {
        Log.e("ProfileActivity", "API Error: " + response.code() + " - " + response.message());
        if (response.code() == 401) { // Unauthorized
            handleUnauthorizedError();
        } else {
            String errorMsg = "Error: ";
            try {
                errorMsg += response.message();
                if (response.errorBody() != null) {
                    errorMsg += " - " + response.errorBody().string();
                }
            } catch (Exception e) {
                Log.e("ProfileActivity", "Error parsing error body", e);
                errorMsg += response.message() + " (Could not parse error body)";
            }
            Toast.makeText(ProfileActivity.this, errorMsg, Toast.LENGTH_LONG).show();
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

