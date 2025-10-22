package com.example.sudoku;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ChallengeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChallengeAdapter adapter;
    private Button incomingButton;
    private Button outgoingButton;

    // Mock data for demonstration
    private final List<Challenge> allChallenges = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge);

        recyclerView = findViewById(R.id.challenge_recycler_view);
        incomingButton = findViewById(R.id.incoming_button);
        outgoingButton = findViewById(R.id.outgoing_button);

        setupMockData();
        setupRecyclerView();
        setupTabListeners();

        // Initial load: show Incoming challenges
        showIncomingChallenges();
    }

    private void setupMockData() {
        // Incoming challenges (as seen in the image)
        allChallenges.add(new Challenge("PuzzlerPro", 14250, "P", true));
        allChallenges.add(new Challenge("SudokuMaster", 13900, "S", true));

        // Outgoing challenge (as seen in the image)
        allChallenges.add(new Challenge("Gamer2025", 15000, "G", false));
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Initialize adapter with an empty or filtered list
        adapter = new ChallengeAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);
    }

    private void setupTabListeners() {
        incomingButton.setOnClickListener(v -> showIncomingChallenges());
        outgoingButton.setOnClickListener(v -> showOutgoingChallenges());
    }

    private void showIncomingChallenges() {
        // Filter challenges where isIncoming is true
        List<Challenge> incoming = allChallenges.stream()
                .filter(Challenge::isIncoming)
                .collect(Collectors.toList());

        adapter.updateData(incoming);
        updateTabStyles(incomingButton, outgoingButton, incoming.size());
    }

    private void showOutgoingChallenges() {
        // Filter challenges where isIncoming is false
        List<Challenge> outgoing = allChallenges.stream()
                .filter(c -> !c.isIncoming())
                .collect(Collectors.toList());

        adapter.updateData(outgoing);
        updateTabStyles(outgoingButton, incomingButton, outgoing.size());
    }

    private void updateTabStyles(Button selected, Button unselected, int count) {
        // Update selected button style (Cyan background, dark text)
        selected.setBackgroundResource(R.drawable.tab_selected_background);
        selected.setTextColor(ContextCompat.getColor(this, R.color.background_dark));

        // Update unselected button style (Transparent background, white text)
        unselected.setBackgroundResource(android.R.color.transparent);
        unselected.setTextColor(ContextCompat.getColor(this, R.color.white));

        // Update the button text to show the count
        selected.setText(String.format(Locale.getDefault(), "%s (%d)",
                selected.getId() == R.id.incoming_button ? "Incoming" : "Outgoing", count));
        unselected.setText(unselected.getId() == R.id.incoming_button ? "Incoming (2)" : "Outgoing (1)"); // Use hardcoded mock count for the unselected one to match the image
    }
}