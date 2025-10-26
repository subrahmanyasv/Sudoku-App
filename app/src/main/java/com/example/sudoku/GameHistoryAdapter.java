// Relative Path: app/src/main/java/com/example/sudoku/GameHistoryAdapter.java
package com.example.sudoku;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sudoku.data.model.GameResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class GameHistoryAdapter extends RecyclerView.Adapter<GameHistoryAdapter.HistoryViewHolder> {

    private List<GameResponse> historyList;
    private final Context context;
    private final String currentUserId; // Added to determine challenge win/loss

    // Modified Constructor
    public GameHistoryAdapter(List<GameResponse> historyList, Context context, String currentUserId) {
        this.historyList = (historyList != null) ? historyList : new ArrayList<>();
        this.context = context;
        this.currentUserId = currentUserId; // Store current user ID
        if (currentUserId == null) {
            Log.w("GameHistoryAdapter", "Current User ID is null. Challenge win/loss status may be incorrect.");
        }
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        GameResponse historyItem = historyList.get(position);
        if (historyItem == null) return;

        // Set Date (Common to both)
        holder.dateText.setText(formatDate(historyItem.getCompletedAt()));

        if (historyItem.isChallenge()) {
            // --- Display Challenge Result ---
            holder.scoreText.setVisibility(View.GONE); // Hide standard score
            holder.challengeResultIcon.setVisibility(View.VISIBLE); // Show icon view

            String opponentName = "Unknown";
            boolean userWon = false;
            boolean isTie = false;
            boolean userWasChallenger = false; // Flag to know user's role

            // Determine opponent and if current user won using IDs
            if (currentUserId != null) {
                // *** MODIFIED: Use getChallengerId() and getOpponentId() ***
                if (currentUserId.equals(historyItem.getChallengerId())) { // User was Challenger
                    userWasChallenger = true;
                    opponentName = historyItem.getOpponentUsername() != null ? historyItem.getOpponentUsername() : "Opponent";
                    userWon = currentUserId.equals(historyItem.getWinnerId());
                } else if (currentUserId.equals(historyItem.getOpponentId())) { // User was Opponent
                    userWasChallenger = false;
                    opponentName = historyItem.getChallengerUsername() != null ? historyItem.getChallengerUsername() : "Challenger";
                    userWon = currentUserId.equals(historyItem.getWinnerId());
                } else {
                    Log.w("GameHistoryAdapter", "User ID " + currentUserId + " doesn't match challenger (" + historyItem.getChallengerId() + ") or opponent (" + historyItem.getOpponentId() + ")");
                }

                // Check for tie (winnerId might be null if backend doesn't set it on tie)
                isTie = historyItem.getWinnerId() == null;
                // Refined tie check based on backend logic (tie goes to challenger)
                // If winner is null OR winner is challenger AND challenger duration >= opponent duration
                Integer oppDuration = historyItem.getOpponentDuration();
                Integer chalDuration = historyItem.getChallengerDuration();
                if (!userWon && historyItem.getWinnerId() != null && historyItem.getWinnerId().equals(historyItem.getChallengerId()) && oppDuration != null && chalDuration != null && chalDuration >= oppDuration) {
                    // This scenario suggests the challenger won on a tie or outright
                    // If the user *was* the challenger, it's still a win for them. If they were opponent, it's a loss.
                    // The simple userWon check should cover this based on winnerId. Let's rely on winnerId.
                    isTie = false; // Rely solely on winnerId being null for a true tie/error state
                }
                if (historyItem.getWinnerId() == null) {
                    Log.w("GameHistoryAdapter", "Winner ID is null for completed challenge " + historyItem.getId() + ". Displaying as Tie.");
                    isTie = true; // Treat null winnerId as a tie or data issue
                }


            } else {
                Log.e("GameHistoryAdapter", "Current User ID is null, cannot determine challenge result accurately.");
                // Display as undetermined?
                opponentName = historyItem.getChallengerUsername() != null ? historyItem.getChallengerUsername() : (historyItem.getOpponentUsername() != null ? historyItem.getOpponentUsername() : "Player");
                isTie = true; // Treat as tie if user ID unknown
            }


            // Line 1: Opponent Info
            holder.line1Text.setText(String.format("vs %s (%s)", opponentName, capitalize(historyItem.getDifficulty())));
            holder.line1Text.setTextColor(ContextCompat.getColor(context, R.color.text_primary)); // Reset color

            // Line 2: Result Text & Icon
            String resultText;
            int resultColorResId;
            int resultIconResId;

            if (isTie) {
                resultText = "Result: Tie / Undetermined";
                resultColorResId = R.color.text_secondary;
                resultIconResId = R.drawable.ic_pause; // Placeholder for tie icon
            } else if (userWon) {
                resultText = "Result: You Won!";
                resultColorResId = R.color.difficulty_easy; // Green for win
                resultIconResId = R.drawable.ic_check_circle; // Checkmark for win
            } else {
                resultText = "Result: You Lost";
                resultColorResId = R.color.difficulty_hard; // Red for loss
                // Using a different icon for loss, e.g., a simple 'close' or 'X' icon
                // android.R.drawable.ic_delete might be too strong, let's assume you have an 'ic_close' or similar
                resultIconResId = android.R.drawable.ic_menu_close_clear_cancel; // Using a standard cancel icon
            }

            holder.line2Text.setText(resultText);
            holder.line2Text.setTextColor(ContextCompat.getColor(context, resultColorResId));
            holder.challengeResultIcon.setImageResource(resultIconResId);
            // Use mutate() to avoid tinting all instances of the drawable if it's reused elsewhere
            holder.challengeResultIcon.getDrawable().mutate().setTint(ContextCompat.getColor(context, resultColorResId));


        } else {
            // --- Display Standard Game Result ---
            holder.scoreText.setVisibility(View.VISIBLE); // Show standard score
            holder.challengeResultIcon.setVisibility(View.GONE); // Hide challenge icon view

            // Line 1: Difficulty
            String difficulty = capitalize(historyItem.getDifficulty());
            holder.line1Text.setText(String.format("%s Puzzle", difficulty));

            // Line 2: Time
            int timeSeconds = historyItem.getDurationSeconds();
            String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d",
                    TimeUnit.SECONDS.toMinutes(timeSeconds),
                    timeSeconds % 60);
            holder.line2Text.setText(String.format("Time: %s", timeFormatted));

            // Set colors based on difficulty
            int colorResId = getDifficultyColor(historyItem.getDifficulty());
            holder.line1Text.setTextColor(ContextCompat.getColor(context, colorResId));
            holder.line2Text.setTextColor(ContextCompat.getColor(context, colorResId));
            holder.scoreText.setText(String.valueOf(historyItem.getFinalScore()));
            holder.scoreText.setTextColor(ContextCompat.getColor(context, colorResId));
        }
    }


    @Override
    public int getItemCount() {
        return historyList.size();
    }

    // Method to update the data in the adapter
    public void updateData(List<GameResponse> newHistoryList) {
        this.historyList = (newHistoryList != null) ? newHistoryList : new ArrayList<>();
        Log.d("GameHistoryAdapter", "Updating adapter with " + this.historyList.size() + " items.");
        notifyDataSetChanged(); // Refresh the RecyclerView
    }

    // --- Helper Methods ---

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return "Unknown";
        }
        // Capitalize first letter, make rest lowercase for consistency
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private int getDifficultyColor(String difficulty) {
        if (difficulty == null) return R.color.text_secondary;
        switch (difficulty.toLowerCase()) {
            case "easy": return R.color.difficulty_easy;
            case "medium": return R.color.difficulty_medium;
            case "hard": return R.color.difficulty_hard;
            default: return R.color.text_secondary;
        }
    }

    // Improved Date Formatting (handles potential missing Z and fractional seconds more robustly)
    private String formatDate(String isoDateString) {
        if (isoDateString == null || isoDateString.isEmpty()) {
            return "Unknown Date";
        }

        // Try parsing common ISO 8601 formats, prioritizing UTC ('Z')
        String[] possibleFormats = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", // With fractional seconds and Z
                "yyyy-MM-dd'T'HH:mm:ss'Z'",         // Without fractional seconds and Z
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",    // With fractional seconds, no Z
                "yyyy-MM-dd'T'HH:mm:ss"             // Without fractional seconds, no Z
        };

        Date date = null;
        for (String format : possibleFormats) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat(format, Locale.US);
                // Assume UTC if Z is present or no timezone info is given in these formats
                if (format.endsWith("'Z'") || (!format.contains("Z") && !format.contains("+") && !format.contains("-"))) {
                    inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                }
                date = inputFormat.parse(isoDateString);
                if (date != null) {
                    break; // Successfully parsed
                }
            } catch (ParseException e) {
                // Ignore and try the next format
            }
        }

        if (date == null) {
            Log.e("GameHistoryAdapter", "Failed to parse date string: " + isoDateString);
            return isoDateString; // Return original if all formats fail
        }

        try {
            // Format for display in local timezone
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            outputFormat.setTimeZone(TimeZone.getDefault()); // Display in local time
            return outputFormat.format(date);
        } catch (Exception e) {
            Log.e("GameHistoryAdapter", "Error formatting date object for display", e);
            return "Invalid Date";
        }
    }


    // --- ViewHolder ---
    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView line1Text; // Renamed for generic use
        TextView line2Text; // Renamed for generic use
        TextView dateText;
        TextView scoreText; // For standard game score
        ImageView challengeResultIcon; // For challenge win/loss icon

        HistoryViewHolder(View itemView) {
            super(itemView);
            line1Text = itemView.findViewById(R.id.history_item_line1_text); // Updated ID
            line2Text = itemView.findViewById(R.id.history_item_line2_text); // Updated ID
            dateText = itemView.findViewById(R.id.history_item_date);
            scoreText = itemView.findViewById(R.id.history_item_score);
            challengeResultIcon = itemView.findViewById(R.id.history_item_challenge_result_icon); // Updated ID
        }
    }
}

