package com.example.sudoku;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder> {

    private List<LeaderboardEntry> entryList;
    private Context context;
    // We will need to create these drawable files
    private final int[] rankBackgrounds = {R.drawable.rank_1_background, R.drawable.rank_2_background, R.drawable.rank_3_background};


    public LeaderboardAdapter(List<LeaderboardEntry> entryList, Context context) {
        this.entryList = entryList;
        this.context = context;
    }

    // Method to update the data in the adapter
    public void updateData(List<LeaderboardEntry> newEntryList) {
        this.entryList.clear();
        this.entryList.addAll(newEntryList);
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
        LeaderboardEntry entry = entryList.get(position);
        holder.rankText.setText(String.format(Locale.getDefault(), "%d.", entry.getRank()));
        holder.usernameText.setText(entry.getUsername());
        holder.scoreText.setText(String.valueOf(entry.getScore()));
        holder.initialText.setText(entry.getInitial());

        // Set special background for top 3 ranks
        if (entry.getRank() >= 1 && entry.getRank() <= 3) {
            holder.itemView.setBackgroundResource(rankBackgrounds[entry.getRank() - 1]);
            // You might want to change text color for better contrast on these backgrounds
            holder.rankText.setTextColor(ContextCompat.getColor(context, R.color.background_dark));
            holder.usernameText.setTextColor(ContextCompat.getColor(context, R.color.background_dark));
            holder.scoreText.setTextColor(ContextCompat.getColor(context, R.color.background_dark));
            holder.initialText.setTextColor(ContextCompat.getColor(context, R.color.background_dark));
        } else {
            // Default transparent background for other ranks
            holder.itemView.setBackgroundResource(android.R.color.transparent);
            holder.rankText.setTextColor(ContextCompat.getColor(context, R.color.white));
            holder.usernameText.setTextColor(ContextCompat.getColor(context, R.color.white));
            holder.scoreText.setTextColor(ContextCompat.getColor(context, R.color.white));
            holder.initialText.setTextColor(ContextCompat.getColor(context, R.color.white));
        }
    }

    @Override
    public int getItemCount() {
        return entryList.size();
    }

    // ViewHolder class to hold the views for each item
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

