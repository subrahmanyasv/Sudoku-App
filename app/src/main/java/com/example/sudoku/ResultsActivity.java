// Relative Path: app/src/main/java/com/example/sudoku/ResultsActivity.java
package com.example.sudoku;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sudoku.data.local.SessionManager;
import com.example.sudoku.data.model.ChallengeCreateRequest;
import com.example.sudoku.data.model.ChallengeResponse;
import com.example.sudoku.data.network.ApiService;
import com.example.sudoku.data.network.RetrofitClient;

import java.util.UUID; // Import UUID

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResultsActivity extends AppCompatActivity {

    private TextView textTime;
    private TextView textScore;
    private Button btnChallenge;
    private Button btnBack;
    private ApiService apiService; // Add ApiService
    private ProgressDialog progressDialog; // Add ProgressDialog

    // Static keys for receiving data from GameActivity
    public static final String KEY_TIME = "PUZZLE_TIME_FORMATTED"; // Keep formatted time
    public static final String KEY_SCORE = "PUZZLE_SCORE";
    public static final String KEY_TIME_SECONDS = "PUZZLE_TIME_SECONDS"; // *** ADDED: Key for raw time in seconds ***
    public static final String KEY_GAME_ID = "GAME_ID"; // *** ADDED: Key for Game ID ***
    public static final String KEY_PUZZLE_ID = "PUZZLE_ID"; // *** ADDED: Key for Puzzle ID ***

    // Member variables to store received data
    private String puzzleId;
    private int challengerDurationSeconds;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        // Initialize Network Service
        apiService = RetrofitClient.getApiService(this);

        // Initialize ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating challenge...");
        progressDialog.setCancelable(false);

        // Initialize Views
        textTime = findViewById(R.id.text_time); // ID from activity_results.xml
        textScore = findViewById(R.id.text_score); // ID from activity_results.xml
        btnChallenge = findViewById(R.id.btn_challenge_friend); // ID from activity_results.xml
        btnBack = findViewById(R.id.btn_back_to_menu); // ID from activity_results.xml

        // Load Dynamic Data received from GameActivity
        loadResultsData();

        // Set Button Click Logic
        setupButtonListeners();
    }

    // --- Method to Load Dynamic Data ---
    private void loadResultsData() {
        Intent intent = getIntent();
        if (intent != null) {
            String timeFormatted = intent.getStringExtra(KEY_TIME);
            int score = intent.getIntExtra(KEY_SCORE, 0);
            challengerDurationSeconds = intent.getIntExtra(KEY_TIME_SECONDS, 0); // *** Store raw seconds ***
            puzzleId = intent.getStringExtra(KEY_PUZZLE_ID); // *** Store Puzzle ID ***
            // gameId is not strictly needed in this activity unless used for something else

            if (timeFormatted == null) timeFormatted = "00:00"; // Fallback value

            textTime.setText(timeFormatted);
            textScore.setText(String.valueOf(score));

            // Log received data for debugging
            Log.d("ResultsActivity", "Received Time: " + timeFormatted + ", Score: " + score + ", Seconds: " + challengerDurationSeconds + ", PuzzleID: " + puzzleId);

            // Validate that we received necessary data for challenging
            if (puzzleId == null || challengerDurationSeconds <= 0) {
                Log.e("ResultsActivity", "Missing Puzzle ID or valid Time (seconds) for challenge.");
                btnChallenge.setEnabled(false); // Disable challenge button if data is missing
                btnChallenge.setAlpha(0.5f);
                Toast.makeText(this, "Cannot challenge: Missing game data.", Toast.LENGTH_SHORT).show();
            }

        } else {
            // Fallback for missing Intent data
            textTime.setText("N/A");
            textScore.setText("0");
            challengerDurationSeconds = 0;
            puzzleId = null;
            btnChallenge.setEnabled(false); // Disable challenge button
            btnChallenge.setAlpha(0.5f);
            Log.e("ResultsActivity", "Intent data was null.");
        }
    }

    // --- Method to Setup Button Click Logic ---
    private void setupButtonListeners() {

        // ** 1. Logic for "Challenge a Friend" **
        btnChallenge.setOnClickListener(v -> {
            // Ensure necessary data is available before showing dialog
            if (puzzleId == null || challengerDurationSeconds <= 0) {
                Toast.makeText(ResultsActivity.this, "Cannot create challenge: Missing required data.", Toast.LENGTH_LONG).show();
                return;
            }
            showOpponentIdDialog();
        });

        // ** 2. Logic for "Back to Menu" **
        btnBack.setOnClickListener(v -> {
            Intent menuIntent = new Intent(ResultsActivity.this, HomeActivity.class);
            menuIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(menuIntent);
            finish();
        });
    }

    // --- Dialog to get Opponent's User ID ---
    private void showOpponentIdDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Challenge Friend");
        builder.setMessage("Enter your friend's User ID:");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT); // Allow text input for UUID
        input.setHint("User ID (UUID)");
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Send Challenge", (dialog, which) -> {
            String opponentIdStr = input.getText().toString().trim();
            // Validate UUID format before proceeding
            if (isValidUUID(opponentIdStr)) {
                callCreateChallengeApi(opponentIdStr);
            } else {
                Toast.makeText(ResultsActivity.this, "Invalid User ID format. Please enter a valid UUID.", Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    // --- Basic UUID format validation ---
    private boolean isValidUUID(String uuidString) {
        if (uuidString == null) return false;
        try {
            UUID.fromString(uuidString);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // --- API Call to Create Challenge ---
    private void callCreateChallengeApi(String opponentId) {
        showLoading(true);

        // Create the request body using the correct schema
        ChallengeCreateRequest request = new ChallengeCreateRequest(
                puzzleId,
                opponentId,
                challengerDurationSeconds
        );

        Log.d("ResultsActivity", "Sending Challenge Request: PuzzleID=" + puzzleId + ", OpponentID=" + opponentId + ", Duration=" + challengerDurationSeconds);

        apiService.createChallenge(request).enqueue(new Callback<ChallengeResponse>() {
            @Override
            public void onResponse(@NonNull Call<ChallengeResponse> call, @NonNull Response<ChallengeResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("ResultsActivity", "Challenge created successfully! ID: " + response.body().getId());
                    Toast.makeText(ResultsActivity.this, "Challenge sent successfully!", Toast.LENGTH_LONG).show();
                    // Optionally navigate to ChallengeActivity or Home
                    // navigateToChallenges(); // Example navigation
                } else {
                    handleApiError(response, "Failed to send challenge");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChallengeResponse> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e("ResultsActivity", "Network error creating challenge: " + t.getMessage(), t);
                Toast.makeText(ResultsActivity.this, "Network Error: Could not send challenge.", Toast.LENGTH_LONG).show();
            }
        });
    }

    // --- Helper to show/hide loading indicator ---
    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressDialog.show();
        } else {
            progressDialog.dismiss();
        }
    }

    // --- Navigation Helper (Optional) ---
    private void navigateToChallenges() {
        Intent intent = new Intent(this, ChallengeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); // Bring to front if already exists
        startActivity(intent);
        // Do NOT finish ResultsActivity here, allow user to go back to Home manually
    }


    // --- Generic API Error Handler ---
    private void handleApiError(Response<?> response, String defaultMessage) {
        String errorMsg = defaultMessage;
        try {
            if (response.errorBody() != null) {
                // Attempt to parse a standard error structure if your backend provides one
                // For now, just show the raw error body or code/message
                errorMsg += ": " + response.code() + " - " + response.message();
                // String errorBody = response.errorBody().string(); // Careful, can only be read once
                // Log.e("ResultsActivity", "API Error Body: " + errorBody);
            } else {
                errorMsg += ": " + response.code() + " " + response.message();
            }
        } catch (Exception e) {
            Log.e("ResultsActivity", "Error parsing error body", e);
        }
        Log.e("ResultsActivity", "API Error: " + errorMsg);
        Toast.makeText(ResultsActivity.this, errorMsg, Toast.LENGTH_LONG).show();

        if (response.code() == 401) { // Unauthorized
            handleUnauthorizedError();
        }
        // Handle other specific codes like 404 (User not found), 409 (Conflict) if needed
        else if (response.code() == 404) {
            Toast.makeText(ResultsActivity.this, "Friend's User ID not found.", Toast.LENGTH_LONG).show();
        }
        else if (response.code() == 409) {
            Toast.makeText(ResultsActivity.this, "Challenge already exists or conflicts.", Toast.LENGTH_LONG).show();
        }

    }

    // --- Unauthorized Error Handler ---
    private void handleUnauthorizedError() {
        SessionManager sessionManager = new SessionManager(this);
        sessionManager.clear();
        RetrofitClient.clearInstance();
        Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity(); // Finish this and all parent activities
    }
}
