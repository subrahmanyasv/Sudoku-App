// Relative Path: app/src/main/java/com/example/sudoku/GameActivity.java

package com.example.sudoku;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull; // Added NonNull
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
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
import com.example.sudoku.data.model.ChallengeCompleteRequest; // Import for challenge completion
import com.example.sudoku.data.model.ChallengeResponse; // Import for challenge completion
import com.example.sudoku.data.model.GameResponse;
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

    // --- Constants ---
    public static final String KEY_IS_CHALLENGE = "IS_CHALLENGE_GAME"; // Key to identify challenge mode

    // --- Views ---
    private SudokuBoardView sudokuBoardView;
    private Chronometer timerChronometer;
    private ImageView pauseButton;
    private FrameLayout pauseOverlay;
    private Button resumeButton, restartButton, quitButton;
    private TextView undoButton, eraseButton, hintButton;
    private Button submitButton;
    private Button solveButtonDebug; // Debug button

    // --- State & Data ---
    private ApiService apiService;
    private ProgressDialog progressDialog; // Added for API calls
    private SessionManager sessionManager; // Added for auth handling

    private String currentPuzzleId;    // ID of the puzzle being played
    private String currentGameId;      // ID of the standard game record OR the challenge ID
    private String currentDifficulty;
    private String currentSolutionString; // Store the solution string locally
    private int initialDurationSeconds = 0;
    private String initialCurrentState = null;

    private boolean isPaused = false;
    private long timeWhenStopped = 0;
    private boolean isChallengeGame = false; // Flag for challenge mode
    private int hintsUsed = 0;
    private static final int MAX_HINTS = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        apiService = RetrofitClient.getApiService(this);
        sessionManager = new SessionManager(this); // Initialize SessionManager
        initProgressDialog(); // Initialize ProgressDialog

        // Find Views
        findViews(); // Encapsulated finding views

        // --- Get Game Data ---
        Intent intent = getIntent();
        boolean dataLoaded = loadGameData(intent);

        if (!dataLoaded) {
            // If data loading failed, finish activity
            handleLoadError("Failed to load necessary game data.");
            return;
        }

        // Setup UI Listeners only if data loaded successfully
        setupNumberPad();
        setupControlButtons();
        setupPauseMenu();
        setupOnBackPressed();
        setupDebugButton(); // Setup the debug solve button

        // Start Timer
        // Set base correctly whether resuming (initialDurationSeconds > 0) or starting new (initialDurationSeconds == 0)
        timerChronometer.setBase(SystemClock.elapsedRealtime() - (initialDurationSeconds * 1000L));
        startTimer(); // Start the timer explicitly
    }

    // --- Initialization Methods ---

    private void initProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Submitting...");
        progressDialog.setCancelable(false);
    }

    private void findViews() {
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
        solveButtonDebug = findViewById(R.id.solve_button_debug); // Find debug button
    }


    /**
     * Loads game data from Intent extras for new games, resumed games, or challenges.
     * Sets member variables like currentGameId, currentPuzzleId, isChallengeGame, etc.
     * Calls sudokuBoardView.setBoard() and optionally loadCurrentState().
     * @param intent The intent that started the activity.
     * @return true if data was loaded successfully, false otherwise.
     */
    private boolean loadGameData(Intent intent) {
        if (intent == null) return false;

        isChallengeGame = intent.getBooleanExtra(KEY_IS_CHALLENGE, false);

        // --- Resuming an existing standard game ---
        if (intent.hasExtra("EXISTING_GAME_DATA")) {
            GameResponse existingGame = (GameResponse) intent.getSerializableExtra("EXISTING_GAME_DATA");
            if (existingGame != null && existingGame.getPuzzle() != null) {
                currentPuzzleId = existingGame.getPuzzle().getId();
                currentGameId = existingGame.getId(); // Standard game ID
                currentDifficulty = existingGame.getDifficulty();
                initialDurationSeconds = existingGame.getDurationSeconds();
                initialCurrentState = existingGame.getCurrentState();
                currentSolutionString = existingGame.getPuzzle().getSolutionString(); // Get solution

                Log.d("GameActivity", "Resuming Game: GameID=" + currentGameId + ", PuzzleID=" + currentPuzzleId + ", Initial Duration=" + initialDurationSeconds);

                if (existingGame.getPuzzle().getBoardString() != null) {
                    sudokuBoardView.setBoard(existingGame.getPuzzle().getBoardString(), currentSolutionString);
                    if (initialCurrentState != null && !initialCurrentState.equals(existingGame.getPuzzle().getBoardString())) {
                        sudokuBoardView.loadCurrentState(initialCurrentState);
                    }
                    return true;
                } else {
                    Log.e("GameActivity", "Board string missing in existing game data.");
                    return false;
                }
            } else {
                Log.e("GameActivity", "Failed to retrieve or parse existing game data object from intent.");
                return false;
            }
        }
        // --- Starting a new game OR a challenge game ---
        else if (intent.hasExtra("PUZZLE_DATA")) {
            PuzzleResponse puzzleData = (PuzzleResponse) intent.getSerializableExtra("PUZZLE_DATA");
            if (puzzleData != null) {
                currentPuzzleId = puzzleData.getId();
                currentGameId = puzzleData.getGameId(); // This will be Game ID (new game) OR Challenge ID (challenge)
                currentDifficulty = puzzleData.getDifficulty();
                initialDurationSeconds = 0; // New game always starts at 0
                initialCurrentState = null; // No saved state
                currentSolutionString = puzzleData.getSolutionString(); // *** Get solution (will be null for old challenge impl) ***

                if (isChallengeGame) {
                    Log.d("GameActivity", "Starting Challenge Game: ChallengeID=" + currentGameId + ", PuzzleID=" + currentPuzzleId + ", Solution Provided: " + (currentSolutionString != null));
                } else {
                    Log.d("GameActivity", "Starting New Standard Game: GameID=" + currentGameId + ", PuzzleID=" + currentPuzzleId);
                }


                if (puzzleData.getBoardString() != null) {
                    // *** Pass the potentially null solution string ***
                    sudokuBoardView.setBoard(puzzleData.getBoardString(), currentSolutionString);
                    return true;
                } else {
                    Log.e("GameActivity", "Board string missing in new puzzle data.");
                    return false;
                }
            } else {
                Log.e("GameActivity", "Failed to retrieve or parse new puzzle data object from intent.");
                return false;
            }
        } else {
            Log.e("GameActivity", "Intent is missing required game/puzzle data extras.");
            return false;
        }
    }


    private void handleLoadError(String message) {
        Log.e("GameActivity", message);
        Toast.makeText(this, "Error loading game data. Returning.", Toast.LENGTH_LONG).show();
        finish(); // Exit activity if data loading fails
    }


    // --- UI Setup Methods ---

    private void setupNumberPad() {
        int[] buttonIds = {R.id.button_1, R.id.button_2, R.id.button_3, R.id.button_4, R.id.button_5, R.id.button_6, R.id.button_7, R.id.button_8, R.id.button_9};
        for (int i = 0; i < buttonIds.length; i++) {
            Button button = findViewById(buttonIds[i]);
            if (button != null) {
                final int number = i + 1;
                button.setOnClickListener(v -> {
                    if (sudokuBoardView != null) {
                        sudokuBoardView.setNumber(number);
                    }
                });
            }
        }
    }


    private void setupControlButtons() {
        if (undoButton != null) {
            undoButton.setOnClickListener(v -> Toast.makeText(this, "Undo (Not Implemented)", Toast.LENGTH_SHORT).show());
        }
        if (eraseButton != null && sudokuBoardView != null) {
            eraseButton.setOnClickListener(v -> sudokuBoardView.eraseNumber());
        }
        if (hintButton != null) {
            hintButton.setOnClickListener(v -> {
                if (hintsUsed >= MAX_HINTS) {
                    Toast.makeText(this, "You have used all " + MAX_HINTS + " hints!", Toast.LENGTH_SHORT).show();
                    return;
                }
                int hintResult = sudokuBoardView.provideHint();
                switch (hintResult) {
                    case 1: // Success
                        hintsUsed++;
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

        if (solveButtonDebug != null) {
            solveButtonDebug.setOnClickListener(v -> {
                sudokuBoardView.fillSolution();
                stopTimer();
            });
        }
    }

    private void setupDebugButton() {
        if (solveButtonDebug != null && sudokuBoardView != null) {
            solveButtonDebug.setOnClickListener(v -> {
                Log.d("GameActivity", "Debug Solve button clicked.");
                // Use the hasSolution check before attempting to fill
                if (sudokuBoardView.hasSolution()) {
                    sudokuBoardView.fillWithSolution();
                    Toast.makeText(this, "Board filled with solution!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.w("GameActivity", "Debug Solve: Solution not available.");
                    Toast.makeText(this, "Debug Solve: Solution not available for this puzzle.", Toast.LENGTH_SHORT).show();
                }
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
            // Prevent clicks passing through the overlay
            pauseOverlay.setOnClickListener(v -> {}); // Empty listener consumes clicks
        }
    }


    private void togglePauseMenu(boolean show) {
        if (pauseOverlay == null) return;
        if (show) {
            stopTimer();
            pauseOverlay.setVisibility(View.VISIBLE);
            pauseOverlay.bringToFront(); // Ensure it's on top
        } else {
            startTimer();
            pauseOverlay.setVisibility(View.GONE);
        }
    }

    // --- Confirmation Dialogs ---

    private void showRestartConfirmation() {
        if (isChallengeGame) {
            Toast.makeText(this, "Cannot restart a challenge game.", Toast.LENGTH_SHORT).show();
            return; // Don't allow restart for challenges
        }

        new AlertDialog.Builder(this)
                .setTitle("Restart Game")
                .setMessage("Are you sure you want to restart this puzzle? Your progress will be lost.")
                .setPositiveButton("Restart", (dialog, which) -> {
                    // Fetch the original board and solution strings
                    String initialBoard = null;
                    String solution = sudokuBoardView.getSolutionStringInternal(); // Use internal getter
                    // Need to get the original board string from somewhere reliable
                    // Re-loading from intent might be complex, let's assume we can get it from puzzleData if needed
                    // For simplicity, let's just use the currentPuzzleData if available

                    Intent intent = getIntent();
                    if (intent != null && intent.hasExtra("PUZZLE_DATA")) {
                        PuzzleResponse puzzleData = (PuzzleResponse) intent.getSerializableExtra("PUZZLE_DATA");
                        initialBoard = puzzleData != null ? puzzleData.getBoardString() : null;
                    } else if (intent != null && intent.hasExtra("EXISTING_GAME_DATA")) {
                        GameResponse existingGame = (GameResponse) intent.getSerializableExtra("EXISTING_GAME_DATA");
                        initialBoard = (existingGame != null && existingGame.getPuzzle() != null) ? existingGame.getPuzzle().getBoardString() : null;
                        // Solution should already be stored in currentSolutionString or retrieved via getSolutionStringInternal()
                    }


                    if (initialBoard != null && sudokuBoardView != null && timerChronometer != null) {
                        sudokuBoardView.setBoard(initialBoard, solution); // Reset board
                        timeWhenStopped = 0; // Reset timer state
                        timerChronometer.stop(); // Stop before setting base
                        timerChronometer.setBase(SystemClock.elapsedRealtime()); // Reset base to now
                        startTimer(); // Restart timer
                        togglePauseMenu(false); // Close pause menu
                        Toast.makeText(this, "Game Restarted", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("GameActivity", "Cannot restart: Critical components or initial board missing.");
                        Toast.makeText(this, "Error restarting game.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void showQuitConfirmation() {
        // Different message/options for challenges?
        String message = isChallengeGame
                ? "Quit this challenge attempt? You can accept it again later if it hasn't expired."
                : "Are you sure you want to quit? Your progress will be saved.";
        String positiveButtonText = isChallengeGame ? "Quit Challenge" : "Quit & Save";

        new AlertDialog.Builder(this)
                .setTitle(isChallengeGame ? "Quit Challenge" : "Quit Game")
                .setMessage(message)
                .setPositiveButton(positiveButtonText, (dialog, which) -> {
                    stopTimer();
                    if (!isChallengeGame) {
                        // Only save standard games
                        callUpdateGameApi(false, 0); // Mark as not completed, score 0
                        // Finish() called within callUpdateGameApi for standard game quit
                    } else {
                        // For challenges, just go back to ChallengeActivity (or Home)
                        // No API call needed to save progress for challenges in current design
                        Log.d("GameActivity", "Quitting challenge. No save API call needed.");
                        // Navigate back to Home or Challenges? Home is simpler.
                        Intent homeIntent = new Intent(GameActivity.this, HomeActivity.class);
                        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(homeIntent);
                        finish();
                    }
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
                    togglePauseMenu(false); // Close pause menu if open
                } else {
                    showQuitConfirmation(); // Show quit confirmation if pause menu is not open
                }
            }
        });
    }

    // Linked from XML layout (android:onClick) for the top-left back arrow
    public void onBackButtonClicked(View view) {
        showQuitConfirmation(); // Use the same logic as pressing the system back button
    }


    // --- Timer Management ---

    private void startTimer() {
        if (!isPaused && timerChronometer != null) {
            // Base should already be set correctly in onCreate or showRestartConfirmation
            timerChronometer.start();
        } else if (timerChronometer == null) {
            Log.e("GameActivity", "timerChronometer is null in startTimer()");
        }
    }


    private void stopTimer() {
        if (!isPaused && timerChronometer != null) {
            timerChronometer.stop();
            // Store the elapsed time correctly
            timeWhenStopped = SystemClock.elapsedRealtime() - timerChronometer.getBase();
            isPaused = true;
        } else if (timerChronometer == null) {
            Log.e("GameActivity", "timerChronometer is null in stopTimer()");
        }
    }


    private int getElapsedTimeSeconds() {
        long elapsedMillis;
        if (timerChronometer == null) {
            Log.w("GameActivity", "getElapsedTimeSeconds: timerChronometer is null, using timeWhenStopped.");
            elapsedMillis = timeWhenStopped; // Use last known stopped time
        } else if (isPaused) {
            elapsedMillis = timeWhenStopped; // Use stored time when paused
        } else {
            // Calculate current elapsed time if running
            elapsedMillis = SystemClock.elapsedRealtime() - timerChronometer.getBase();
        }
        return (int) (elapsedMillis / 1000); // Convert ms to seconds
    }


    // --- Game Logic & Submission ---

    private void submitPuzzle() {
        stopTimer();
        if (sudokuBoardView == null) {
            Log.e("GameActivity", "SudokuBoardView is null, cannot submit.");
            Toast.makeText(this, "Error submitting puzzle.", Toast.LENGTH_SHORT).show();
            startTimer(); // Resume timer if submission pre-check failed
            return;
        }

        // isSolvedCorrectly now handles the null solution case (checks isBoardFull only)
        boolean isSolvedOrFull = sudokuBoardView.isSolvedCorrectly();
        int errors = sudokuBoardView.getErrorCount(); // Will be 0 if solutionString is null
        int timeSeconds = getElapsedTimeSeconds();

        if (isSolvedOrFull) {
            if (isChallengeGame) {
                // Submit challenge result
                Log.d("GameActivity", "Challenge board is full. Submitting time: " + timeSeconds + "s");
                callCompleteChallengeApi(timeSeconds);
            } else {
                // Submit standard game result
                String difficulty = (currentDifficulty != null) ? currentDifficulty : "unknown";
                int finalScore = calculateScore(timeSeconds, errors, difficulty);
                Log.d("GameActivity", "Standard game solved! Score: " + finalScore + ", Time: " + timeSeconds + "s, Errors: " + errors);
                Toast.makeText(this, "Puzzle Correct! Score: " + finalScore, Toast.LENGTH_LONG).show();
                callUpdateGameApi(true, finalScore);
            }
        } else {
            // Board is not full or (if solution available) not correct
            String message = sudokuBoardView.hasSolution()
                    ? "Puzzle is not solved correctly or is incomplete. Keep trying!"
                    : "Puzzle is not complete. Keep trying!";
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            startTimer(); // Resume timer if submission failed
        }
    }


    private int calculateScore(int timeSeconds, int errors, String difficulty) {
        int baseScore;
        String lowerCaseDifficulty = (difficulty != null) ? difficulty.toLowerCase() : "unknown";
        switch (lowerCaseDifficulty) {
            case "easy": baseScore = 1500; break; // Increased base scores slightly
            case "medium": baseScore = 2500; break;
            case "hard": baseScore = 4000; break;
            default: baseScore = 2000; break;
        }
        // More aggressive time penalty: penalty starts sooner, increases faster
        int timePenalty = Math.max(0, (timeSeconds - 30) * 2); // Penalty starts after 30s, x2 multiplier
        int errorPenalty = errors * 100; // Increased error penalty

        Log.d("CalculateScore", "Base: " + baseScore + ", Time: " + timeSeconds + "s -> Penalty: " + timePenalty + ", Errors: " + errors + " -> Penalty: " + errorPenalty);

        int finalScore = baseScore - timePenalty - errorPenalty;
        return Math.max(50, finalScore); // Minimum score slightly higher
    }

    // --- API Calls ---

    /**
     * Updates a standard game record (not for challenges).
     * Called on successful submission or when quitting a standard game.
     */
    private void callUpdateGameApi(boolean completed, int score) {
        if (currentGameId == null || currentGameId.isEmpty() || isChallengeGame) {
            Log.e("GameActivity", "Cannot update standard game: Game ID/Challenge ID is missing or this is a challenge.");
            handleMissingGameId(completed); // Special handling if ID is missing
            return;
        }
        // Ensure difficulty is set (should be loaded in loadGameData)
        if (currentDifficulty == null || currentDifficulty.isEmpty()) {
            Log.w("GameActivity", "Cannot update game: Difficulty is missing. Setting to 'unknown'.");
            currentDifficulty = "unknown";
        }
        if (sudokuBoardView == null) {
            Log.e("GameActivity", "Cannot update game: SudokuBoardView is null.");
            handleApiErrorCondition(completed);
            return;
        }

        final int finalScore = score;
        int timeSeconds = getElapsedTimeSeconds();
        int errors = sudokuBoardView.getErrorCount();
        String completedTimestamp = completed ? getTimestamp() : null;
        String currentBoardState = sudokuBoardView.getBoardString();

        GameUpdateRequest updateRequest = new GameUpdateRequest();
        updateRequest.setId(currentGameId); // Use standard Game ID
        updateRequest.setDifficulty(currentDifficulty);
        updateRequest.setWasCompleted(completed);
        updateRequest.setDurationSeconds(timeSeconds);
        updateRequest.setErrorsMade(errors);
        updateRequest.setHintsUsed(this.hintsUsed); // Assuming hints not implemented
        updateRequest.setFinalScore(finalScore);
        updateRequest.setCompletedAt(completedTimestamp);
        updateRequest.setCurrentState(currentBoardState); // Save current state

        Log.d("GameActivity", "Updating Standard Game - ID: " + currentGameId + ", Completed: " + completed + ", Score: " + finalScore + ", Time: " + timeSeconds + ", Errors: " + errors + ", State: " + currentBoardState.substring(0, Math.min(20, currentBoardState.length())) + "..."); // Log start of state

        if (apiService == null) {
            Log.e("GameActivity", "ApiService is null, cannot update game.");
            handleApiErrorCondition(completed);
            return;
        }

        showLoading(true, completed ? "Saving Result..." : "Saving Progress...");

        apiService.updateGame(updateRequest).enqueue(new Callback<UpdateResponse>() {
            @Override
            public void onResponse(@NonNull Call<UpdateResponse> call, @NonNull Response<UpdateResponse> response) {
                showLoading(false," Update Successful.");
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().getStatus())) {
                    handleSuccessfulUpdate(completed, finalScore, timeSeconds);
                } else {
                    handleFailedUpdate(response, completed);
                }
            }
            @Override
            public void onFailure(@NonNull Call<UpdateResponse> call, @NonNull Throwable t) {
                showLoading(false," Update Failed.");
                handleNetworkFailure(t, completed);
            }
        });

        // Navigate away immediately only if quitting a standard game
        if (!completed) {
            Intent homeIntent = new Intent(GameActivity.this, HomeActivity.class);
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
            finish();
        }
    }

    /**
     * Calls the backend API to complete a challenge.
     * Called only when the opponent successfully submits a full board.
     * @param opponentTimeSeconds The time taken by the opponent.
     */
    private void callCompleteChallengeApi(int opponentTimeSeconds) {
        if (currentGameId == null || currentGameId.isEmpty() || !isChallengeGame) {
            Log.e("GameActivity", "Cannot complete challenge: Challenge ID is missing or this is not a challenge game.");
            Toast.makeText(this, "Error submitting challenge result (Invalid ID).", Toast.LENGTH_LONG).show();
            startTimer(); // Resume timer if pre-check fails
            return;
        }
        if (apiService == null) {
            Log.e("GameActivity", "ApiService is null, cannot complete challenge.");
            Toast.makeText(this, "Error submitting challenge result (Network service unavailable).", Toast.LENGTH_LONG).show();
            startTimer(); // Resume timer
            return;
        }

        showLoading(true, "Submitting Challenge...");

        ChallengeCompleteRequest request = new ChallengeCompleteRequest(opponentTimeSeconds);

        Log.d("GameActivity", "Completing Challenge - ID: " + currentGameId + ", Opponent Time: " + opponentTimeSeconds + "s");

        apiService.completeChallenge(currentGameId, request).enqueue(new Callback<ChallengeResponse>() {
            @Override
            public void onResponse(@NonNull Call<ChallengeResponse> call, @NonNull Response<ChallengeResponse> response) {
                showLoading(false," Challenge Submission Successful.");
                if (response.isSuccessful() && response.body() != null) {
                    ChallengeResponse challengeResult = response.body();
                    Log.d("GameActivity", "Challenge completion successful. Status: " + challengeResult.getStatus() + ", WinnerID: " + challengeResult.getWinnerId());
                    // Navigate to a specific Challenge Results screen or back home/challenges?
                    // For now, navigate to standard results screen, passing challenge info if needed
                    // TODO: Create a dedicated Challenge Results screen?
                    int fakeScore = calculateScore(opponentTimeSeconds, 0, currentDifficulty); // Calculate score for display
                    navigateToResults(fakeScore, opponentTimeSeconds); // Navigate to standard results
                } else {
                    // Handle failed challenge completion API call
                    handleFailedChallengeCompletion(response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChallengeResponse> call, @NonNull Throwable t) {
                showLoading(false," Challenge Submission Failed.");
                // Handle network failure during challenge completion
                handleNetworkFailureChallenge(t);
            }
        });
    }


    // --- API Response Handling Helpers ---

    private void handleMissingGameId(boolean completed) {
        // This is specific to standard games
        if (!completed) {
            Log.w("GameActivity", "Quitting standard game but Game ID is missing. Cannot save progress.");
            Intent homeIntent = new Intent(GameActivity.this, HomeActivity.class);
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
            finish();
        } else {
            Toast.makeText(this, "Error: Could not save game result (Missing Game ID).", Toast.LENGTH_LONG).show();
            startTimer(); // Resume timer if submitting standard game failed
        }
    }

    private void handleSuccessfulUpdate(boolean completed, int finalScore, int timeSeconds) {
        // This is specific to standard games
        Log.d("GameActivity", "Standard game update successful via API.");
        if (completed) {
            navigateToResults(finalScore, timeSeconds);
        } else {
            Log.d("GameActivity", "Standard Game Quit & Save API call finished successfully. Activity should already be finishing.");
            // finish() was called earlier for the quitting case
        }
    }

    private void handleFailedUpdate(Response<?> response, boolean completed) {
        // This is specific to standard games
        String defaultError = completed ? "Error saving game result." : "Error saving game progress.";
        String apiMsg = parseApiErrorMessage(response);
        String errorMsg = defaultError + (apiMsg != null ? ": " + apiMsg : " Code: " + response.code());

        Log.e("GameActivity", "API Error updating standard game: " + response.code() + " - " + response.message() + (apiMsg != null ? " | Body/Msg: " + apiMsg : ""));
        Toast.makeText(GameActivity.this, errorMsg, Toast.LENGTH_LONG).show();

        if (response.code() == 401) {
            handleUnauthorizedError(); // Special handling for auth errors
        } else if (completed) {
            startTimer(); // If submitting standard game failed, resume timer
        } else {
            Log.w("GameActivity", "Failed to save standard game quit status to backend, but activity should be finishing anyway.");
            // finish() was called earlier for the quitting case
        }
    }

    private void handleFailedChallengeCompletion(Response<?> response) {
        // Specific to challenge completion errors
        String defaultError = "Error submitting challenge result.";
        String apiMsg = parseApiErrorMessage(response);
        String errorMsg = defaultError + (apiMsg != null ? ": " + apiMsg : " Code: " + response.code());

        Log.e("GameActivity", "API Error completing challenge: " + response.code() + " - " + response.message() + (apiMsg != null ? " | Body/Msg: " + apiMsg : ""));
        Toast.makeText(GameActivity.this, errorMsg, Toast.LENGTH_LONG).show();

        if (response.code() == 401) {
            handleUnauthorizedError();
        } else {
            startTimer(); // Resume timer if challenge submission failed
        }
    }


    private void handleNetworkFailure(Throwable t, boolean completed) {
        // Specific to standard game update network errors
        Log.e("GameActivity", "Network Error updating standard game: " + t.getMessage(), t);
        Toast.makeText(GameActivity.this, "Network Error: Could not save progress.", Toast.LENGTH_LONG).show();
        if (completed) {
            startTimer(); // Resume timer if submitting standard game failed
        } else {
            Log.w("GameActivity", "Failed to save standard game quit status due to network error, but activity should be finishing anyway.");
            // finish() was called earlier for the standard game quitting case
        }
    }

    private void handleNetworkFailureChallenge(Throwable t) {
        // Specific to challenge completion network errors
        Log.e("GameActivity", "Network Error completing challenge: " + t.getMessage(), t);
        Toast.makeText(GameActivity.this, "Network Error: Could not submit challenge result.", Toast.LENGTH_LONG).show();
        startTimer(); // Resume timer if challenge submission failed
    }


    private void handleApiErrorCondition(boolean completed) {
        // Generic handler if API call couldn't even be prepared (e.g., view is null)
        Toast.makeText(this, "Internal error preparing game update.", Toast.LENGTH_SHORT).show();
        if (completed) { // If it was a submission attempt
            startTimer(); // Resume timer
        } else if (!isChallengeGame) { // If it was a quit attempt for a standard game
            // Navigate home immediately if quitting fails before API call
            Intent homeIntent = new Intent(GameActivity.this, HomeActivity.class);
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
            finish();
        }
        // If it was a quit attempt for a challenge, do nothing extra (quit confirmation handled it)
    }

    private String parseApiErrorMessage(Response<?> response) {
        // Helper to try and extract a meaningful message from error body
        try {
            if (response.errorBody() != null) {
                String errorBodyStr = response.errorBody().string();
                // Simplistic check for FastAPI's default detail message format
                if (errorBodyStr.contains("\"detail\":\"")) {
                    String detail = errorBodyStr.split("\"detail\":\"")[1].split("\"")[0];
                    return detail;
                }
                // Check for our custom AuthResponse format (less likely in error bodies but possible)
                else if (errorBodyStr.contains("\"message\":\"")) {
                    String msg = errorBodyStr.split("\"message\":\"")[1].split("\"")[0];
                    return msg;
                }
                return errorBodyStr; // Return raw body if no known pattern found
            }
        } catch (Exception e) {
            Log.e("GameActivity", "Error parsing error body", e);
        }
        return response.message(); // Fallback to HTTP status message
    }

    // --- Navigation & Utility ---

    private void navigateToResults(int finalScore, int timeSeconds) {
        Intent resultsIntent = new Intent(GameActivity.this, ResultsActivity.class);
        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", timeSeconds / 60, timeSeconds % 60);
        resultsIntent.putExtra(ResultsActivity.KEY_TIME, timeFormatted);
        resultsIntent.putExtra(ResultsActivity.KEY_SCORE, finalScore);
        resultsIntent.putExtra(ResultsActivity.KEY_TIME_SECONDS, timeSeconds); // Pass raw seconds
        resultsIntent.putExtra(ResultsActivity.KEY_GAME_ID, currentGameId); // Pass game/challenge ID
        resultsIntent.putExtra(ResultsActivity.KEY_PUZZLE_ID, currentPuzzleId); // Pass puzzle ID

        // Indicate if it was a challenge result (optional, ResultsActivity might want to know)
        // resultsIntent.putExtra("IS_CHALLENGE_RESULT", isChallengeGame);

        startActivity(resultsIntent);
        finish(); // Finish GameActivity after navigating to results
    }


    private String getTimestamp() {
        // Use ISO 8601 format with UTC timezone 'Z'
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }

    private void handleUnauthorizedError() {
        if (sessionManager != null) {
            sessionManager.clear(); // Clear token and user ID
        }
        RetrofitClient.clearInstance(); // Reset Retrofit client
        Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity(); // Finish this and all parent activities
    }

    private void showLoading(boolean show, String message) {
        if (progressDialog == null) return;
        try {
            if (show) {
                progressDialog.setMessage(message);
                if (!progressDialog.isShowing()) {
                    progressDialog.show();
                }
            } else {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        } catch (Exception e) {
            Log.e("GameActivity", "Error showing/hiding progress dialog", e);
        }
    }
}

