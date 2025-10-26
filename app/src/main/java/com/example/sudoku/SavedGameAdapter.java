// Sudoku-App/app/src/main/java/com/example/sudoku/SavedGameAdapter.java
// This is a new RecyclerView Adapter file
package com.example.sudoku;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sudoku.data.model.GameResponse;

import java.util.List;
import java.util.Locale;

public class SavedGameAdapter extends RecyclerView.Adapter<SavedGameAdapter.SavedGameViewHolder> {

    private List<GameResponse> savedGames;
    private Context context;

    public SavedGameAdapter(Context context, List<GameResponse> savedGames) {
        this.context = context;
        this.savedGames = savedGames;
    }

    @NonNull
    @Override
    public SavedGameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_saved_game, parent, false);
        return new SavedGameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SavedGameViewHolder holder, int position) {
        GameResponse game = savedGames.get(position);
        if (game == null) return;

        // Populate Card Details
        String difficulty = game.getDifficulty() != null ?
                Character.toUpperCase(game.getDifficulty().charAt(0)) + game.getDifficulty().substring(1)
                : "Unknown";
        int seconds = game.getDurationSeconds();
        String timeStr = String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60);
        String details = difficulty + " Puzzle - " + timeStr;

        holder.gameDetailsText.setText(details);

        // Set the title based on position (e.g., "Continue Game", "Saved Game 2")
        if (position == 0) {
            holder.gameTitleText.setText("Continue Your Last Game");
        } else {
            holder.gameTitleText.setText("Saved Game " + (position + 1));
        }

        // Set click listener for the whole item or just the button
        View.OnClickListener continueClickListener = v -> {
            if (game.getPuzzle() != null) {
                Intent gameIntent = new Intent(context, GameActivity.class);
                gameIntent.putExtra("EXISTING_GAME_DATA", game);
                context.startActivity(gameIntent);
            } else {
                Toast.makeText(context, "Error loading game data.", Toast.LENGTH_SHORT).show();
                Log.e("SavedGameAdapter", "Puzzle data is null for game ID: " + game.getId());
            }
        };

        holder.itemView.setOnClickListener(continueClickListener);
        holder.playButton.setOnClickListener(continueClickListener);
    }

    @Override
    public int getItemCount() {
        return savedGames != null ? savedGames.size() : 0;
    }

    // ViewHolder Class
    public static class SavedGameViewHolder extends RecyclerView.ViewHolder {
        TextView gameTitleText;
        TextView gameDetailsText;
        ImageButton playButton;

        public SavedGameViewHolder(@NonNull View itemView) {
            super(itemView);
            gameTitleText = itemView.findViewById(R.id.saved_game_title_text);
            gameDetailsText = itemView.findViewById(R.id.saved_game_details_text);
            playButton = itemView.findViewById(R.id.continue_play_button);
        }
    }
}
