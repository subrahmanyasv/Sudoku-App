package com.example.sudoku;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull; // Added NonNull
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sudoku.data.local.SessionManager;
import com.example.sudoku.data.model.GameResponse; // Import GameResponse
import com.example.sudoku.data.model.GameUpdateRequest;
import com.example.sudoku.data.model.PuzzleResponse;
import com.example.sudoku.data.model.UpdateResponse;
import com.example.sudoku.data.network.ApiService;
import com.example.sudoku.data.network.RetrofitClient;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GameActivity extends AppCompatActivity {

    // ... (Keep existing member variables)
    private SudokuBoardView sudokuBoardView;
    private Chronometer timerChronometer;
    private ImageView pauseButton;
    private FrameLayout pauseOverlay;
    private Button resumeButton, restartButton, quitButton;
    private TextView undoButton, eraseButton, hintButton;
    private Button submitButton;

    private ApiService apiService;
    private PuzzleResponse currentPuzzleData; // Store the puzzle part
    private String currentGameId; // Store the Game ID
    private String currentDifficulty;
    private int initialDurationSeconds = 0; // Store duration if resuming game

    private boolean isPaused = false;
    private long timeWhenStopped = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        apiService = RetrofitClient.getApiService(this);

        // Find Views (Keep existing findViewById calls)
        sudokuBoardView = findViewById(R.id.sudoku_board_view);
        timerChronometer = findViewById(R.id.timer_chronometer);
        pauseButton = findViewById(R.id.pause_button);
        pauseOverlay = findViewById(R.id.pause_overlay);
        resumeButton = findViewById(R.id.resume_button);
        restartButton = findViewById(R.id.restart_button);
        quitButton = findViewById(R.id.quit_button);
        undoButton = findViewById(R.id.undo_button);
        eraseButton = findViewById(R.id.erase_button);
        hintButton = findViewById(R.id.hint_button);
        submitButton = findViewById(R.id.submit_button);


        // --- Get Game Data ---
        Intent intent = getIntent();
        boolean isResumingGame = false; // Flag to indicate if we're loading an existing game

        // Check if resuming an existing game
        if (intent != null && intent.hasExtra("EXISTING_GAME_DATA")) {
            GameResponse existingGame = (GameResponse) intent.getSerializableExtra("EXISTING_GAME_DATA");
            if (existingGame != null && existingGame.getPuzzle() != null) {
                isResumingGame = true;
                currentPuzzleData = existingGame.getPuzzle(); // Extract puzzle data
                currentGameId = existingGame.getId();         // Get Game ID from GameResponse
                currentDifficulty = existingGame.getDifficulty();
                initialDurationSeconds = existingGame.getDurationSeconds(); // Store initial duration

                Log.d("GameActivity", "Resuming Game: ID=" + currentGameId + ", Initial Duration=" + initialDurationSeconds);

                // Load the board state (Need to add this method to SudokuBoardView if not present)
                // Assuming the puzzle object contains the *initial* board string.
                // If the backend needs to provide the *current* state of an unfinished game,
                // the API and SudokuBoardView will need adjustments. For now, we load the initial state.
                if (currentPuzzleData.getBoardString() != null && currentPuzzleData.getSolutionString() != null) {
                    sudokuBoardView.setBoard(currentPuzzleData.getBoardString(), currentPuzzleData.getSolutionString());
                    // TODO: Ideally, backend should send the *last saved state* of the board,
                    // and we'd need a `sudokuBoardView.loadStateFromString(savedStateString)` method.
                    // For now, we only load the initial puzzle.
                } else {
                    handleLoadError("Board or solution string missing in existing game data.");
                    return;
                }

            } else {
                handleLoadError("Failed to retrieve existing game data object from intent.");
                return;
            }
        }
        // Check if starting a new game (from DifficultyActivity)
        else if (intent != null && intent.hasExtra("PUZZLE_DATA")) {
            currentPuzzleData = (PuzzleResponse) intent.getSerializableExtra("PUZZLE_DATA");
            if (currentPuzzleData != null) {
                currentGameId = currentPuzzleData.getGameId(); // Get Game ID from PuzzleResponse
                currentDifficulty = currentPuzzleData.getDifficulty();
                initialDurationSeconds = 0; // New game starts at 0

                Log.d("GameActivity", "Starting New Game: ID=" + currentGameId);

                if (currentPuzzleData.getBoardString() != null && currentPuzzleData.getSolutionString() != null) {
                    sudokuBoardView.setBoard(currentPuzzleData.getBoardString(), currentPuzzleData.getSolutionString());
                } else {
                    handleLoadError("Board or solution string missing in new puzzle data.");
                    return;
                }
            } else {
                handleLoadError("Failed to retrieve new puzzle data object from intent.");
                return;
            }
        }
        // No valid data found
        else {
            handleLoadError("Intent or required game/puzzle data extra is missing.");
            return;
        }


        // Setup UI Listeners (Keep existing setup calls)
        setupNumberPad();
        setupControlButtons();
        setupPauseMenu();
        setupOnBackPressed();

        // Start Timer (adjusting for resumed game)
        timeWhenStopped = initialDurationSeconds * 1000L; // Convert saved seconds to millis
        startTimer();
    }

    // Helper for handling data loading errors
    private void handleLoadError(String message) {
        Log.e("GameActivity", message);
        Toast.makeText(this, "Error loading game data.", Toast.LENGTH_SHORT).show();
        finish();
    }


    // --- Timer Management ---
    private void startTimer() {
        if (timerChronometer != null) {
            // Set base considering previously stopped time
            timerChronometer.setBase(SystemClock.elapsedRealtime() - timeWhenStopped);
            timerChronometer.start();
            isPaused = false;
        } else {
            Log.e("GameActivity", "timerChronometer is null in startTimer()");
        }
    }

    private void stopTimer() {
        if (!isPaused && timerChronometer != null) {
            timerChronometer.stop();
            // Store the total elapsed time correctly
            timeWhenStopped = SystemClock.elapsedRealtime() - timerChronometer.getBase();
            isPaused = true;
        } else if (timerChronometer == null) {
            Log.e("GameActivity", "timerChronometer is null in stopTimer()");
        }
    }


    private int getElapsedTimeSeconds() {
        // Correctly return the stored time if paused, or calculate current if running
        if (timerChronometer == null) return (int) (timeWhenStopped / 1000);

        if (isPaused) {
            return (int) (timeWhenStopped / 1000);
        } else {
            // Calculate current elapsed time based on the chronometer's base
            long elapsedMillis = SystemClock.elapsedRealtime() - timerChronometer.getBase();
            return (int) (elapsedMillis / 1000);
        }
    }


    // --- UI Setup (Keep existing methods) ---
    private void setupNumberPad() {
        if (findViewById(R.id.button_1) != null) findViewById(R.id.button_1).setOnClickListener(v -> sudokuBoardView.setNumber(1));
        if (findViewById(R.id.button_2) != null) findViewById(R.id.button_2).setOnClickListener(v -> sudokuBoardView.setNumber(2));
        if (findViewById(R.id.button_3) != null) findViewById(R.id.button_3).setOnClickListener(v -> sudokuBoardView.setNumber(3));
        if (findViewById(R.id.button_4) != null) findViewById(R.id.button_4).setOnClickListener(v -> sudokuBoardView.setNumber(4));
        if (findViewById(R.id.button_5) != null) findViewById(R.id.button_5).setOnClickListener(v -> sudokuBoardView.setNumber(5));
        if (findViewById(R.id.button_6) != null) findViewById(R.id.button_6).setOnClickListener(v -> sudokuBoardView.setNumber(6));
        if (findViewById(R.id.button_7) != null) findViewById(R.id.button_7).setOnClickListener(v -> sudokuBoardView.setNumber(7));
        if (findViewById(R.id.button_8) != null) findViewById(R.id.button_8).setOnClickListener(v -> sudokuBoardView.setNumber(8));
        if (findViewById(R.id.button_9) != null) findViewById(R.id.button_9).setOnClickListener(v -> sudokuBoardView.setNumber(9));
    }


    private void setupControlButtons() {
        if (undoButton != null) {
            undoButton.setOnClickListener(v -> {
                Toast.makeText(this, "Undo clicked (Not Implemented)", Toast.LENGTH_SHORT).show();
            });
        }
        if (eraseButton != null && sudokuBoardView != null) {
            eraseButton.setOnClickListener(v -> sudokuBoardView.eraseNumber());
        }
        if (hintButton != null) {
            hintButton.setOnClickListener(v -> {
                Toast.makeText(this, "Hint clicked (Not Implemented)", Toast.LENGTH_SHORT).show();
            });
        }
        if (submitButton != null) {
            submitButton.setOnClickListener(v -> submitPuzzle());
        }
    }


    private void setupPauseMenu() {
        if (pauseButton != null) pauseButton.setOnClickListener(v -> togglePauseMenu(true));
        if (resumeButton != null) resumeButton.setOnClickListener(v -> togglePauseMenu(false));
        if (restartButton != null) restartButton.setOnClickListener(v -> showRestartConfirmation());
        if (quitButton != null) quitButton.setOnClickListener(v -> showQuitConfirmation());
        if (pauseOverlay != null) {
            pauseOverlay.setVisibility(View.GONE);
        }
    }


    private void togglePauseMenu(boolean show) {
        if (pauseOverlay == null) return;
        if (show) {
            stopTimer();
            pauseOverlay.setVisibility(View.VISIBLE);
            pauseOverlay.setClickable(true);
        } else {
            startTimer();
            pauseOverlay.setVisibility(View.GONE);
        }
    }

    // --- Confirmation Dialogs (Keep existing methods) ---
    private void showRestartConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Restart Game")
                .setMessage("Are you sure you want to restart this puzzle? Your progress will be lost.")
                .setPositiveButton("Restart", (dialog, which) -> {
                    if (currentPuzzleData != null && currentPuzzleData.getBoardString() != null && currentPuzzleData.getSolutionString() != null && sudokuBoardView != null && timerChronometer != null) {
                        sudokuBoardView.setBoard(currentPuzzleData.getBoardString(), currentPuzzleData.getSolutionString());
                        timeWhenStopped = 0;
                        timerChronometer.stop();
                        timerChronometer.setBase(SystemClock.elapsedRealtime());
                        startTimer();
                        togglePauseMenu(false);
                        Toast.makeText(this, "Game Restarted", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("GameActivity", "Cannot restart: puzzle data, board view, or timer is missing.");
                        Toast.makeText(this, "Error restarting game.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showQuitConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Quit Game")
                .setMessage("Are you sure you want to quit? Your progress will be saved.") // Updated message
                .setPositiveButton("Quit & Save", (dialog, which) -> {
                    stopTimer();
                    // Update game state on backend as incomplete, SAVE current progress
                    callUpdateGameApi(false, 0); // Mark as not completed, score 0 for now
                    // Finish() is now called *after* API call starts in callUpdateGameApi for quitting
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    // --- Back Press Handling (Keep existing method) ---
    private void setupOnBackPressed() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (pauseOverlay != null && pauseOverlay.getVisibility() == View.VISIBLE) {
                    togglePauseMenu(false); // If paused, just resume
                } else {
                    showQuitConfirmation(); // Otherwise, show quit dialog
                }
            }
        });
    }


    // --- Game Logic (Keep existing methods, ensure null checks) ---
    private void submitPuzzle() {
        stopTimer();
        if (sudokuBoardView == null) {
            Log.e("GameActivity", "SudokuBoardView is null, cannot submit.");
            Toast.makeText(this, "Error submitting puzzle.", Toast.LENGTH_SHORT).show();
            startTimer();
            return;
        }
        boolean isSolvedCorrectly = sudokuBoardView.isSolvedCorrectly();
        int errors = sudokuBoardView.getErrorCount();
        int timeSeconds = getElapsedTimeSeconds();

        if (isSolvedCorrectly) {
            String difficulty = (currentDifficulty != null) ? currentDifficulty : "unknown";
            int finalScore = calculateScore(timeSeconds, errors, difficulty);
            Toast.makeText(this, "Puzzle Correct! Score: " + finalScore, Toast.LENGTH_LONG).show();
            callUpdateGameApi(true, finalScore);
        } else {
            Toast.makeText(this, "Puzzle is not solved correctly. Keep trying!", Toast.LENGTH_LONG).show();
            startTimer();
        }
    }


    private int calculateScore(int timeSeconds, int errors, String difficulty) {
        int baseScore;
        String lowerCaseDifficulty = (difficulty != null) ? difficulty.toLowerCase() : "hard";
        switch (lowerCaseDifficulty) {
            case "easy": baseScore = 1000; break;
            case "medium": baseScore = 2000; break;
            case "hard": default: baseScore = 3000; break;
        }
        int timePenalty = Math.max(0, timeSeconds - 60);
        int errorPenalty = errors * 50;
        int finalScore = baseScore - timePenalty - errorPenalty;
        return Math.max(10, finalScore);
    }


    // --- API Call (Keep existing method and helpers) ---
    private void callUpdateGameApi(boolean completed, int score) {
        if (currentGameId == null || currentGameId.isEmpty()) {
            Log.e("GameActivity", "Cannot update game: Game ID is missing.");
            handleMissingGameId(completed);
            return;
        }
        if (currentDifficulty == null || currentDifficulty.isEmpty()) {
            Log.e("GameActivity", "Cannot update game: Difficulty is missing.");
            currentDifficulty = "unknown";
            if (completed) {
                Toast.makeText(this, "Error: Could not save game result (Missing Difficulty).", Toast.LENGTH_LONG).show();
                startTimer();
                return;
            }
        }

        final int finalScore = score;
        int timeSeconds = getElapsedTimeSeconds();
        int errors = (sudokuBoardView != null) ? sudokuBoardView.getErrorCount() : 0;
        String completedTimestamp = getTimestamp();

        GameUpdateRequest updateRequest = new GameUpdateRequest();
        updateRequest.setId(currentGameId);
        updateRequest.setDifficulty(currentDifficulty);
        updateRequest.setWasCompleted(completed);
        updateRequest.setDurationSeconds(timeSeconds); // Save current duration
        updateRequest.setErrorsMade(errors);
        updateRequest.setHintsUsed(0);
        updateRequest.setFinalScore(finalScore);
        updateRequest.setCompletedAt(completed ? completedTimestamp : null); // Only set if completed

        // TODO: Add current board state to updateRequest if backend supports saving it
        // updateRequest.setCurrentBoardState(sudokuBoardView.getBoardString());


        Log.d("GameActivity", "Updating Game - ID: " + currentGameId + ", Completed: " + completed + ", Score: " + finalScore + ", Time: " + timeSeconds + ", Errors: " + errors);

        if (apiService == null) {
            Log.e("GameActivity", "ApiService is null, cannot update game.");
            handleApiErrorCondition(completed);
            return;
        }

        apiService.updateGame(updateRequest).enqueue(new Callback<UpdateResponse>() {
            @Override
            public void onResponse(@NonNull Call<UpdateResponse> call, @NonNull Response<UpdateResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleSuccessfulUpdate(completed, finalScore, timeSeconds);
                } else {
                    handleFailedUpdate(response, completed);
                }
            }
            @Override
            public void onFailure(@NonNull Call<UpdateResponse> call, @NonNull Throwable t) {
                handleNetworkFailure(t, completed);
            }
        });

        if (!completed) {
            // Go back to Home screen immediately after starting the save request when quitting
            Intent homeIntent = new Intent(GameActivity.this, HomeActivity.class);
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
            finish(); // Finish GameActivity after starting intent
        }
    }

    // --- Helper methods for API response handling (Keep existing methods) ---
    private void handleMissingGameId(boolean completed) {
        if (!completed) {
            finish();
        } else {
            Toast.makeText(this, "Error: Could not save game result (Missing ID).", Toast.LENGTH_LONG).show();
            startTimer();
        }
    }

    private void handleSuccessfulUpdate(boolean completed, int finalScore, int timeSeconds) {
        Log.d("GameActivity", "Game update successful.");
        if (completed) {
            navigateToResults(finalScore, timeSeconds);
        } else {
            Log.d("GameActivity", "Quit & Save API call finished successfully.");
            // finish() was already called when quitting
        }
    }

    private void handleFailedUpdate(Response<UpdateResponse> response, boolean completed) {
        String errorMsg = "API Error updating game: " + response.code() + " - " + response.message();
        try {
            if (response.errorBody() != null) { errorMsg += " | Body: " + response.errorBody().string(); }
        } catch (Exception e) { Log.e("GameActivity", "Error reading error body", e); }
        Log.e("GameActivity", errorMsg);
        Toast.makeText(GameActivity.this, "Error saving game progress: " + response.message(), Toast.LENGTH_LONG).show();

        if (response.code() == 401) {
            handleUnauthorizedError();
        } else if (completed) {
            startTimer();
        } else {
            Log.w("GameActivity", "Failed to save quit status to backend, but finishing anyway.");
            // finish() was already called when quitting
        }
    }

    private void handleNetworkFailure(Throwable t, boolean completed) {
        Log.e("GameActivity", "Network Error updating game: " + t.getMessage(), t);
        Toast.makeText(GameActivity.this, "Network Error: Could not save progress.", Toast.LENGTH_LONG).show();
        if (completed) {
            startTimer();
        } else {
            Log.w("GameActivity", "Failed to save quit status due to network error, but finishing anyway.");
            // finish() was already called when quitting
        }
    }

    private void handleApiErrorCondition(boolean completed) {
        Toast.makeText(this, "Error preparing game update.", Toast.LENGTH_SHORT).show();
        if (completed) {
            startTimer();
        } else {
            finish();
        }
    }


    private void navigateToResults(int finalScore, int timeSeconds) {
        Intent resultsIntent = new Intent(GameActivity.this, ResultsActivity.class);
        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", timeSeconds / 60, timeSeconds % 60);
        resultsIntent.putExtra(ResultsActivity.KEY_TIME, timeFormatted);
        resultsIntent.putExtra(ResultsActivity.KEY_SCORE, finalScore);
        startActivity(resultsIntent);
        finish();
    }


    private String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }

    private void handleUnauthorizedError() {
        SessionManager sessionManager = new SessionManager(this);
        sessionManager.clear();
        RetrofitClient.clearInstance();
        Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void onBackButtonClicked(View view) {
        showQuitConfirmation();
    }
}

