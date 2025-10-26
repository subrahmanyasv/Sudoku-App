// Relative Path: Sudoku-App/app/src/main/java/com/example/sudoku/LeaderboardAdapter.java
package com.example.sudoku;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sudoku.data.model.LeaderboardEntryResponse; // Import the new POJO
import com.example.sudoku.utils.ProfileColorUtil; // Import ProfileColorUtil

import java.util.ArrayList; // Import ArrayList
import java.util.List;
import java.util.Locale;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder> {

    // Use the new POJO type
    private List<LeaderboardEntryResponse> entryList;
    private Context context;
    // Keep rank backgrounds
    private final int[] rankBackgrounds = {R.drawable.rank_1_background, R.drawable.rank_2_background, R.drawable.rank_3_background};


    // Constructor updated for new POJO type
    public LeaderboardAdapter(List<LeaderboardEntryResponse> entryList, Context context) {
        this.entryList = (entryList != null) ? entryList : new ArrayList<>(); // Ensure list is not null
        this.context = context;
    }

    // Method to update the data in the adapter with new POJO type
    public void updateData(List<LeaderboardEntryResponse> newEntryList) {
        this.entryList.clear();
        if (newEntryList != null) {
            this.entryList.addAll(newEntryList);
        }
        notifyDataSetChanged(); // Refreshes the list
    }

    @NonNull
    @Override
    public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_leaderboard, parent, false);
        return new LeaderboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
        LeaderboardEntryResponse entry = entryList.get(position);
        holder.rankText.setText(String.format(Locale.getDefault(), "%d.", entry.getRank()));
        holder.usernameText.setText(entry.getUsername());
        holder.scoreText.setText(String.valueOf(entry.getTotalScore())); // Use getTotalScore()

        // Set initial and background color based on username
        if (entry.getUsername() != null && !entry.getUsername().isEmpty()) {
            String initial = entry.getUsername().substring(0, 1).toUpperCase();
            holder.initialText.setText(initial);
            ProfileColorUtil.setProfileColor(holder.initialText, initial); // Use utility
        } else {
            holder.initialText.setText("?"); // Fallback
            holder.initialText.setBackgroundResource(R.drawable.profile_icon_background); // Default background
        }


        // Set special background for top 3 ranks (remains the same)
        if (entry.getRank() >= 1 && entry.getRank() <= 3) {
            // Apply rank-specific background from drawables
            int backgroundResId = rankBackgrounds[entry.getRank() - 1];
            holder.itemView.setBackgroundResource(backgroundResId);

            // Change text color for contrast on rank backgrounds (if needed, adjust colors)
            int textColor = ContextCompat.getColor(context, R.color.text_primary_inverted); // Example: Dark text
            holder.rankText.setTextColor(textColor);
            holder.usernameText.setTextColor(textColor);
            holder.scoreText.setTextColor(textColor); // Keep score text accent? Maybe adjust this.
            holder.initialText.setTextColor(textColor); // Adjust initial text color if needed

        } else {
            // Default card background for other ranks
            holder.itemView.setBackgroundResource(R.drawable.card_background);

            // Reset text colors to default for non-ranked items
            int defaultTextColor = ContextCompat.getColor(context, R.color.text_primary); // Example: Light text
            int accentColor = ContextCompat.getColor(context, R.color.text_accent); // Accent for score

            holder.rankText.setTextColor(defaultTextColor);
            holder.usernameText.setTextColor(defaultTextColor);
            holder.scoreText.setTextColor(accentColor);
            holder.initialText.setTextColor(defaultTextColor);
        }
    }


    @Override
    public int getItemCount() {
        return entryList.size();
    }

    // ViewHolder class remains largely the same
    static class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        TextView rankText, initialText, usernameText, scoreText;

        public LeaderboardViewHolder(@NonNull View itemView) {
            super(itemView);
            rankText = itemView.findViewById(R.id.rank_text);
            initialText = itemView.findViewById(R.id.initial_text);
            usernameText = itemView.findViewById(R.id.username_text);
            scoreText = itemView.findViewById(R.id.score_text);
        }
    }
}
