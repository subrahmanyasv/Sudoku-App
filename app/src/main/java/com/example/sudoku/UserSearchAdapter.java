// Relative Path: app/src/main/java/com/example/sudoku/UserSearchAdapter.java
package com.example.sudoku;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sudoku.data.model.UserBase;
import com.example.sudoku.utils.ProfileColorUtil; // Assuming you have this utility

import java.util.ArrayList;
import java.util.List;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.UserViewHolder> {

    private List<UserBase> userList;
    private final Context context;
    private final OnUserSelectedListener listener;

    public interface OnUserSelectedListener {
        void onUserSelected(UserBase user);
    }

    public UserSearchAdapter(Context context, OnUserSelectedListener listener) {
        this.context = context;
        this.userList = new ArrayList<>();
        this.listener = listener;
    }

    public void updateData(List<UserBase> newUserList) {
        this.userList.clear();
        if (newUserList != null) {
            this.userList.addAll(newUserList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_user_search, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserBase user = userList.get(position);
        holder.bind(user, listener);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView initialText;
        TextView usernameText;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            initialText = itemView.findViewById(R.id.user_search_initial_text);
            usernameText = itemView.findViewById(R.id.user_search_username_text);
        }

        void bind(final UserBase user, final OnUserSelectedListener listener) {
            usernameText.setText(user.getUsername());

            // Set initial and background color
            if (user.getUsername() != null && !user.getUsername().isEmpty()) {
                String initial = user.getUsername().substring(0, 1).toUpperCase();
                initialText.setText(initial);
                // Use ProfileColorUtil to set the background color based on the initial
                ProfileColorUtil.setProfileColor(initialText, initial);
            } else {
                initialText.setText("?");
                initialText.setBackgroundResource(R.drawable.circle_profile_background); // Default background
            }

            itemView.setOnClickListener(v -> listener.onUserSelected(user));
        }
    }
}
