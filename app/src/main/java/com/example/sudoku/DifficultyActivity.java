package com.example.sudoku;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DifficultyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_difficulty);

        LinearLayout easyButton = findViewById(R.id.easy_button);
        LinearLayout mediumButton = findViewById(R.id.medium_button);
        LinearLayout hardButton = findViewById(R.id.hard_button);
        TextView backButton = findViewById(R.id.back_to_menu);

        easyButton.setOnClickListener(v -> startGame("EASY"));
        mediumButton.setOnClickListener(v -> startGame("MEDIUM"));
        hardButton.setOnClickListener(v -> startGame("HARD"));

        backButton.setOnClickListener(v -> {
            // Finish this activity and go back to the previous one (HomeActivity)
            finish();
        });
    }

    private void startGame(String difficulty) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("DIFFICULTY", difficulty);
        startActivity(intent);
    }
}

