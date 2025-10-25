package com.example.sudoku;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog; // Import ProgressDialog


import com.example.sudoku.data.local.SessionManager;
import com.example.sudoku.data.model.PuzzleResponse;
import com.example.sudoku.data.network.ApiService;
import com.example.sudoku.data.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DifficultyActivity extends AppCompatActivity {

    private ApiService apiService;
    private ProgressDialog progressDialog; // Add ProgressDialog


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_difficulty);

        // Initialize Retrofit Service
        apiService = RetrofitClient.getApiService(this);

        // Initialize ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Generating puzzle...");
        progressDialog.setCancelable(false); // Prevent dismissing by tapping outside


        LinearLayout easyButton = findViewById(R.id.easy_button);
        LinearLayout mediumButton = findViewById(R.id.medium_button);
        LinearLayout hardButton = findViewById(R.id.hard_button);
        TextView backButton = findViewById(R.id.back_to_menu);

        easyButton.setOnClickListener(v -> fetchAndStartGame("easy"));
        mediumButton.setOnClickListener(v -> fetchAndStartGame("medium"));
        hardButton.setOnClickListener(v -> fetchAndStartGame("hard"));

        backButton.setOnClickListener(v -> {
            // Finish this activity and go back to the previous one (HomeActivity)
            finish();
        });
    }

    private void fetchAndStartGame(String difficulty) {
        // Show loading indicator
        showLoading(true);

        Call<PuzzleResponse> call = apiService.getNewGame(difficulty);
        call.enqueue(new Callback<PuzzleResponse>() {
            @Override
            public void onResponse(Call<PuzzleResponse> call, Response<PuzzleResponse> response) {
                showLoading(false); // Hide loading indicator
                if (response.isSuccessful() && response.body() != null) {
                    PuzzleResponse puzzle = response.body();
                    Log.d("DifficultyActivity", "Puzzle fetched: ID=" + puzzle.getId() + ", GameID=" + puzzle.getGameId() + ", Board=" + puzzle.getBoardString());
                    startGame(puzzle);
                } else {
                    // Handle API error (e.g., no puzzles available, server error)
                    Log.e("DifficultyActivity", "API Error: " + response.code() + " - " + response.message());
                    Toast.makeText(DifficultyActivity.this, "Error fetching puzzle: " + response.message(), Toast.LENGTH_LONG).show();
                    if (response.code() == 401) { // Unauthorized
                        handleUnauthorizedError();
                    }
                }
            }

            @Override
            public void onFailure(Call<PuzzleResponse> call, Throwable t) {
                showLoading(false); // Hide loading indicator
                // Handle network failure
                Log.e("DifficultyActivity", "Network Error: " + t.getMessage(), t);
                Toast.makeText(DifficultyActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressDialog.show();
        } else {
            progressDialog.dismiss();
        }
    }


    private void startGame(PuzzleResponse puzzle) {
        Intent intent = new Intent(this, GameActivity.class);
        // Pass the entire PuzzleResponse object
        intent.putExtra("PUZZLE_DATA", puzzle);
        startActivity(intent);
        finish(); // Finish DifficultyActivity after starting GameActivity
    }

    private void handleUnauthorizedError() {
        SessionManager sessionManager = new SessionManager(this);
        sessionManager.clear(); // Clear token
        RetrofitClient.clearInstance(); // Clear Retrofit instance

        Toast.makeText(this, "Session expired or invalid. Please log in again.", Toast.LENGTH_LONG).show();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

