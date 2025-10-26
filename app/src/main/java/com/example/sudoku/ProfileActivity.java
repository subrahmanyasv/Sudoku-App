// Relative Path: app/src/main/java/com/example/sudoku/ProfileActivity.java
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
// import android.widget.FrameLayout; // Keep FrameLayout if used, otherwise remove
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sudoku.data.local.SessionManager;
import com.example.sudoku.data.model.GameResponse; // Use GameResponse for history
import com.example.sudoku.data.model.UserData;
import com.example.sudoku.data.model.UserResponse;
import com.example.sudoku.data.network.ApiService;
import com.example.sudoku.data.network.RetrofitClient;
import com.example.sudoku.utils.ProfileColorUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    // --- Views ---
    private TextView usernameText, emailText, profileInitialText;
    private TextView textTotalScore, textGamesPlayed, textBestEasy, textBestMedium, textBestHard, textWinRate;
    private TabLayout tabLayout;
    private LinearLayout statsLayout;
    private GridLayout achievementsGrid;
    private LinearLayout historyLayout;
    private RecyclerView historyRecyclerView;
    private Button logoutButton;
    private BottomNavigationView bottomNavigationView; // Added

    // --- Data & Adapters ---
    private GameHistoryAdapter gameHistoryAdapter;
    private List<GameResponse> gameHistoryList = new ArrayList<>();
    private boolean historyLoaded = false;
    private String currentUserId = null; // Store user ID

    // --- Services ---
    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Services
        apiService = RetrofitClient.getApiService(this);
        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.fetchUserId(); // Fetch user ID

        // Initialize Views
        findViews(); // Encapsulate finding views

        // Setup History RecyclerView
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // *** MODIFIED: Pass currentUserId to adapter ***
        gameHistoryAdapter = new GameHistoryAdapter(gameHistoryList, this, currentUserId);
        historyRecyclerView.setAdapter(gameHistoryAdapter);

        // Setup Listeners
        setupTabLayoutListener();
        setupLogoutButton();
        setupBottomNavigation();
        setupAchievementIconClicks(); // Setup achievement clicks (remains mock)

        // Load Data
        fetchUserProfile(); // Load user profile data initially

        // Set initial tab state
        selectInitialTab();
    }

    private void findViews() {
        usernameText = findViewById(R.id.username_text);
        emailText = findViewById(R.id.email_text);
        profileInitialText = findViewById(R.id.profile_initial_text);
        textTotalScore = findViewById(R.id.text_total_score);
        textGamesPlayed = findViewById(R.id.text_games_played);
        textBestEasy = findViewById(R.id.text_best_easy);
        textBestMedium = findViewById(R.id.text_best_medium);
        textBestHard = findViewById(R.id.text_best_hard);
        textWinRate = findViewById(R.id.text_win_rate);
        tabLayout = findViewById(R.id.tab_layout);
        statsLayout = findViewById(R.id.stats_layout);
        achievementsGrid = findViewById(R.id.achievements_grid);
        historyLayout = findViewById(R.id.history_layout);
        historyRecyclerView = findViewById(R.id.history_recycler_view);
        logoutButton = findViewById(R.id.logout_button);
        bottomNavigationView = findViewById(R.id.bottom_navigation_bar); // Find bottom nav
    }

    private void selectInitialTab() {
        TabLayout.Tab initialTab = tabLayout.getTabAt(0);
        if (initialTab != null) {
            initialTab.select();
            showTabContent(0); // Show content for the first tab
        } else {
            Log.e("ProfileActivity", "Could not get initial tab at position 0");
            statsLayout.setVisibility(View.VISIBLE); // Fallback
        }
    }


    private void fetchUserProfile() {
        if (apiService == null) {
            Log.e("ProfileActivity", "ApiService is null. Cannot fetch profile.");
            // Consider showing an error state or attempting re-initialization
            return;
        }
        // Check if user ID is available
        if (currentUserId == null) {
            Log.e("ProfileActivity", "User ID is null. Cannot fetch profile. Redirecting to login.");
            handleUnauthorizedError(); // Treat missing ID as needing re-login
            return;
        }

        Log.d("ProfileActivity", "Fetching user profile for ID: " + currentUserId);
        apiService.getUser().enqueue(new Callback<UserResponse>() { // Assuming getUser() uses the token implicitly
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    UserData user = response.body().getMessage();
                    if (user != null) {
                        Log.d("ProfileActivity", "User profile fetched successfully for: " + user.getUsername());
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
                // Consider showing cached data or an error state
            }
        });
    }

    private void fetchGameHistory() {
        if (historyLoaded) {
            Log.d("ProfileActivity", "Game history already loaded. Skipping fetch.");
            // Optionally add a refresh mechanism if needed later
            return;
        }
        if (apiService == null || currentUserId == null) {
            Log.e("ProfileActivity", "ApiService or UserID null. Cannot fetch history.");
            return; // Don't proceed if prerequisites missing
        }

        Log.d("ProfileActivity", "Fetching game history...");
        apiService.getGameHistory().enqueue(new Callback<List<GameResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<GameResponse>> call, @NonNull Response<List<GameResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    gameHistoryList = response.body();
                    Log.d("ProfileActivity", "Game history fetched successfully. Count: " + gameHistoryList.size());
                    // Update adapter on UI thread
                    runOnUiThread(() -> {
                        if(gameHistoryAdapter != null) {
                            gameHistoryAdapter.updateData(gameHistoryList);
                            Log.d("ProfileActivity", "Adapter updated with history data.");
                        } else {
                            Log.e("ProfileActivity", "gameHistoryAdapter is null when trying to update data.");
                        }
                    });
                    historyLoaded = true;
                } else {
                    Log.e("ProfileActivity", "Failed to fetch game history: " + response.code() + " - " + response.message());
                    handleApiError(response); // Use generic handler
                    runOnUiThread(() -> { if (gameHistoryAdapter != null) gameHistoryAdapter.updateData(new ArrayList<>()); }); // Clear list on error
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<GameResponse>> call, @NonNull Throwable t) {
                Log.e("ProfileActivity", "Network error fetching game history: " + t.getMessage(), t);
                Toast.makeText(ProfileActivity.this, "Network error fetching history.", Toast.LENGTH_SHORT).show();
                runOnUiThread(() -> { if (gameHistoryAdapter != null) gameHistoryAdapter.updateData(new ArrayList<>()); }); // Clear list on error
            }
        });
    }


    private void populateUI(UserData user) {
        usernameText.setText(user.getUsername());
        emailText.setText(user.getEmail());

        if (user.getUsername() != null && !user.getUsername().isEmpty()) {
            String initial = user.getUsername().substring(0, 1).toUpperCase();
            profileInitialText.setText(initial);
            ProfileColorUtil.setProfileColor(profileInitialText, initial); // Use utility
        } else {
            profileInitialText.setText("?");
            profileInitialText.setBackgroundResource(R.drawable.circle_profile_background); // Default
        }


        // Populate Stats Tab
        textGamesPlayed.setText(String.valueOf(user.getTotal_games_played()));
        textTotalScore.setText(String.format(Locale.getDefault(), "%,d", user.getTotal_score()));
        textBestEasy.setText(String.format(Locale.getDefault(), "%,d", user.getBest_score_easy()));
        textBestMedium.setText(String.format(Locale.getDefault(), "%,d", user.getBest_score_medium()));
        textBestHard.setText(String.format(Locale.getDefault(), "%,d", user.getBest_score_hard()));

        // TODO: Calculate and display Win Rate
        // Requires #wins / #games_played. Backend might need to provide #wins or frontend calculate locally if history stores win status reliably.
        textWinRate.setText("N/A"); // Placeholder
    }


    private void setupTabLayoutListener() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                showTabContent(position);
                if (position == 2) { // History tab index
                    fetchGameHistory(); // Fetch history only when tab is selected
                }
                // Add logic for achievements tab if/when implemented
                // else if (position == 1) { fetchAchievements(); }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Optionally re-fetch data if tab is reselected
                if (tab.getPosition() == 2) {
                    historyLoaded = false; // Reset flag to allow re-fetching
                    fetchGameHistory();
                }
            }
        });
    }


    private void showTabContent(int position) {
        statsLayout.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
        achievementsGrid.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
        historyLayout.setVisibility(position == 2 ? View.VISIBLE : View.GONE);
    }

    // Example of setting up achievement icon clicks (mock)
    private void setupAchievementIconClicks() {
        // Find icons (add null checks for safety)
        ImageView firstWinIcon = findViewById(R.id.ach_first_win_icon);
        ImageView puzzleProIcon = findViewById(R.id.ach_puzzle_pro_icon);
        ImageView speedDemonIcon = findViewById(R.id.ach_speed_demon_icon);
        // ... find others ...

        // Example: Set state based on mock data or future API data
        setAchievementState(firstWinIcon, true); // Example: unlocked
        setAchievementState(puzzleProIcon, true); // Example: unlocked
        setAchievementState(speedDemonIcon, false); // Example: locked
        // ... set others ...

        // Add click listeners if desired (e.g., show achievement details)
        if (firstWinIcon != null) {
            firstWinIcon.setOnClickListener(v -> Toast.makeText(ProfileActivity.this, "Achievement: First Win!", Toast.LENGTH_SHORT).show());
        }
        if (speedDemonIcon != null) {
            speedDemonIcon.setOnClickListener(v -> Toast.makeText(ProfileActivity.this, "Achievement Locked: Speed Demon", Toast.LENGTH_SHORT).show());
        }
    }

    // Helper to set visual state of achievement icon
    private void setAchievementState(ImageView iconView, boolean unlocked) {
        if (iconView == null) return;
        if (unlocked) {
            iconView.setImageResource(R.drawable.circle_achievement_filled); // Or your specific unlocked icon
            // Optionally set alpha to 1.0f if it might be dimmed when locked
            // iconView.setAlpha(1.0f);
        } else {
            iconView.setImageResource(R.drawable.circle_achievement_empty); // Or your specific locked icon/placeholder
            // Optionally dim the icon
            // iconView.setAlpha(0.5f);
        }
    }


    private void setupLogoutButton() {
        logoutButton.setOnClickListener(v -> {
            Log.d("ProfileActivity", "Logout button clicked.");
            sessionManager.clear();
            RetrofitClient.clearInstance(); // Important to clear Retrofit instance with interceptor
            Toast.makeText(ProfileActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Finish current activity
        });
    }


    private void setupBottomNavigation() {
        // Ensure bottomNavigationView is not null
        if (bottomNavigationView == null) {
            Log.e("ProfileActivity", "BottomNavigationView not found!");
            return;
        }
        // Set the Profile item as selected initially
        bottomNavigationView.setSelectedItemId(R.id.navigation_profile);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Prevent re-navigating to the current screen
            if (itemId == R.id.navigation_profile) {
                return true; // Already here, do nothing
            }

            Intent intent = null;
            if (itemId == R.id.navigation_home) {
                intent = new Intent(ProfileActivity.this, HomeActivity.class);
            } else if (itemId == R.id.navigation_ranks) {
                intent = new Intent(ProfileActivity.this, LeaderboardActivity.class);
            } else if (itemId == R.id.navigation_challenges) {
                intent = new Intent(ProfileActivity.this, ChallengeActivity.class);
            }
            // No 'else' needed for navigation_profile as we handled it above

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); // Efficient navigation
                startActivity(intent);
                return true; // Indicate item selection was handled
            }
            return false; // Item selection not handled
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ensure correct bottom nav item is selected when returning
        if (bottomNavigationView != null && bottomNavigationView.getSelectedItemId() != R.id.navigation_profile) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_profile);
        }
        // Optionally refresh profile data if it might change frequently
        // fetchUserProfile();
        // Refresh history only if the history tab is currently selected
        if (tabLayout != null && tabLayout.getSelectedTabPosition() == 2) {
            historyLoaded = false; // Allow refresh on resume if history tab is active
            fetchGameHistory();
        }
    }


    // --- Error Handling ---

    private void handleApiError(Response<?> response) {
        String errorMsg = "API Error: ";
        try {
            errorMsg += response.code() + " - " + response.message();
            if (response.errorBody() != null) {
                String errorBodyStr = response.errorBody().string();
                Log.e("ProfileActivity", "Error Body: " + errorBodyStr); // Log raw error body
                // Try to parse common error structures (adjust based on your backend)
                if (errorBodyStr.contains("\"detail\":\"")) { // FastAPI default
                    errorMsg += " (" + errorBodyStr.split("\"detail\":\"")[1].split("\"")[0] + ")";
                } else if (errorBodyStr.contains("\"message\":\"")) { // Your AuthResponse structure?
                    errorMsg += " (" + errorBodyStr.split("\"message\":\"")[1].split("\"")[0] + ")";
                }
            }
        } catch (Exception e) {
            Log.e("ProfileActivity", "Error parsing error body", e);
        }
        Log.e("ProfileActivity", errorMsg); // Log the constructed message

        if (response.code() == 401) { // Unauthorized
            handleUnauthorizedError();
        } else {
            Toast.makeText(ProfileActivity.this, errorMsg, Toast.LENGTH_LONG).show();
        }
    }


    private void handleUnauthorizedError() {
        Log.e("ProfileActivity", "Unauthorized access detected. Clearing session and redirecting to login.");
        sessionManager.clear();
        RetrofitClient.clearInstance();
        Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity(); // Finish this and all parent activities
    }
}
