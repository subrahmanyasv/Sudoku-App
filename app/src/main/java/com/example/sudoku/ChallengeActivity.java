// Relative Path: app/src/main/java/com/example/sudoku/ChallengeActivity.java
package com.example.sudoku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.example.sudoku.data.local.SessionManager;
import com.example.sudoku.data.model.ChallengeResponse;
import com.example.sudoku.data.network.ApiService;
import com.example.sudoku.data.network.RetrofitClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID; // Import UUID
import java.util.stream.Collectors; // Import Collectors

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChallengeActivity extends AppCompatActivity {

    private static final String TAG = "ChallengeActivity"; // Logging Tag

    private RecyclerView recyclerView;
    private ChallengeAdapter adapter;
    private Button incomingButton;
    private Button outgoingButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressDialog progressDialog;

    private List<ChallengeResponse> allChallenges = new ArrayList<>(); // Store all fetched challenges
    private boolean showingIncoming = true; // Track current view state

    private ApiService apiService;
    private SessionManager sessionManager;
    private String currentUserId = null; // Store the logged-in user's ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge);

        apiService = RetrofitClient.getApiService(this);
        sessionManager = new SessionManager(this);
        fetchCurrentUserId(); // Fetch user ID early

        recyclerView = findViewById(R.id.challenge_recycler_view);
        incomingButton = findViewById(R.id.incoming_button);
        outgoingButton = findViewById(R.id.outgoing_button);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading challenges...");
        progressDialog.setCancelable(false);

        setupRecyclerView();
        setupTabListeners();
        setupBottomNavigation();
        setupSwipeRefresh();

        // Initial load: show Incoming challenges (will fetch data)
        selectTab(incomingButton); // Select incoming tab visually
        fetchChallenges();         // Fetch data
    }

    // Helper to get user ID from session/token (simplified example)
    private void fetchCurrentUserId() {
        String token = sessionManager.fetchAuthToken();
        if (token != null) {
            try {
                // Decode JWT locally - THIS IS NOT RECOMMENDED FOR PRODUCTION
                // In a real app, fetch user profile via API or pass ID during login
                // This is a placeholder to get the adapter working
                String[] split = token.split("\\.");
                String body = new String(android.util.Base64.decode(split[1], android.util.Base64.URL_SAFE));
                org.json.JSONObject jwtBody = new org.json.JSONObject(body);
                currentUserId = jwtBody.optString("id", null);
                Log.d(TAG, "Fetched current user ID (from token): " + currentUserId);
            } catch (Exception e) {
                Log.e(TAG, "Error decoding token for user ID", e);
                // Handle error - maybe redirect to login
                handleUnauthorizedError();
            }
        } else {
            handleUnauthorizedError(); // No token, redirect to login
        }
    }


    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Pass currentUserId to the adapter
        adapter = new ChallengeAdapter(new ArrayList<>(), this, apiService, this::fetchChallenges, currentUserId);
        recyclerView.setAdapter(adapter);
    }

    private void setupTabListeners() {
        incomingButton.setOnClickListener(v -> showIncomingChallenges());
        outgoingButton.setOnClickListener(v -> showOutgoingChallenges());
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::fetchChallenges);
        // Optional: Customize refresh indicator colors
        swipeRefreshLayout.setColorSchemeResources(R.color.accent_blue, R.color.accent_green, R.color.accent_orange);
    }

    // Fetches challenges from the backend
    private void fetchChallenges() {
        Log.d(TAG, "Fetching challenges...");
        if (currentUserId == null) {
            Log.w(TAG, "Cannot fetch challenges, user ID not available.");
            swipeRefreshLayout.setRefreshing(false);
            // Optionally show error or try fetching ID again
            return;
        }

        // Show progress dialog only if not already refreshing via swipe
        if (!swipeRefreshLayout.isRefreshing() && allChallenges.isEmpty()) {
            showLoading(true);
        }

        // Call the updated API endpoint
        apiService.getChallenges().enqueue(new Callback<List<ChallengeResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<ChallengeResponse>> call, @NonNull Response<List<ChallengeResponse>> response) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    allChallenges = response.body(); // Store the fetched (incoming + outgoing) challenges
                    Log.d(TAG, "Fetched " + allChallenges.size() + " total relevant challenges.");
                    updateDisplayedChallenges(); // Update RecyclerView based on the current tab
                    updateTabCounts();           // Update counts on both tabs

                } else {
                    Log.e(TAG, "Error fetching challenges: " + response.code() + " - " + response.message());
                    handleApiError(response, "Failed to load challenges");
                    allChallenges.clear(); // Clear local list on error
                    updateDisplayedChallenges();
                    updateTabCounts();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ChallengeResponse>> call, @NonNull Throwable t) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                Log.e(TAG, "Network error fetching challenges: " + t.getMessage(), t);
                Toast.makeText(ChallengeActivity.this, "Network Error. Please check connection.", Toast.LENGTH_SHORT).show();
                allChallenges.clear(); // Clear local list on network error
                updateDisplayedChallenges();
                updateTabCounts();
            }
        });
    }

    // Call this when switching tabs or after fetching data
    private void updateDisplayedChallenges() {
        if (currentUserId == null) {
            Log.w(TAG, "Cannot update display, user ID is null.");
            adapter.updateData(new ArrayList<>()); // Show empty list
            return;
        }

        List<ChallengeResponse> filteredList;
        if (showingIncoming) {
            // Filter for incoming (opponent is current user AND status is pending)
            filteredList = allChallenges.stream()
                    .filter(c -> currentUserId.equalsIgnoreCase(c.getOpponentId()) && "pending".equalsIgnoreCase(c.getStatus()))
                    .collect(Collectors.toList());
            Log.d(TAG, "Displaying " + filteredList.size() + " incoming challenges.");
        } else {
            // Filter for outgoing (challenger is current user AND status is pending or accepted)
            filteredList = allChallenges.stream()
                    .filter(c -> currentUserId.equalsIgnoreCase(c.getChallengerId()) && ("pending".equalsIgnoreCase(c.getStatus()) || "accepted".equalsIgnoreCase(c.getStatus())))
                    .collect(Collectors.toList());
            Log.d(TAG, "Displaying " + filteredList.size() + " outgoing challenges.");
        }
        adapter.updateData(filteredList);
    }


    private void showIncomingChallenges() {
        if (showingIncoming && !swipeRefreshLayout.isRefreshing()) return; // Do nothing if already selected and not refreshing
        showingIncoming = true;
        selectTab(incomingButton); // Visually select tab first
        updateDisplayedChallenges(); // Update the display with filtered data
    }

    private void showOutgoingChallenges() {
        if (!showingIncoming && !swipeRefreshLayout.isRefreshing()) return; // Do nothing if already selected and not refreshing
        showingIncoming = false;
        selectTab(outgoingButton); // Visually select tab first
        updateDisplayedChallenges(); // Update display with filtered data
    }

    // Update counts on both tabs based on the *full* fetched list
    private void updateTabCounts() {
        if (currentUserId == null) {
            Log.w(TAG, "Cannot update tab counts, user ID is null.");
            incomingButton.setText("Incoming (0)");
            outgoingButton.setText("Outgoing (0)");
            return;
        }

        long incomingCount = allChallenges.stream()
                .filter(c -> currentUserId.equalsIgnoreCase(c.getOpponentId()) && "pending".equalsIgnoreCase(c.getStatus()))
                .count();

        long outgoingCount = allChallenges.stream()
                .filter(c -> currentUserId.equalsIgnoreCase(c.getChallengerId()) && ("pending".equalsIgnoreCase(c.getStatus()) || "accepted".equalsIgnoreCase(c.getStatus())))
                .count();

        incomingButton.setText(String.format(Locale.getDefault(), "Incoming (%d)", incomingCount));
        outgoingButton.setText(String.format(Locale.getDefault(), "Outgoing (%d)", outgoingCount));
    }


    private void updateTabStyles(Button selected, Button unselected) {
        // Update counts before applying style
        updateTabCounts();

        // Apply visual styles
        selected.setBackgroundResource(R.drawable.tab_selected_background);
        selected.setTextColor(ContextCompat.getColor(this, R.color.primary_background)); // Dark text

        unselected.setBackgroundResource(android.R.color.transparent);
        unselected.setTextColor(ContextCompat.getColor(this, R.color.text_secondary)); // Lighter text
    }

    // Helper to visually select a tab and manage the state
    private void selectTab(Button tabToSelect) {
        boolean wasIncoming = showingIncoming;
        showingIncoming = (tabToSelect.getId() == R.id.incoming_button);

        // Update styles based on the new state
        if (showingIncoming) {
            updateTabStyles(incomingButton, outgoingButton);
        } else {
            updateTabStyles(outgoingButton, incomingButton);
        }

        // Only log or update display if the state actually changed
        if (wasIncoming != showingIncoming) {
            Log.d(TAG, "Switched to " + (showingIncoming ? "Incoming" : "Outgoing") + " tab.");
            updateDisplayedChallenges(); // Ensure display updates after tab style change
        }
    }


    // --- Helper to show/hide loading indicator ---
    private void showLoading(boolean isLoading) {
        if (progressDialog == null) return;
        if (isLoading && !progressDialog.isShowing()) {
            progressDialog.show();
        } else if (!isLoading && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_challenges);
        fetchCurrentUserId(); // Ensure user ID is fresh, in case of login changes
        fetchChallenges(); // Refresh challenges list
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Intent intent = null;

            if (itemId == R.id.navigation_home) {
                intent = new Intent(ChallengeActivity.this, HomeActivity.class);
            } else if (itemId == R.id.navigation_ranks) {
                intent = new Intent(ChallengeActivity.this, LeaderboardActivity.class);
            } else if (itemId == R.id.navigation_challenges) {
                if(!swipeRefreshLayout.isRefreshing()) fetchChallenges(); // Refresh if reselected, unless already swiping
                return true; // Stay on this screen
            } else if (itemId == R.id.navigation_profile) {
                intent = new Intent(ChallengeActivity.this, ProfileActivity.class);
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); // Use REORDER_TO_FRONT for smoother navigation
                startActivity(intent);
                // overridePendingTransition(R.anim.fade_in, R.anim.fade_out); // Optional transition
                return true;
            }
            return false;
        });
    }

    // --- Generic API Error Handler ---
    private void handleApiError(Response<?> response, String defaultMessage) {
        String errorMsg = defaultMessage;
        try {
            // Try to append standard HTTP error info
            if (response != null) { // Check if response is null
                errorMsg += ": " + response.code() + " " + response.message();
                // Optionally try to parse error body for more details
                // if (response.errorBody() != null) { errorMsg += " - " + response.errorBody().string(); }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding response info to error message", e);
        }
        Log.e(TAG, "API Error: " + errorMsg + (response != null ? " | Raw Message: " + response.message() : ""));
        Toast.makeText(ChallengeActivity.this, errorMsg, Toast.LENGTH_LONG).show();

        if (response != null && response.code() == 401) { // Unauthorized
            handleUnauthorizedError();
        }
    }

    // --- Unauthorized Error Handler ---
    private void handleUnauthorizedError() {
        if (sessionManager != null) sessionManager.clear();
        RetrofitClient.clearInstance();
        Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity(); // Close all activities in the task to prevent back navigation
    }
}

