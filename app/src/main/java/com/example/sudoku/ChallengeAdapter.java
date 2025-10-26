// Relative Path: app/src/main/java/com/example/sudoku/ChallengeAdapter.java
package com.example.sudoku;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sudoku.data.model.ChallengePuzzle; // Ensure this is imported
import com.example.sudoku.data.model.ChallengeRespondRequest;
import com.example.sudoku.data.model.ChallengeResponse;
import com.example.sudoku.data.model.PuzzleResponse; // Needed to start GameActivity
import com.example.sudoku.data.network.ApiService;
import com.example.sudoku.utils.ProfileColorUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit; // For formatting time

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChallengeAdapter extends RecyclerView.Adapter<ChallengeAdapter.ChallengeViewHolder> {

    private List<ChallengeResponse> challengeList;
    private final Context context;
    private final ApiService apiService;
    private final Runnable refreshCallback; // Callback to refresh list in Activity
    private final String currentUserId; // ID of the logged-in user
    private ProgressDialog progressDialog; // Loading indicator


    public ChallengeAdapter(List<ChallengeResponse> challengeList, Context context, ApiService apiService, Runnable refreshCallback, String currentUserId) {
        this.challengeList = (challengeList != null) ? challengeList : new ArrayList<>();
        this.context = context;
        this.apiService = apiService;
        this.refreshCallback = refreshCallback;
        this.currentUserId = currentUserId; // Store current user ID
        initProgressDialog(); // Initialize ProgressDialog
    }

    private void initProgressDialog() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Processing...");
        progressDialog.setCancelable(false);
    }

    public void updateData(List<ChallengeResponse> newChallengeList) {
        this.challengeList = (newChallengeList != null) ? newChallengeList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChallengeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_challenge, parent, false);
        return new ChallengeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChallengeViewHolder holder, int position) {
        ChallengeResponse challenge = challengeList.get(position);
        if (challenge == null) return;

        // Determine if the current user is the opponent (incoming challenge)
        boolean isIncoming = currentUserId != null && currentUserId.equals(challenge.getOpponentId());

        // Use challenger or opponent details based on direction
        String displayName;
        String displayInitial;
        if (isIncoming) {
            displayName = (challenge.getChallenger() != null) ? challenge.getChallenger().getUsername() : "Unknown";
            displayInitial = (displayName != null && !displayName.isEmpty()) ? displayName.substring(0, 1).toUpperCase() : "?";
            holder.challengerText.setText(String.format(Locale.getDefault(), "%s challenged you!", displayName));
        } else {
            displayName = (challenge.getOpponent() != null) ? challenge.getOpponent().getUsername() : "Unknown";
            displayInitial = (displayName != null && !displayName.isEmpty()) ? displayName.substring(0, 1).toUpperCase() : "?";
            holder.challengerText.setText(String.format(Locale.getDefault(), "You challenged %s", displayName));
        }

        holder.initialText.setText(displayInitial);
        ProfileColorUtil.setProfileColor(holder.initialText, displayInitial); // Apply dynamic color

        // Display challenger's time as the time/score to beat
        int timeSeconds = challenge.getChallengerDuration();
        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d",
                TimeUnit.SECONDS.toMinutes(timeSeconds),
                timeSeconds % 60);
        holder.scoreText.setText(timeFormatted);
        holder.scoreToBeatText.setText("Time to beat:"); // Update label to be consistent

        // Show/Hide buttons and status based on direction and status
        if (isIncoming && "pending".equalsIgnoreCase(challenge.getStatus())) {
            holder.buttonLayout.setVisibility(View.VISIBLE);
            holder.statusText.setVisibility(View.GONE);

            holder.acceptButton.setOnClickListener(v -> handleChallengeResponse(challenge, "accept"));
            holder.declineButton.setOnClickListener(v -> handleChallengeResponse(challenge, "reject"));
        } else {
            // Hide buttons for outgoing or non-pending incoming challenges
            holder.buttonLayout.setVisibility(View.GONE);
            // Show status for outgoing challenges or completed/rejected incoming ones
            holder.statusText.setVisibility(View.VISIBLE);
            String statusDisplay = (challenge.getStatus() != null) ? challenge.getStatus().toUpperCase() : "UNKNOWN";
            holder.statusText.setText(statusDisplay);
            // Optionally change status text color based on status
            switch (statusDisplay) {
                case "ACCEPTED":
                    holder.statusText.setTextColor(ContextCompat.getColor(context, R.color.difficulty_easy)); // Green
                    break;
                case "REJECTED":
                case "EXPIRED":
                    holder.statusText.setTextColor(ContextCompat.getColor(context, R.color.difficulty_hard)); // Red
                    break;
                case "COMPLETED":
                    holder.statusText.setTextColor(ContextCompat.getColor(context, R.color.button_primary)); // Cyan/Accent
                    break;
                case "PENDING":
                default:
                    holder.statusText.setTextColor(ContextCompat.getColor(context, R.color.text_secondary)); // Grey
                    break;
            }
        }
    }


    @Override
    public int getItemCount() {
        return challengeList.size();
    }

    private void handleChallengeResponse(ChallengeResponse challenge, String action) {
        if (apiService == null) {
            Toast.makeText(context, "Error: Network service not available.", Toast.LENGTH_SHORT).show();
            return;
        }
        showLoading(true, "accept".equals(action) ? "Accepting..." : "Rejecting...");

        ChallengeRespondRequest request = new ChallengeRespondRequest(action);
        apiService.respondToChallenge(challenge.getId(), request).enqueue(new Callback<ChallengeResponse>() {
            @Override
            public void onResponse(@NonNull Call<ChallengeResponse> call, @NonNull Response<ChallengeResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    ChallengeResponse updatedChallenge = response.body();
                    if ("accept".equals(action)) {
                        Log.d("ChallengeAdapter", "Challenge accept successful for ID: " + challenge.getId());
                        Toast.makeText(context, "Challenge Accepted!", Toast.LENGTH_SHORT).show();
                        startGameFromChallenge(updatedChallenge); // Start game with UPDATED challenge data
                    } else { // reject
                        Log.d("ChallengeAdapter", "Challenge reject successful for ID: " + challenge.getId());
                        Toast.makeText(context, "Challenge Rejected", Toast.LENGTH_SHORT).show();
                        if (refreshCallback != null) {
                            refreshCallback.run(); // Refresh the list in the activity
                        }
                    }
                } else {
                    Log.e("ChallengeAdapter", "API Error responding to challenge: " + response.code() + " - " + response.message());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e("ChallengeAdapter", "Error Body: " + errorBody);
                        Toast.makeText(context, "Failed to " + action + " challenge: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(context, "Failed to " + action + " challenge: " + response.message(), Toast.LENGTH_LONG).show();
                    }

                }
            }

            @Override
            public void onFailure(@NonNull Call<ChallengeResponse> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e("ChallengeAdapter", "Network Error responding to challenge: " + t.getMessage(), t);
                Toast.makeText(context, "Network Error: Could not " + action + " challenge.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startGameFromChallenge(ChallengeResponse challenge) {
        if (challenge.getPuzzle() == null) {
            Log.e("ChallengeAdapter", "Cannot start game, puzzle data is missing in ChallengeResponse for ID: " + challenge.getId());
            Toast.makeText(context, "Error: Missing puzzle data for this challenge.", Toast.LENGTH_SHORT).show();
            return;
        }

        ChallengePuzzle challengePuzzle = challenge.getPuzzle();

        // *** MODIFIED: Create PuzzleResponse including the solution string ***
        PuzzleResponse puzzleForGame = new PuzzleResponse();
        puzzleForGame.setId(challengePuzzle.getId());
        puzzleForGame.setDifficulty(challengePuzzle.getDifficulty());
        puzzleForGame.setBoardString(challengePuzzle.getBoardString());
        puzzleForGame.setSolutionString(challengePuzzle.getSolutionString()); // Pass the solution
        // *** MODIFIED: Include challenge ID as the "gameId" for context in GameActivity ***
        // GameActivity needs modification to handle this as a 'challenge game ID'
        puzzleForGame.setGameId(challenge.getId()); // Pass challenge ID

        Log.d("ChallengeAdapter", "Starting game for accepted challenge ID: " + challenge.getId() + " with Puzzle ID: " + challengePuzzle.getId() + " | Solution included: " + (challengePuzzle.getSolutionString() != null));

        Intent gameIntent = new Intent(context, GameActivity.class);
        gameIntent.putExtra("PUZZLE_DATA", puzzleForGame);
        // Add a flag to indicate this is a challenge game
        gameIntent.putExtra(GameActivity.KEY_IS_CHALLENGE, true); // Need to define this key in GameActivity
        context.startActivity(gameIntent);

        // Optionally refresh the list in ChallengeActivity immediately
        // Or wait for onResume to trigger the refresh
        // if (refreshCallback != null) {
        //     refreshCallback.run();
        // }
    }


    private void showLoading(boolean show, String message) {
        if (progressDialog == null) return;
        if (show) {
            progressDialog.setMessage(message);
            progressDialog.show();
        } else {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }
    private void showLoading(boolean show) {
        showLoading(show, "Processing..."); // Default message
    }


    static class ChallengeViewHolder extends RecyclerView.ViewHolder {
        TextView initialText, challengerText, scoreText, scoreToBeatText, statusText; // Added statusText
        Button acceptButton, declineButton;
        View buttonLayout; // The LinearLayout containing the buttons

        public ChallengeViewHolder(@NonNull View itemView) {
            super(itemView);
            initialText = itemView.findViewById(R.id.initial_text);
            challengerText = itemView.findViewById(R.id.challenger_text);
            scoreText = itemView.findViewById(R.id.score_text);
            scoreToBeatText = itemView.findViewById(R.id.score_to_beat_text); // Make sure this ID exists
            statusText = itemView.findViewById(R.id.status_text); // Make sure this ID exists
            acceptButton = itemView.findViewById(R.id.accept_button);
            declineButton = itemView.findViewById(R.id.decline_button);
            buttonLayout = itemView.findViewById(R.id.button_layout);
        }
    }
}

