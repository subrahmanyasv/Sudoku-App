// Relative Path: Sudoku-App/app/src/main/java/com/example/sudoku/HomeActivity.java
package com.example.sudoku;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
// Removed Button import as it's not directly used for main actions anymore
import android.widget.ImageView; // Keep ImageView
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sudoku.data.local.SessionManager;
import com.example.sudoku.data.model.UserData;
import com.example.sudoku.data.model.UserResponse;
import com.example.sudoku.data.network.ApiService;
import com.example.sudoku.data.network.RetrofitClient;
import com.example.sudoku.utils.ProfileColorUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List; // Keep List import

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private ApiService apiService;
    private TextView welcomeUsernameText, profileInitialText;
    private RelativeLayout continueGameCard;
    private TextView continueGameDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sessionManager = new SessionManager(this);
        apiService = RetrofitClient.getApiService(this);

        welcomeUsernameText = findViewById(R.id.welcome_username_text);
        profileInitialText = findViewById(R.id.home_profile_initial_text);
        continueGameCard = findViewById(R.id.continue_game_card);
        continueGameDetails = findViewById(R.id.continue_game_details);

        // --- Wire up new game card ---
        CardView newGameCard = findViewById(R.id.new_game_card);
        // Removed settingsIcon find as it's removed from layout

        newGameCard.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, DifficultyActivity.class);
            startActivity(intent);
        });

        // --- Wire up profile initial click ---
        profileInitialText.setOnClickListener(v -> {
            Intent profileIntent = new Intent(HomeActivity.this, ProfileActivity.class);
            profileIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(profileIntent);
        });

        // --- Wire up continue game card click ---
        continueGameCard.setOnClickListener(v -> {
            // TODO: Add logic to load the saved game state
            Toast.makeText(this, "Continue game clicked!", Toast.LENGTH_SHORT).show();
            // Example:
            // Intent intent = new Intent(this, GameActivity.class);
            // intent.putExtra("LOAD_SAVED_GAME", true);
            // startActivity(intent);
        });

        // Removed settingsIcon onClickListener

        // --- Hide continue game card initially ---
        continueGameCard.setVisibility(View.GONE); // Hide until we know there's a saved game
        // TODO: Add logic later to check for saved game state and show this card

        // Dynamic quest loading logic (using mock data for now)
        populateMockQuests();

        // Setup Bottom Navigation
        setupBottomNavigation();

        // Fetch user data
        fetchUserProfile();
    }

    // Removed startGame method

    private void fetchUserProfile() {
        apiService.getUser().enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse userResponse = response.body();
                    if ("success".equals(userResponse.getStatus()) && userResponse.getMessage() != null) {
                        updateHomeUI(userResponse.getMessage());
                    } else {
                        // Handle cases where API returns success false or empty message
                        Toast.makeText(HomeActivity.this, "Could not fetch user data (API error).", Toast.LENGTH_SHORT).show();
                        // Consider redirecting to login if data is essential
                        // handleUnauthorizedError(); // Or a specific error handling
                    }
                } else {
                    if (response.code() == 401) { // Unauthorized
                        handleUnauthorizedError();
                    } else {
                        Toast.makeText(HomeActivity.this, "Error fetching profile: " + response.code(), Toast.LENGTH_SHORT).show();
                        // Redirect if necessary based on error code
                    }
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e("HomeActivity", "Network failure: " + t.getMessage(), t); // Log the full stack trace
                Toast.makeText(HomeActivity.this, "Could not connect to server. Please check your internet connection.", Toast.LENGTH_LONG).show();

                // --- ROBUST OFFLINE HANDLING ---
                // Redirect back to MainActivity (Login) if the initial fetch fails
                handleUnauthorizedError(); // Treat network failure on load like an invalid session
            }
        });
    }

    private void updateHomeUI(UserData user) {
        welcomeUsernameText.setText(user.getUsername());
        String username = user.getUsername();
        if (username != null && !username.isEmpty()) {
            char initial = username.toUpperCase().charAt(0);
            profileInitialText.setText(String.valueOf(initial));
            // Ensure ProfileColorUtil is correctly applying background
            profileInitialText.setBackground(ProfileColorUtil.getProfileDrawable(this, initial));
        } else {
            // Handle case where username is null or empty
            profileInitialText.setText("?");
            profileInitialText.setBackground(ProfileColorUtil.getProfileDrawable(this, '?'));
        }
    }

    private void handleUnauthorizedError() {
        // Token is invalid, expired, or initial network failed
        sessionManager.clearAuthToken();
        RetrofitClient.clearInstance(); // Clear the Retrofit instance

        Toast.makeText(this, "Session invalid or network error. Please log in.", Toast.LENGTH_LONG).show();

        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear the task stack
        startActivity(intent);
        finish(); // Finish HomeActivity
    }


    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_bar);
        // Ensure the correct item is selected without triggering the listener unnecessarily
        if (bottomNavigationView.getSelectedItemId() != R.id.navigation_home) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        }
    }

    private void populateMockQuests() {
        TextView questTitle1 = findViewById(R.id.quest_title_1);
        TextView questDesc1 = findViewById(R.id.quest_description_1);
        TextView questTitle2 = findViewById(R.id.quest_title_2);
        TextView questDesc2 = findViewById(R.id.quest_description_2);

        // TODO: Fetch quests dynamically. Using mock data for now.
        List<Quest> dailyQuests = QuestManager.getDailyQuests(); // Assuming this returns 2 quests

        LinearLayout questContainer1 = (LinearLayout) questTitle1.getParent().getParent(); // Get the parent LinearLayout
        LinearLayout questContainer2 = (LinearLayout) questTitle2.getParent().getParent(); // Get the parent LinearLayout


        if (dailyQuests.size() > 0) {
            Quest quest1 = dailyQuests.get(0);
            questTitle1.setText(quest1.getTitle());
            questDesc1.setText(quest1.getDescription());
            questContainer1.setOnClickListener(v -> handleQuestClick(quest1));
            questContainer1.setAlpha(quest1.isCompleted() ? 0.6f : 1.0f); // Adjust alpha based on completion

        } else {
            questContainer1.setVisibility(View.GONE); // Hide if no quest data
        }

        if (dailyQuests.size() > 1) {
            Quest quest2 = dailyQuests.get(1);
            questTitle2.setText(quest2.getTitle());
            questDesc2.setText(quest2.getDescription());
            questContainer2.setOnClickListener(v -> handleQuestClick(quest2));
            questContainer2.setAlpha(quest2.isCompleted() ? 0.6f : 1.0f); // Adjust alpha based on completion

        } else {
            questContainer2.setVisibility(View.GONE); // Hide if no second quest data
        }
    }

    // Helper method for quest clicks
    private void handleQuestClick(Quest quest) {
        if (quest.isCompleted()) {
            Toast.makeText(this, "Quest '" + quest.getTitle() + "' already completed!", Toast.LENGTH_SHORT).show();
        } else {
            // TODO: Potentially navigate to a specific game mode or difficulty based on the quest
            Toast.makeText(this, "Starting quest: " + quest.getTitle(), Toast.LENGTH_SHORT).show();
            // Example: Start a game matching the quest difficulty
            Intent intent = new Intent(HomeActivity.this, DifficultyActivity.class); // Go to difficulty selection first
            // Or directly to GameActivity if difficulty is known:
            // Intent intent = new Intent(HomeActivity.this, GameActivity.class);
            // intent.putExtra("DIFFICULTY", quest.getDifficulty().toUpperCase());
            startActivity(intent);
        }
    }


    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_bar);
        // Set selected item *before* the listener to avoid initial trigger
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Intent intent = null;

            if (itemId == R.id.navigation_home) {
                // Already on this screen, do nothing
                return true;
            } else if (itemId == R.id.navigation_ranks) {
                intent = new Intent(HomeActivity.this, LeaderboardActivity.class);
            } else if (itemId == R.id.navigation_challenges) {
                intent = new Intent(HomeActivity.this, ChallengeActivity.class);
            } else if (itemId == R.id.navigation_profile) {
                intent = new Intent(HomeActivity.this, ProfileActivity.class);
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                // No finish() here, keep HomeActivity in the back stack
                return true;
            }

            return false;
        });
    }
}

