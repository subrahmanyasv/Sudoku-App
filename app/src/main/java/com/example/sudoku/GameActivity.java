// Sudoku-App/app/src/main/java/com/example/sudoku/GameActivity.java

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

    private SudokuBoardView sudokuBoardView;
    private Chronometer timerChronometer;
    private ImageView pauseButton;
    private FrameLayout pauseOverlay;
    private Button resumeButton, restartButton, quitButton;
    private TextView undoButton, eraseButton, hintButton;
    private Button submitButton;
    private TextView debugSolveButton;

    private ApiService apiService;
    private PuzzleResponse currentPuzzleData; // Store the puzzle part
    private String currentGameId; // Store the Game ID
    private String currentDifficulty;
    private int initialDurationSeconds = 0; // Store duration if resuming game
    private String initialCurrentState = null; // Store the saved board state if resuming

    private boolean isPaused = false;
    private long timeWhenStopped = 0;

    private int hintsUsed = 0;
    private static final int MAX_HINTS = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        apiService = RetrofitClient.getApiService(this);

        // Find Views
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
        debugSolveButton = findViewById(R.id.debug_solve_button);


        // --- Get Game Data ---
        Intent intent = getIntent();
        boolean isResumingGame = false;

        // Check if resuming an existing game (receives GameResponse)
        if (intent != null && intent.hasExtra("EXISTING_GAME_DATA")) {
            GameResponse existingGame = (GameResponse) intent.getSerializableExtra("EXISTING_GAME_DATA");
            if (existingGame != null && existingGame.getPuzzle() != null) {
                isResumingGame = true;
                currentPuzzleData = existingGame.getPuzzle();
                currentGameId = existingGame.getId();
                currentDifficulty = existingGame.getDifficulty(); // Use difficulty from GameResponse
                initialDurationSeconds = existingGame.getDurationSeconds();

                // *** FIX: Get the saved state (assuming GameResponse POJO has getCurrentState()) ***
                // This relies on GameResponse.java having the `currentState` field
                initialCurrentState = existingGame.getCurrentState();

                if (initialCurrentState == null) {
                    Log.w("GameActivity", "Backend did not provide current state for resuming game. Loading initial puzzle.");
                }

                Log.d("GameActivity", "Resuming Game: ID=" + currentGameId + ", Initial Duration=" + initialDurationSeconds + ", State=" + (initialCurrentState != null ? "Loaded" : "Not Provided"));

                if (currentPuzzleData.getBoardString() != null && currentPuzzleData.getSolutionString() != null) {
                    // Set the initial board first to establish starting cells
                    sudokuBoardView.setBoard(currentPuzzleData.getBoardString(), currentPuzzleData.getSolutionString());

                    // *** FIX: If we have a saved state, load it now ***
                    if (initialCurrentState != null && !initialCurrentState.equals(currentPuzzleData.getBoardString())) {
                        sudokuBoardView.loadCurrentState(initialCurrentState);
                    }
                } else {
                    handleLoadError("Board or solution string missing in existing game data.");
                    return;
                }

            } else {
                handleLoadError("Failed to retrieve existing game data object from intent.");
                return;
            }
        }
        // Check if starting a new game (receives PuzzleResponse)
        else if (intent != null && intent.hasExtra("PUZZLE_DATA")) {
            currentPuzzleData = (PuzzleResponse) intent.getSerializableExtra("PUZZLE_DATA");
            if (currentPuzzleData != null) {
                currentGameId = currentPuzzleData.getGameId();
                currentDifficulty = currentPuzzleData.getDifficulty(); // Use difficulty from PuzzleResponse
                initialDurationSeconds = 0; // New game starts at 0
                initialCurrentState = null; // No saved state for new game

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
        else {
            handleLoadError("Intent or required game/puzzle data extra is missing.");
            return;
        }


        // Setup UI Listeners
        setupNumberPad();
        setupControlButtons();
        setupPauseMenu();
        setupOnBackPressed();

        // Start Timer
        timeWhenStopped = initialDurationSeconds * 1000L;
        startTimer();
    }

    private void handleLoadError(String message) {
        Log.e("GameActivity", message);
        Toast.makeText(this, "Error loading game data.", Toast.LENGTH_SHORT).show();
        finish();
    }


    // --- Timer Management ---
    private void startTimer() {
        if (timerChronometer != null) {
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
            timeWhenStopped = SystemClock.elapsedRealtime() - timerChronometer.getBase();
            isPaused = true;
        } else if (timerChronometer == null) {
            Log.e("GameActivity", "timerChronometer is null in stopTimer()");
        }
    }


    private int getElapsedTimeSeconds() {
        if (timerChronometer == null) return (int) (timeWhenStopped / 1000);
        if (isPaused) {
            return (int) (timeWhenStopped / 1000);
        } else {
            long elapsedMillis = SystemClock.elapsedRealtime() - timerChronometer.getBase();
            return (int) (elapsedMillis / 1000);
        }
    }


    // --- UI Setup ---
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
                // 1. Check if hints are available
                if (hintsUsed >= MAX_HINTS) {
                    Toast.makeText(this, "You have used all " + MAX_HINTS + " hints!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 2. Ask the board to provide a hint
                // We will create provideHint() in SudokuBoardView
                int hintResult = sudokuBoardView.provideHint();

                // 3. Handle the result from the board
                switch (hintResult) {
                    case 1: // Success
                        hintsUsed++; // Increment the hint count
                        int hintsRemaining = MAX_HINTS - hintsUsed;
                        Toast.makeText(this, "Hint provided! You have " + hintsRemaining + " hints left.", Toast.LENGTH_SHORT).show();
                        break;
                    case 0: // No cell selected
                        Toast.makeText(this, "Please select an empty cell to use a hint.", Toast.LENGTH_SHORT).show();
                        break;
                    case -1: // Cell already filled
                        Toast.makeText(this, "Cannot use a hint on a cell that is already filled.", Toast.LENGTH_SHORT).show();
                        break;
                    case -2: // Solution not available (error case)
                        Toast.makeText(this, "Error: Could not retrieve hint.", Toast.LENGTH_SHORT).show();
                        break;
                }
            });
        }

        if (submitButton != null) {
            submitButton.setOnClickListener(v -> submitPuzzle());
        }

        if (debugSolveButton != null) {
            debugSolveButton.setOnClickListener(v -> {
                sudokuBoardView.fillSolution();
                stopTimer();
            });
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

    // --- Confirmation Dialogs ---
    private void showRestartConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Restart Game")
                .setMessage("Are you sure you want to restart this puzzle? Your progress will be lost.")
                .setPositiveButton("Restart", (dialog, which) -> {
                    if (currentPuzzleData != null && currentPuzzleData.getBoardString() != null && currentPuzzleData.getSolutionString() != null && sudokuBoardView != null && timerChronometer != null) {
                        sudokuBoardView.setBoard(currentPuzzleData.getBoardString(), currentPuzzleData.getSolutionString());
                        timeWhenStopped = 0;
                        timerChronometer.stop(); // Stop before setting base
                        timerChronometer.setBase(SystemClock.elapsedRealtime());
                        // startTimer(); // Start timer implicitly by togglePauseMenu(false)
                        togglePauseMenu(false);
                        Toast.makeText(this, "Game Restarted", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("GameActivity", "Cannot restart: Critical components missing.");
                        Toast.makeText(this, "Error restarting game.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showQuitConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Quit Game")
                .setMessage("Are you sure you want to quit? Your progress will be saved.")
                .setPositiveButton("Quit & Save", (dialog, which) -> {
                    stopTimer();
                    callUpdateGameApi(false, 0); // Mark as not completed, score 0
                    // Finish() is now called *after* API call starts in callUpdateGameApi for quitting case
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    // --- Back Press Handling ---
    private void setupOnBackPressed() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (pauseOverlay != null && pauseOverlay.getVisibility() == View.VISIBLE) {
                    togglePauseMenu(false);
                } else {
                    showQuitConfirmation();
                }
            }
        });
    }


    // --- Game Logic ---
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
            Toast.makeText(this, "Puzzle is not solved correctly or is incomplete. Keep trying!", Toast.LENGTH_LONG).show();
            startTimer(); // Resume timer if submission failed
        }
    }


    private int calculateScore(int timeSeconds, int errors, String difficulty) {
        int baseScore;
        String lowerCaseDifficulty = (difficulty != null) ? difficulty.toLowerCase() : "unknown"; // Handle null
        switch (lowerCaseDifficulty) {
            case "easy": baseScore = 1000; break;
            case "medium": baseScore = 2000; break;
            case "hard": baseScore = 3000; break;
            default: baseScore = 1500; break; // Default score for unknown
        }
        int timePenalty = Math.max(0, timeSeconds - 60); // Example penalty
        int errorPenalty = errors * 50; // Example penalty
        int finalScore = baseScore - timePenalty - errorPenalty;
        return Math.max(10, finalScore); // Minimum score
    }


    // --- API Call ---
    private void callUpdateGameApi(boolean completed, int score) {
        if (currentGameId == null || currentGameId.isEmpty()) {
            Log.e("GameActivity", "Cannot update game: Game ID is missing.");
            handleMissingGameId(completed);
            return;
        }
        if (currentDifficulty == null || currentDifficulty.isEmpty()) {
            Log.w("GameActivity", "Cannot update game: Difficulty is missing. Setting to 'unknown'.");
            currentDifficulty = "unknown"; // Assign a default if missing
        }
        if (sudokuBoardView == null) {
            Log.e("GameActivity", "Cannot update game: SudokuBoardView is null.");
            handleApiErrorCondition(completed); // Treat as an API error condition
            return;
        }


        final int finalScore = score;
        int timeSeconds = getElapsedTimeSeconds();
        int errors = sudokuBoardView.getErrorCount();
        String completedTimestamp = completed ? getTimestamp() : null;
        // *** FIX: Get current board state for saving ***
        String currentBoardState = sudokuBoardView.getBoardString();

        // Use the correct GameUpdateRequest (matching backend's GameBase for update)
        GameUpdateRequest updateRequest = new GameUpdateRequest();
        updateRequest.setId(currentGameId);
        updateRequest.setDifficulty(currentDifficulty);
        updateRequest.setWasCompleted(completed);
        updateRequest.setDurationSeconds(timeSeconds); // Save current duration
        updateRequest.setErrorsMade(errors);
        updateRequest.setHintsUsed(this.hintsUsed); // Assuming hints not implemented
        updateRequest.setFinalScore(finalScore);
        updateRequest.setCompletedAt(completedTimestamp);

        // *** FIX: Add current board state to request ***
        // This relies on GameUpdateRequest.java having the `currentState` field
        updateRequest.setCurrentState(currentBoardState);


        Log.d("GameActivity", "Updating Game - ID: " + currentGameId + ", Completed: " + completed + ", Score: " + finalScore + ", Time: " + timeSeconds + ", Errors: " + errors + ", State: " + currentBoardState);

        if (apiService == null) {
            Log.e("GameActivity", "ApiService is null, cannot update game.");
            handleApiErrorCondition(completed);
            return;
        }

        // *** FIX: Use the correct API endpoint and request object ***
        apiService.updateGame(updateRequest).enqueue(new Callback<UpdateResponse>() {
            @Override
            public void onResponse(@NonNull Call<UpdateResponse> call, @NonNull Response<UpdateResponse> response) {
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
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

        // *** FIX: Only navigate away immediately if quitting ***
        if (!completed) {
            // Go back to Home screen immediately after *starting* the save request when quitting
            Intent homeIntent = new Intent(GameActivity.this, HomeActivity.class);
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
            finish(); // Finish GameActivity after starting intent
        }
    }

    // --- Helper methods for API response handling ---
    private void handleMissingGameId(boolean completed) {
        if (!completed) {
            // If quitting and ID is missing, just go back home without saving
            Log.w("GameActivity", "Quitting game but Game ID is missing. Cannot save progress.");
            Intent homeIntent = new Intent(GameActivity.this, HomeActivity.class);
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
            finish();
        } else {
            // If submitting and ID is missing, show error and resume timer
            Toast.makeText(this, "Error: Could not save game result (Missing Game ID).", Toast.LENGTH_LONG).show();
            startTimer();
        }
    }

    private void handleSuccessfulUpdate(boolean completed, int finalScore, int timeSeconds) {
        Log.d("GameActivity", "Game update successful via API.");
        if (completed) {
            navigateToResults(finalScore, timeSeconds);
        } else {
            Log.d("GameActivity", "Quit & Save API call finished successfully. Activity should already be finishing.");
            // finish() was called earlier for the quitting case
        }
    }

    private void handleFailedUpdate(Response<UpdateResponse> response, boolean completed) {
        String defaultError = "Error saving game progress.";
        String apiMsg = null;
        if (response.body() != null && response.body().getMessage() != null) {
            apiMsg = response.body().getMessage();
        } else {
            try {
                if (response.errorBody() != null) {
                    apiMsg = response.errorBody().string(); // Try reading error body
                }
            } catch (Exception e) { Log.e("GameActivity", "Error reading error body", e); }
        }

        String errorMsg = defaultError + (apiMsg != null ? ": " + apiMsg : " Code: " + response.code());
        Log.e("GameActivity", "API Error updating game: " + response.code() + " - " + response.message() + (apiMsg != null ? " | Body/Msg: " + apiMsg : ""));
        Toast.makeText(GameActivity.this, errorMsg, Toast.LENGTH_LONG).show();

        if (response.code() == 401) {
            handleUnauthorizedError(); // Special handling for auth errors
        } else if (completed) {
            startTimer(); // If submitting failed, resume timer
        } else {
            Log.w("GameActivity", "Failed to save quit status to backend, but activity should be finishing anyway.");
            // finish() was called earlier for the quitting case
        }
    }

    private void handleNetworkFailure(Throwable t, boolean completed) {
        Log.e("GameActivity", "Network Error updating game: " + t.getMessage(), t);
        Toast.makeText(GameActivity.this, "Network Error: Could not save progress.", Toast.LENGTH_LONG).show();
        if (completed) {
            startTimer(); // If submitting failed due to network, resume timer
        } else {
            Log.w("GameActivity", "Failed to save quit status due to network error, but activity should be finishing anyway.");
            // finish() was called earlier for the quitting case
        }
    }

    private void handleApiErrorCondition(boolean completed) {
        Toast.makeText(this, "Internal error preparing game update.", Toast.LENGTH_SHORT).show();
        if (completed) {
            startTimer();
        } else {
            // If quitting fails before API call, just finish
            Intent homeIntent = new Intent(GameActivity.this, HomeActivity.class);
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
            finish();
        }
    }


    private void navigateToResults(int finalScore, int timeSeconds) {
        Intent resultsIntent = new Intent(GameActivity.this, ResultsActivity.class);
        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", timeSeconds / 60, timeSeconds % 60);
        resultsIntent.putExtra(ResultsActivity.KEY_TIME, timeFormatted);
        resultsIntent.putExtra(ResultsActivity.KEY_SCORE, finalScore);
        startActivity(resultsIntent);
        finish(); // Finish GameActivity after navigating to results
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
        finish(); // Finish current activity
    }

    // This method is linked from the XML layout (android:onClick)
    public void onBackButtonClicked(View view) {
        showQuitConfirmation(); // Use the same logic as pressing the system back button
    }
}

