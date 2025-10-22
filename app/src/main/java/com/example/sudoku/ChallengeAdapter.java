package com.example.sudoku;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class ChallengeAdapter extends RecyclerView.Adapter<ChallengeAdapter.ChallengeViewHolder> {

    private List<Challenge> challengeList;
    private Context context;

    public ChallengeAdapter(List<Challenge> challengeList, Context context) {
        this.challengeList = challengeList;
        this.context = context;
    }

    public void updateData(List<Challenge> newChallengeList) {
        this.challengeList = newChallengeList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChallengeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_challenge, parent, false);
        return new ChallengeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChallengeViewHolder holder, int position) {
        Challenge challenge = challengeList.get(position);

        holder.initialText.setText(challenge.getInitial());

        // Format the challenger text based on challenge type
        if (challenge.isIncoming()) {
            String text = String.format(Locale.getDefault(),
                    "%s challenged you!", challenge.getChallengerName());
            holder.challengerText.setText(text);

            // Incoming challenge requires Accept/Decline buttons
            holder.buttonLayout.setVisibility(View.VISIBLE);
        } else {
            // Outgoing challenge: just show the details, no buttons needed
            String text = String.format(Locale.getDefault(),
                    "You challenged %s", challenge.getChallengerName());
            holder.challengerText.setText(text);
            holder.buttonLayout.setVisibility(View.GONE);
        }

        holder.scoreText.setText(String.format(Locale.getDefault(), "%d", challenge.getScoreToBeat()));

        // Set click listeners for the action buttons
        if (challenge.isIncoming()) {
            holder.acceptButton.setOnClickListener(v -> {
                Toast.makeText(context, "Accepted challenge from " + challenge.getChallengerName(), Toast.LENGTH_SHORT).show();
                // TODO: Launch GameActivity with the challenge puzzle data
            });

            holder.declineButton.setOnClickListener(v -> {
                Toast.makeText(context, "Declined challenge from " + challenge.getChallengerName(), Toast.LENGTH_SHORT).show();
                // TODO: Remove the challenge from the list
            });
        }
    }

    @Override
    public int getItemCount() {
        return challengeList.size();
    }

    static class ChallengeViewHolder extends RecyclerView.ViewHolder {
        TextView initialText, challengerText, scoreText;
        Button acceptButton, declineButton;
        View buttonLayout; // The LinearLayout containing the buttons

        public ChallengeViewHolder(@NonNull View itemView) {
            super(itemView);
            initialText = itemView.findViewById(R.id.initial_text);
            challengerText = itemView.findViewById(R.id.challenger_text);
            scoreText = itemView.findViewById(R.id.score_text);
            acceptButton = itemView.findViewById(R.id.accept_button);
            declineButton = itemView.findViewById(R.id.decline_button);
            buttonLayout = itemView.findViewById(R.id.button_layout);
        }
    }
}