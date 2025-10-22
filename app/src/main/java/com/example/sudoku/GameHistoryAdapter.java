package com.example.sudoku;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class GameHistoryAdapter extends RecyclerView.Adapter<GameHistoryAdapter.HistoryViewHolder> {

    private List<GameHistoryEntry> historyList;
    private Context context;

    public GameHistoryAdapter(List<GameHistoryEntry> historyList, Context context) {
        this.historyList = historyList;
        this.context = context;
    }

    // You will use this method to switch between Stats/Achievements/History data
    public void updateData(List<GameHistoryEntry> newHistoryList) {
        this.historyList = newHistoryList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        GameHistoryEntry entry = historyList.get(position);

        // 1. Set Difficulty and Time
        holder.textDifficulty.setText(String.format(Locale.getDefault(), "%s - ", entry.getDifficulty()));
        holder.textTime.setText(entry.getTime());

        // 2. Set Status and Score with appropriate colors
        if (entry.isWon()) {
            holder.textStatus.setText("Won");
            holder.textScore.setText(String.format(Locale.getDefault(), "(%d)", entry.getScore()));
            // Green color for 'Won' and Score
            holder.textStatus.setTextColor(Color.parseColor("#22C55E")); // Green from easy button
            holder.textScore.setTextColor(Color.parseColor("#22C55E"));
        } else {
            holder.textStatus.setText("Lost");
            holder.textScore.setText(""); // No score displayed for a loss
            // Red color for 'Lost'
            holder.textStatus.setTextColor(Color.parseColor("#EF4444")); // Red from hard button
            holder.textScore.setTextColor(Color.parseColor("#EF4444"));
        }

        // 3. Set Difficulty colors (matching the button colors)
        int difficultyColor = Color.WHITE;
        switch (entry.getDifficulty()) {
            case "Easy":
                difficultyColor = Color.parseColor("#22C55E");
                break;
            case "Medium":
                difficultyColor = Color.parseColor("#F97316");
                break;
            case "Hard":
                difficultyColor = Color.parseColor("#EF4444");
                break;
        }
        holder.textDifficulty.setTextColor(difficultyColor);
        holder.textTime.setTextColor(difficultyColor);
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView textDifficulty, textTime, textStatus, textScore;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textDifficulty = itemView.findViewById(R.id.text_difficulty);
            textTime = itemView.findViewById(R.id.text_time);
            textStatus = itemView.findViewById(R.id.text_status);
            textScore = itemView.findViewById(R.id.text_score);
        }
    }
}