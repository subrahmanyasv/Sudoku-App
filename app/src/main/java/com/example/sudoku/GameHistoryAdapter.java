// Relative Path: Sudoku-App/app/src/main/java/com/example/sudoku/GameHistoryAdapter.java
package com.example.sudoku;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat; // For colors
import androidx.recyclerview.widget.RecyclerView;

import com.example.sudoku.data.model.GameResponse; // Use GameResponse

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit; // For time formatting

public class GameHistoryAdapter extends RecyclerView.Adapter<GameHistoryAdapter.HistoryViewHolder> {

    private List<GameResponse> historyList; // Use GameResponse
    private final Context context; // Store context if needed for resources

    public GameHistoryAdapter(List<GameResponse> historyList, Context context) {
        this.historyList = historyList;
        this.context = context;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        GameResponse game = historyList.get(position);

        // Set Difficulty and Time
        String difficulty = capitalize(game.getDifficulty());
        int timeSeconds = game.getDurationSeconds();
        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d",
                TimeUnit.SECONDS.toMinutes(timeSeconds),
                timeSeconds % 60);
        holder.difficultyTimeText.setText(String.format("%s - %s", difficulty, timeFormatted));

        // Set Score
        holder.scoreText.setText(String.valueOf(game.getFinalScore()));

        // Set Date (parse and format)
        holder.dateText.setText(formatDate(game.getCompletedAt()));

        // Set color based on difficulty
        int colorResId;
        switch (game.getDifficulty().toLowerCase()) {
            case "easy":
                colorResId = R.color.difficulty_easy; // Make sure these colors exist in colors.xml
                break;
            case "medium":
                colorResId = R.color.difficulty_medium;
                break;
            case "hard":
                colorResId = R.color.difficulty_hard;
                break;
            default:
                colorResId = R.color.text_secondary; // Fallback color
                break;
        }
        holder.difficultyTimeText.setTextColor(ContextCompat.getColor(context, colorResId));
        holder.scoreText.setTextColor(ContextCompat.getColor(context, colorResId));

    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    // Method to update the data in the adapter
    public void updateData(List<GameResponse> newHistoryList) {
        this.historyList = newHistoryList;
        notifyDataSetChanged(); // Refresh the RecyclerView
    }

    // Helper to capitalize strings
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    // Helper to format the date string
    private String formatDate(String isoDateString) {
        if (isoDateString == null || isoDateString.isEmpty()) {
            return "N/A";
        }
        try {
            // Input format from backend (assuming ISO 8601 with possible microseconds)
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            // Handle potential fractional seconds if present
            if (isoDateString.contains(".")) {
                inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.US);
                // Handle timezone if present
                if (isoDateString.endsWith("Z") || isoDateString.contains("+")) {
                    // Adjust format if timezone info is included
                    if (isoDateString.endsWith("Z")) {
                        inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US);
                        inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    } else if (isoDateString.contains("+")) {
                        // More complex timezone offset parsing might be needed if format varies
                        inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX", Locale.US); // Assumes XXX format like +05:30
                    }
                }
            } else if (isoDateString.endsWith("Z") || isoDateString.contains("+")) {
                // Handle timezone for format without fractional seconds
                if (isoDateString.endsWith("Z")) {
                    inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                    inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                } else if (isoDateString.contains("+")) {
                    inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US);
                }
            }


            Date date = inputFormat.parse(isoDateString);

            // Output format (e.g., "Oct 26, 2025")
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            outputFormat.setTimeZone(TimeZone.getDefault()); // Display in local time

            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return isoDateString; // Return original string if parsing fails
        } catch (IllegalArgumentException e) {
            e.printStackTrace(); // Catch potential timezone errors
            return isoDateString;
        }
    }


    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView difficultyTimeText;
        TextView scoreText;
        TextView dateText; // Added TextView for date

        HistoryViewHolder(View itemView) {
            super(itemView);
            difficultyTimeText = itemView.findViewById(R.id.history_item_difficulty_time);
            scoreText = itemView.findViewById(R.id.history_item_score);
            dateText = itemView.findViewById(R.id.history_item_date); // Initialize date TextView
        }
    }
}

