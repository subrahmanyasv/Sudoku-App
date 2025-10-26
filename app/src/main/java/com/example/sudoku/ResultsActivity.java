// Relative Path: app/src/main/java/com/example/sudoku/ResultsActivity.java
package com.example.sudoku;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.UUID; // Import UUID

public class ResultsActivity extends AppCompatActivity {

    private TextView textTime;
    private TextView textScore;
    private Button btnChallenge;
    private Button btnBack;

    public static final String KEY_TIME = "PUZZLE_TIME"; // Formatted Time (String)
    public static final String KEY_SCORE = "PUZZLE_SCORE"; // Score (int)
    public static final String KEY_TIME_SECONDS = "PUZZLE_TIME_SECONDS"; // Raw Time (int)
    public static final String KEY_GAME_ID = "GAME_ID"; // Game ID (String) - Not directly used here but good practice
    public static final String KEY_PUZZLE_ID = "PUZZLE_ID"; // Puzzle ID (String)

    private ApiService apiService;
    private SessionManager sessionManager;
    private ProgressDialog progressDialog;

    // Store data needed for challenge creation
    private String puzzleId;
    private int challengerDurationSeconds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        apiService = RetrofitClient.getApiService(this);
        sessionManager = new SessionManager(this);

        textTime = findViewById(R.id.text_time);
        textScore = findViewById(R.id.text_score);
        btnChallenge = findViewById(R.id.btn_challenge_friend);
        btnBack = findViewById(R.id.btn_back_to_menu);

        loadResultsData();
        setupButtonListeners();
    }

    private void loadResultsData() {
        Intent intent = getIntent();
        if (intent != null) {
            String timeFormatted = intent.getStringExtra(KEY_TIME);
            int score = intent.getIntExtra(KEY_SCORE, 0);

            // Get data needed for challenge
            puzzleId = intent.getStringExtra(KEY_PUZZLE_ID);
            challengerDurationSeconds = intent.getIntExtra(KEY_TIME_SECONDS, -1);

            textTime.setText(timeFormatted != null ? timeFormatted : "N/A");
            textScore.setText(String.valueOf(score));

            // Disable challenge button if necessary data is missing
            if (puzzleId == null || challengerDurationSeconds == -1) {
                Log.w("ResultsActivity", "Missing puzzleId or duration for challenge creation. Disabling button.");
                btnChallenge.setEnabled(false);
                btnChallenge.setAlpha(0.5f); // Visually indicate disabled state
                // Optional: Show a toast or message
                Toast.makeText(this, "Cannot create challenge: Missing game data.", Toast.LENGTH_SHORT).show();
            } else {
                btnChallenge.setEnabled(true);
                btnChallenge.setAlpha(1.0f);
            }

        } else {
            textTime.setText("N/A");
            textScore.setText("0");
            btnChallenge.setEnabled(false); // Disable if intent is null
            btnChallenge.setAlpha(0.5f);
        }
    }

    private void setupButtonListeners() {
        btnChallenge.setOnClickListener(v -> {
            // *** CHANGE: Launch ChallengeUserSearchActivity instead of showing dialog ***
            if (puzzleId != null && challengerDurationSeconds != -1) {
                Intent searchIntent = new Intent(ResultsActivity.this, ChallengeUserSearchActivity.class);
                searchIntent.putExtra(ResultsActivity.KEY_PUZZLE_ID, puzzleId);
                searchIntent.putExtra(ResultsActivity.KEY_TIME_SECONDS, challengerDurationSeconds);
                startActivity(searchIntent);
                // We no longer finish ResultsActivity here; it stays open until a challenge is sent
                // or the user navigates back from the search screen.
            } else {
                Toast.makeText(this, "Cannot start challenge: Missing necessary data.", Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(v -> {
            Intent menuIntent = new Intent(ResultsActivity.this, HomeActivity.class);
            menuIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(menuIntent);
            finish();
        });
    }

    // --- REMOVED: showOpponentIdDialog, isValidUUID, callCreateChallengeApi ---
    // --- The logic is now moved to ChallengeUserSearchActivity ---

    // --- Keep standard error handling ---
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
            Log.e("ResultsActivity", "Error parsing error body", e);
            errorMsg += response.message() + " (Could not parse error body)";
        }
        Log.e("ResultsActivity", "API Error: " + errorMsg);
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

