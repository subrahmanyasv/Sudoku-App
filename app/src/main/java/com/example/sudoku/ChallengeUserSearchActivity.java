// Relative Path: app/src/main/java/com/example/sudoku/ChallengeUserSearchActivity.java
package com.example.sudoku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem; // For toolbar back button
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.sudoku.data.local.SessionManager;
import com.example.sudoku.data.model.ChallengeCreateRequest;
import com.example.sudoku.data.model.ChallengeResponse;
import com.example.sudoku.data.model.UserBase;
import com.example.sudoku.data.network.ApiService;
import com.example.sudoku.data.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects; // For Objects.requireNonNull

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChallengeUserSearchActivity extends AppCompatActivity implements UserSearchAdapter.OnUserSelectedListener {

    private static final String TAG = "ChallengeUserSearch";
    private static final long SEARCH_DELAY_MS = 500; // Delay for debouncing search input

    private SearchView searchView;
    private RecyclerView recyclerView;
    private UserSearchAdapter adapter;
    private ProgressBar progressBar;
    private Toolbar toolbar;
    private ProgressDialog challengeProgressDialog; // For creating challenge

    private ApiService apiService;
    private SessionManager sessionManager;

    private String puzzleId; // Received from ResultsActivity
    private int challengerDurationSeconds; // Received from ResultsActivity

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_user_search);

        apiService = RetrofitClient.getApiService(this);
        sessionManager = new SessionManager(this);

        // Retrieve data from ResultsActivity
        Intent intent = getIntent();
        puzzleId = intent.getStringExtra(ResultsActivity.KEY_PUZZLE_ID);
        challengerDurationSeconds = intent.getIntExtra(ResultsActivity.KEY_TIME_SECONDS, -1);

        if (puzzleId == null || challengerDurationSeconds == -1) {
            Log.e(TAG, "Missing puzzleId or duration from ResultsActivity");
            Toast.makeText(this, "Error: Missing data needed to create challenge.", Toast.LENGTH_LONG).show();
            finish(); // Close activity if essential data is missing
            return;
        }

        // Initialize Views
        toolbar = findViewById(R.id.toolbar);
        searchView = findViewById(R.id.user_search_view);
        recyclerView = findViewById(R.id.user_search_recycler_view);
        progressBar = findViewById(R.id.search_progress_bar);

        // Setup Toolbar
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserSearchAdapter(this, this);
        recyclerView.setAdapter(adapter);

        setupSearchView();
        fetchUserList(null); // Initial fetch without query
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Trigger search immediately on submit
                searchHandler.removeCallbacks(searchRunnable);
                fetchUserList(query);
                searchView.clearFocus(); // Hide keyboard
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Debounce search input
                searchHandler.removeCallbacks(searchRunnable);
                searchRunnable = () -> fetchUserList(newText.trim());
                searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
                return true;
            }
        });
    }

    private void fetchUserList(String query) {
        showLoading(true);
        Log.d(TAG, "Fetching user list with query: " + query);

        apiService.getUserList(query).enqueue(new Callback<List<UserBase>>() {
            @Override
            public void onResponse(@NonNull Call<List<UserBase>> call, @NonNull Response<List<UserBase>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Fetched " + response.body().size() + " users.");
                    adapter.updateData(response.body());
                } else {
                    handleApiError(response, "fetch user list");
                    adapter.updateData(new ArrayList<>()); // Clear list on error
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<UserBase>> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e(TAG, "Network error fetching user list: " + t.getMessage(), t);
                Toast.makeText(ChallengeUserSearchActivity.this, "Network error fetching users.", Toast.LENGTH_SHORT).show();
                adapter.updateData(new ArrayList<>()); // Clear list on error
            }
        });
    }

    @Override
    public void onUserSelected(UserBase user) {
        if (user == null || user.getId() == null) {
            Toast.makeText(this, "Invalid user selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Confirmation Dialog (Optional but recommended)
        new android.app.AlertDialog.Builder(this)
                .setTitle("Confirm Challenge")
                .setMessage("Challenge " + user.getUsername() + "?")
                .setPositiveButton("Challenge", (dialog, which) -> {
                    createChallenge(user.getId());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void createChallenge(String opponentId) {
        showChallengeLoading(true, "Sending challenge...");
        Log.d(TAG, "Creating challenge: PuzzleID=" + puzzleId + ", OpponentID=" + opponentId + ", Duration=" + challengerDurationSeconds);

        ChallengeCreateRequest request = new ChallengeCreateRequest(puzzleId, opponentId, challengerDurationSeconds);

        apiService.createChallenge(request).enqueue(new Callback<ChallengeResponse>() {
            @Override
            public void onResponse(@NonNull Call<ChallengeResponse> call, @NonNull Response<ChallengeResponse> response) {
                showChallengeLoading(false, "");
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Challenge created successfully: ID=" + response.body().getId());
                    Toast.makeText(ChallengeUserSearchActivity.this, "Challenge sent successfully!", Toast.LENGTH_LONG).show();
                    // Go back to HomeActivity, clearing ResultsActivity and this one
                    Intent homeIntent = new Intent(ChallengeUserSearchActivity.this, HomeActivity.class);
                    homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(homeIntent);
                    finish(); // Finish this activity
                } else {
                    handleApiError(response, "create challenge");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChallengeResponse> call, @NonNull Throwable t) {
                showChallengeLoading(false, "");
                Log.e(TAG, "Network error creating challenge: " + t.getMessage(), t);
                Toast.makeText(ChallengeUserSearchActivity.this, "Network error sending challenge.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        // Disable search view while loading?
        // searchView.setEnabled(!isLoading);
    }

    private void showChallengeLoading(boolean show, String message) {
        if (show) {
            if (challengeProgressDialog == null) {
                challengeProgressDialog = new ProgressDialog(this);
                challengeProgressDialog.setCancelable(false);
            }
            challengeProgressDialog.setMessage(message);
            challengeProgressDialog.show();
        } else {
            if (challengeProgressDialog != null && challengeProgressDialog.isShowing()) {
                challengeProgressDialog.dismiss();
            }
        }
    }

    // Handle Toolbar back button press
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to previous one (ResultsActivity)
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // --- Standard Error Handling ---
    private void handleApiError(Response<?> response, String contextAction) {
        String errorMsg = "Error during " + contextAction + ": ";
        try {
            errorMsg += response.code();
            if (response.errorBody() != null) {
                errorMsg += " - " + response.errorBody().string();
            } else {
                errorMsg += " - " + response.message();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing error body", e);
            errorMsg += response.message() + " (Could not parse error body)";
        }
        Log.e(TAG, "API Error: " + errorMsg);
        Toast.makeText(this, "API Error: " + response.message(), Toast.LENGTH_LONG).show(); // Show simpler message to user

        if (response.code() == 401) { // Unauthorized
            handleUnauthorizedError();
        }
    }

    private void handleUnauthorizedError() {
        sessionManager.clear();
        RetrofitClient.clearInstance();
        Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity(); // Finish this and all parent activities up to MainActivity
    }
}
