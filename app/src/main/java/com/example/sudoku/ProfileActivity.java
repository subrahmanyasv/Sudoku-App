// Relative Path: Sudoku-App/app/src/main/java/com/example/sudoku/ProfileActivity.java
package com.example.sudoku;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sudoku.data.local.SessionManager;
import com.example.sudoku.data.model.UserData;
import com.example.sudoku.data.model.UserResponse;
import com.example.sudoku.data.network.ApiService;
import com.example.sudoku.data.network.RetrofitClient;
import com.example.sudoku.utils.ProfileColorUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private SessionManager sessionManager;
    private Button logoutButton;

    private ApiService apiService;
    private TextView usernameText, emailText, profileInitialText;

    private LinearLayout statsLayout;
    private GridLayout achievementsGrid;
    private LinearLayout historyLayout;

    private TextView textTotalScore, textGamesPlayed, textBestEasy, textBestMedium, textBestHard, textWinRate;

    private RecyclerView historyRecyclerView;
    private GameHistoryAdapter historyAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tabLayout = findViewById(R.id.tab_layout);
        logoutButton = findViewById(R.id.logout_button);

        usernameText = findViewById(R.id.username_text);
        emailText = findViewById(R.id.email_text);
        profileInitialText = findViewById(R.id.profile_initial_text);

        statsLayout = findViewById(R.id.stats_layout);
        achievementsGrid = findViewById(R.id.achievements_grid);
        historyLayout = findViewById(R.id.history_layout);

        textTotalScore = findViewById(R.id.text_total_score);
        textGamesPlayed = findViewById(R.id.text_games_played);
        textBestEasy = findViewById(R.id.text_best_easy);
        textBestMedium = findViewById(R.id.text_best_medium);
        textBestHard = findViewById(R.id.text_best_hard);
        textWinRate = findViewById(R.id.text_win_rate);

        historyRecyclerView = findViewById(R.id.history_recycler_view);

        sessionManager = new SessionManager(this);
        apiService = RetrofitClient.getApiService(this);

        TabLayout.Tab statsTab = tabLayout.getTabAt(0);
        if (statsTab != null) {
            statsTab.select();
        }
        showTabContent(0);

        fetchUserProfile();
        setupHistoryList();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showTabContent(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        ImageView perfectionistIcon = findViewById(R.id.ach_perfectionist_icon);
        perfectionistIcon.setOnClickListener(v -> {
            Toast.makeText(ProfileActivity.this, "Achievement: Perfectionist!", Toast.LENGTH_SHORT).show();
        });

        ImageView speedDemonIcon = findViewById(R.id.ach_speed_demon_icon);
        speedDemonIcon.setOnClickListener(v -> {
            Toast.makeText(ProfileActivity.this, "Achievement is Locked! Keep Playing.", Toast.LENGTH_SHORT).show();
        });

        logoutButton.setOnClickListener(v -> {
            sessionManager.clearAuthToken();
            RetrofitClient.clearInstance();

            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        setupBottomNavigation();
    }

    private void showTabContent(int position) {
        statsLayout.setVisibility(View.GONE);
        achievementsGrid.setVisibility(View.GONE);
        historyLayout.setVisibility(View.GONE);

        if (position == 0) {
            statsLayout.setVisibility(View.VISIBLE);
        } else if (position == 1) {
            achievementsGrid.setVisibility(View.VISIBLE);
        } else if (position == 2) {
            historyLayout.setVisibility(View.VISIBLE);
        }
    }

    private void fetchUserProfile() {
        apiService.getUser().enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse userResponse = response.body();
                    if ("success".equals(userResponse.getStatus()) && userResponse.getMessage() != null) {
                        updateProfileUI(userResponse.getMessage());
                    } else {
                        Toast.makeText(ProfileActivity.this, "Could not fetch user data.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (response.code() == 401) {
                        handleUnauthorizedError();
                    } else {
                        Toast.makeText(ProfileActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e("ProfileActivity", "Network failure: " + t.getMessage());
                Toast.makeText(ProfileActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfileUI(UserData user) {
        usernameText.setText(user.getUsername());
        emailText.setText(user.getEmail());

        String username = user.getUsername();
        if (username != null && !username.isEmpty()) {
            char initial = username.toUpperCase().charAt(0);
            profileInitialText.setText(String.valueOf(initial));
            profileInitialText.setBackground(ProfileColorUtil.getProfileDrawable(this, initial));
        }

        textTotalScore.setText(String.format(Locale.US, "%,d", user.getTotal_score()));
        textGamesPlayed.setText(String.format(Locale.US, "%d", user.getTotal_games_played()));
        textBestEasy.setText(String.format(Locale.US, "%,d", user.getBest_score_easy()));
        textBestMedium.setText(String.format(Locale.US, "%,d", user.getBest_score_medium()));
        textBestHard.setText(String.format(Locale.US, "%,d", user.getBest_score_hard()));

        if (user.getTotal_games_played() > 0) {
            // TODO: Calculate actual win rate once 'wins' field is available from backend
            textWinRate.setText("N/A");
        } else {
            textWinRate.setText("N/A");
        }
    }

    private void setupHistoryList() {
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<GameHistoryEntry> mockHistory = GameHistoryManager.getHistory(); // Using mock for now
        historyAdapter = new GameHistoryAdapter(mockHistory, this);
        historyRecyclerView.setAdapter(historyAdapter);
    }

    private void handleUnauthorizedError() {
        sessionManager.clearAuthToken();
        RetrofitClient.clearInstance();

        Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show();

        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_bar);
        bottomNavigationView.setSelectedItemId(R.id.navigation_profile);
    }

    private void setupBottomNavigation() {
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
                return true; // Already on this screen, do nothing
            }

            if (intent != null) {
                // Use singleTop to prevent multiple instances if already at the destination
                // This is crucial for avoiding the navigation bug
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                // No need to finish() here, as we want to keep HomeActivity in the back stack
                return true;
            }
            return false;
        });
    }
}


