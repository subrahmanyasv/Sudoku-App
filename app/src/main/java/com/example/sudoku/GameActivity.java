package com.example.sudoku;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

public class GameActivity extends AppCompatActivity {

    private SudokuBoardView sudokuBoardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        sudokuBoardView = findViewById(R.id.sudoku_board_view);

        // Get difficulty from Intent
        String difficulty = getIntent().getStringExtra("DIFFICULTY");
        // TODO: Use the difficulty to generate or fetch a new puzzle

        setupNumberPad();

        Button submitButton = findViewById(R.id.submit_button);
        submitButton.setOnClickListener(v -> {
            // TODO: Add logic to check if the puzzle is solved correctly
            Toast.makeText(this, "Checking puzzle...", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupNumberPad() {
        findViewById(R.id.button_1).setOnClickListener(v -> sudokuBoardView.setNumber(1));
        findViewById(R.id.button_2).setOnClickListener(v -> sudokuBoardView.setNumber(2));
        findViewById(R.id.button_3).setOnClickListener(v -> sudokuBoardView.setNumber(3));
        findViewById(R.id.button_4).setOnClickListener(v -> sudokuBoardView.setNumber(4));
        findViewById(R.id.button_5).setOnClickListener(v -> sudokuBoardView.setNumber(5));
        findViewById(R.id.button_6).setOnClickListener(v -> sudokuBoardView.setNumber(6));
        findViewById(R.id.button_7).setOnClickListener(v -> sudokuBoardView.setNumber(7));
        findViewById(R.id.button_8).setOnClickListener(v -> sudokuBoardView.setNumber(8));
        findViewById(R.id.button_9).setOnClickListener(v -> sudokuBoardView.setNumber(9));
    }
}
