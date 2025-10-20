package com.example.sudoku; // CHANGE THIS TO YOUR APP'S PACKAGE



import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ResultsActivity extends AppCompatActivity {

    private TextView textTime;
    private TextView textScore;
    private Button btnChallenge;
    private Button btnBack;

    // Static keys for the backend team to use when passing data
    public static final String KEY_TIME = "PUZZLE_TIME";
    public static final String KEY_SCORE = "PUZZLE_SCORE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        // 1. Initialize Views
        textTime = findViewById(R.id.text_time);
        textScore = findViewById(R.id.text_score);
        btnChallenge = findViewById(R.id.btn_challenge_friend);
        btnBack = findViewById(R.id.btn_back_to_menu);

        // 2. Load Dynamic Data
        loadResultsData();

        // 3. Set Button Click Logic
        setupButtonListeners();
    }

    // --- Method to Load Dynamic Data ---
    private void loadResultsData() {
        Intent intent = getIntent();
        if (intent != null) {

            // Get Time (String). Uses the KEY_TIME.
            String time = intent.getStringExtra(KEY_TIME);
            if (time == null) time = "00:00"; // Fallback value

            // Get Score (int). Uses the KEY_SCORE.
            int score = intent.getIntExtra(KEY_SCORE, 0);

            // Set the Dynamic Data to the TextViews
            textTime.setText(time);
            textScore.setText(String.valueOf(score));

        } else {
            // Fallback for missing Intent data
            textTime.setText("N/A");
            textScore.setText("0");
        }
    }

    // --- Method to Setup Button Click Logic ---
    private void setupButtonListeners() {

        // ** 1. Logic for "Challenge a Friend" **
        btnChallenge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get the displayed results to pass to the next activity
                String finalTime = textTime.getText().toString();
                String finalScore = textScore.getText().toString();

                // Launch the dedicated in-app friend selection screen (ChallengeSelectActivity)
                // You MUST have a ChallengeSelectActivity class defined for this to work.
                Intent challengeIntent = new Intent(ResultsActivity.this, ChallengeSelectActivity.class);
                challengeIntent.putExtra("CHALLENGE_TIME", finalTime);
                challengeIntent.putExtra("CHALLENGE_SCORE", finalScore);
                startActivity(challengeIntent);
            }
        });

        // ** 2. Logic for "Back to Menu" **
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 1. Define the specific destination Activity.
                //    *** REPLACE 'MainMenuActivity.class' with the actual class name of your desired screen. ***
                Intent menuIntent = new Intent(ResultsActivity.this, HomeActivity.class);

                // 2. Set flags to clear the activity stack. This is the robust solution
                //    to go directly to the screen you want and clear the screens in between (like the game).
                menuIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                // 3. Start the Activity
                startActivity(menuIntent);

                // 4. Finish the current (Results) Activity
                finish();
            }

        });
    }
}