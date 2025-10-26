// Sudoku-App/app/src/main/java/com/example/sudoku/HomeActivity.java
package com.example.sudoku;

import androidx.annotation.NonNull; // Import NonNull
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton; // Import ImageButton
import android.widget.LinearLayout;
import android.widget.RelativeLayout; // Import RelativeLayout
import android.widget.TextView;
import android.widget.Toast;
import com.example.sudoku.data.local.SessionManager;
import com.example.sudoku.data.model.GameResponse; // Import GameResponse
import com.example.sudoku.data.model.PuzzleResponse; // Import PuzzleResponse
import com.example.sudoku.data.model.UserData;
import com.example.sudoku.data.model.UserResponse;
import com.example.sudoku.data.network.ApiService;
import com.example.sudoku.data.network.RetrofitClient;
import com.example.sudoku.utils.ProfileColorUtil; // Ensure this exists
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.List;
import java.util.Locale; // Import Locale

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private TextView welcomeUsernameText, homeProfileInitialText;
    private Button newGameButton;
    private RelativeLayout continueGameCard; // Card layout
    private TextView continueGameDetailsText; // Text inside the card
    private ImageButton continuePlayButton;   // Button inside the card
    private LinearLayout questsContainer;
    private BottomNavigationView bottomNavigationView;

    private ApiService apiService;
    private SessionManager sessionManager;
    private GameResponse inProgressGame = null; // Store the fetched in-progress game


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        apiService = RetrofitClient.getApiService(this);
        sessionManager = new SessionManager(this);

        // Find Views
        welcomeUsernameText = findViewById(R.id.welcome_username_text);
        homeProfileInitialText = findViewById(R.id.home_profile_initial_text);
        newGameButton = findViewById(R.id.new_game_button);
        continueGameCard = findViewById(R.id.continue_game_card);
        continueGameDetailsText = findViewById(R.id.continue_game_details_text);
        continuePlayButton = findViewById(R.id.continue_play_button);
        questsContainer = findViewById(R.id.quests_container);
        bottomNavigationView = findViewById(R.id.bottom_navigation_bar); // Use correct ID

        // Setup Listeners
        if (newGameButton != null) {
            newGameButton.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, DifficultyActivity.class);
                startActivity(intent);
            });
        }

        if (homeProfileInitialText != null) {
            homeProfileInitialText.setOnClickListener(v -> {
                Intent profileIntent = new Intent(HomeActivity.this, ProfileActivity.class);
                profileIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(profileIntent);
            });
        }

        // Add listener for the continue game card/button
        View.OnClickListener continueClickListener = v -> {
            // *** FIX: Check if game is NOT completed before allowing continue ***
            if (inProgressGame != null && !inProgressGame.wasCompleted() && inProgressGame.getPuzzle() != null) {
                Intent gameIntent = new Intent(HomeActivity.this, GameActivity.class);
                // Pass the *entire* GameResponse object, which includes the PuzzleResponse
                gameIntent.putExtra("EXISTING_GAME_DATA", inProgressGame);
                startActivity(gameIntent);
            } else {
                Toast.makeText(HomeActivity.this, "Error loading saved game or game already completed.", Toast.LENGTH_SHORT).show();
                Log.e("HomeActivity", "Attempted to continue game, but inProgressGame was null, completed, or puzzle was null.");
                // Refresh the UI state in case the game was completed elsewhere
                fetchInProgressGame();
            }
        };

        if (continueGameCard != null) continueGameCard.setOnClickListener(continueClickListener);
        if (continuePlayButton != null) continuePlayButton.setOnClickListener(continueClickListener);


        setupBottomNavigation();
        fetchUserProfile(); // Fetch user profile first
        // Fetch in-progress game will be called after user profile fetch succeeds

        populateDailyQuests(); // Keep mock quests for now
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set correct item without triggering listener loop
        if (bottomNavigationView.getSelectedItemId() != R.id.navigation_home) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        }
        // Re-fetch data in case user comes back from profile/game
        fetchUserProfile(); // Re-fetch user profile and then in-progress game
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            // Prevent re-launching the current activity
            if (itemId == R.id.navigation_home) {
                return true; // Already here, do nothing
            }

            Intent intent = null;
            if (itemId == R.id.navigation_ranks) {
                intent = new Intent(HomeActivity.this, LeaderboardActivity.class);
            } else if (itemId == R.id.navigation_challenges) {
                intent = new Intent(HomeActivity.this, ChallengeActivity.class);
            } else if (itemId == R.id.navigation_profile) {
                intent = new Intent(HomeActivity.this, ProfileActivity.class);
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true; // Return true to show item as selected
            }
            return false; // Item not handled
        });
    }


    private void fetchUserProfile() {
        if (apiService == null) {
            Log.e("HomeActivity", "ApiService is null, cannot fetch profile.");
            // Maybe redirect to login or show error
            return;
        }

        apiService.getUser().enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getMessage() != null) {
                    UserData userData = response.body().getMessage();
                    updateUI(userData);
                    // Now fetch the in-progress game AFTER successfully getting user data
                    fetchInProgressGame();
                } else {
                    Log.e("HomeActivity", "Error fetching user profile: " + response.code() + " - " + response.message());
                    if (response.code() == 401) { // Unauthorized
                        handleUnauthorizedError();
                    } else {
                        Toast.makeText(HomeActivity.this, "Error loading profile: " + response.message(), Toast.LENGTH_SHORT).show();
                        // Handle other errors, maybe show placeholders or retry
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                Log.e("HomeActivity", "Network error fetching user profile: " + t.getMessage(), t);
                // More robust handling: Check connectivity, redirect if needed
                handleNetworkError(); // Call the specific network error handler
            }
        });
    }

    // --- New method to fetch in-progress game ---
    private void fetchInProgressGame() {
        if (apiService == null) {
            Log.e("HomeActivity", "ApiService is null, cannot fetch in-progress game.");
            return;
        }

        apiService.getInProgressGame().enqueue(new Callback<GameResponse>() {
            @Override
            public void onResponse(@NonNull Call<GameResponse> call, @NonNull Response<GameResponse> response) {
                if (response.isSuccessful()) {
                    inProgressGame = response.body(); // Can be null if no game found
                    updateContinueCardVisibility();
                } else {
                    // Handle errors specifically for fetching the in-progress game
                    Log.e("HomeActivity", "Error fetching in-progress game: " + response.code() + " - " + response.message());
                    if (response.code() == 401) {
                        handleUnauthorizedError(); // Still handle auth errors
                    } else {
                        // Non-auth error, maybe just log it or show a subtle message
                        // Toast.makeText(HomeActivity.this, "Could not check for saved game.", Toast.LENGTH_SHORT).show();
                        inProgressGame = null; // Ensure it's null on error
                        updateContinueCardVisibility(); // Hide card if error occurs
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<GameResponse> call, @NonNull Throwable t) {
                Log.e("HomeActivity", "Network error fetching in-progress game: " + t.getMessage(), t);
                // Network error, treat as no game found for UI purposes
                inProgressGame = null;
                updateContinueCardVisibility();
                // Don't necessarily redirect to login for this specific error,
                // maybe just disable the continue button if it was previously visible.
            }
        });
    }

    // --- Updated method to update UI based on in-progress game ---
    private void updateContinueCardVisibility() {
        if (continueGameCard == null || newGameButton == null || continueGameDetailsText == null) return; // Views not ready

        // *** FIX: Check wasCompleted flag ***
        if (inProgressGame != null && !inProgressGame.wasCompleted() && inProgressGame.getPuzzle() != null) {
            // Show Continue Card, Hide New Game Button
            continueGameCard.setVisibility(View.VISIBLE);
            newGameButton.setVisibility(View.GONE);

            // Populate Card Details
            String difficulty = inProgressGame.getDifficulty() != null ?
                    Character.toUpperCase(inProgressGame.getDifficulty().charAt(0)) + inProgressGame.getDifficulty().substring(1)
                    : "Unknown";
            int seconds = inProgressGame.getDurationSeconds();
            String timeStr = String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60);
            String details = difficulty + " Puzzle - " + timeStr;
            continueGameDetailsText.setText(details);

            Log.d("HomeActivity", "Showing continue card for game: " + inProgressGame.getId());
        } else {
            // Hide Continue Card, Show New Game Button
            continueGameCard.setVisibility(View.GONE);
            newGameButton.setVisibility(View.VISIBLE);
            if (inProgressGame != null && inProgressGame.wasCompleted()) {
                Log.d("HomeActivity", "Last game was completed. Showing new game button.");
            } else {
                Log.d("HomeActivity", "No active in-progress game found. Showing new game button.");
            }
        }
    }


    private void updateUI(UserData userData) {
        if (userData == null) return;

        if (welcomeUsernameText != null) {
            welcomeUsernameText.setText(userData.getUsername());
        }
        if (homeProfileInitialText != null && userData.getUsername() != null && !userData.getUsername().isEmpty()) {
            String initial = userData.getUsername().substring(0, 1).toUpperCase();
            homeProfileInitialText.setText(initial);
            // Apply color using the utility class
            ProfileColorUtil.setProfileColor(homeProfileInitialText, initial);
        }
    }

    private void handleUnauthorizedError() {
        sessionManager.clear();
        RetrofitClient.clearInstance();
        Toast.makeText(this, "Session expired or invalid. Please log in again.", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void handleNetworkError() {
        // More robust handling for network failure during profile fetch
        Toast.makeText(this, "Could not connect to server. Please check your internet connection.", Toast.LENGTH_LONG).show();
        // Option 1: Stay on HomeActivity but maybe disable buttons or show placeholders
        // updateUI(null); // Clear username, show placeholders?
        // disableButtons();

        // Option 2: Redirect to login (more strict, ensures user can't proceed offline if profile is needed)
        handleUnauthorizedError(); // Reuse the logic to clear session and go to login
    }


    // --- Mock Quest Population (Keep as is for now) ---
    private void populateDailyQuests() {
        if (questsContainer == null) return;
        List<Quest> dailyQuests = QuestManager.getDailyQuests();
        LayoutInflater inflater = LayoutInflater.from(this);

        questsContainer.removeAllViews(); // Clear old quests

        for (Quest quest : dailyQuests) {
            View questView = inflater.inflate(R.layout.list_item_quest, questsContainer, false);
            TextView questTitle = questView.findViewById(R.id.quest_title);
            TextView questDescription = questView.findViewById(R.id.quest_description);
            // Assuming your list_item_quest.xml has these IDs now based on activity_home.xml
            // Update: We'll use the generic IDs from list_item_quest.xml

            questTitle.setText(quest.getTitle());
            questDescription.setText(quest.getDescription());

            questView.setOnClickListener(v -> {
                if (quest.isCompleted()) {
                    Toast.makeText(this, "Quest already completed!", Toast.LENGTH_SHORT).show();
                } else {
                    // Start game with specific difficulty for the quest
                    Intent intent = new Intent(HomeActivity.this, DifficultyActivity.class);
                    // Pass difficulty *specifically* for the quest
                    // intent.putExtra("QUEST_DIFFICULTY", quest.getDifficulty()); // Use a distinct key if needed
                    // For now, assume DifficultyActivity handles "DIFFICULTY" generically
                    intent.putExtra("DIFFICULTY", quest.getDifficulty());
                    startActivity(intent);
                }
            });

            if (quest.isCompleted()) {
                questView.setAlpha(0.6f);
                // Optionally change background or add checkmark icon
            }

            questsContainer.addView(questView);
        }
    }

}

