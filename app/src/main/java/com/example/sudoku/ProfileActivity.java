package com.example.sudoku;


import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;

public class ProfileActivity extends AppCompatActivity {

    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // 1. Initialization of Views
        tabLayout = findViewById(R.id.tab_layout);

        // Optional: Set static user data
        TextView usernameText = findViewById(R.id.username_text);
        TextView emailText = findViewById(R.id.email_text);
        // ImageView profileImage = findViewById(R.id.profile_image);

        // usernameText.setText("Gamer2025");
        // emailText.setText("gamer2025@email.com");

        // 2. Select the 'Achievements' Tab by default (Index 1)
        // Tabs are indexed 0 (Stats), 1 (Achievements), 2 (History)
        TabLayout.Tab achievementsTab = tabLayout.getTabAt(1);
        if (achievementsTab != null) {
            achievementsTab.select();
        }

        // 3. Handle Tab Selection (Important for app functionality)
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                // In a production app, you would use a ViewPager2 and Fragments here
                // to swap the content below the tabs.

                if (position == 0) {
                    Toast.makeText(ProfileActivity.this, "Showing Stats", Toast.LENGTH_SHORT).show();
                    // showStatsContent();
                } else if (position == 1) {
                    Toast.makeText(ProfileActivity.this, "Showing Achievements", Toast.LENGTH_SHORT).show();
                    // showAchievementsContent(); // Current visible view
                } else if (position == 2) {
                    Toast.makeText(ProfileActivity.this, "Showing History", Toast.LENGTH_SHORT).show();
                    // showHistoryContent();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Not strictly necessary for this UI, but useful for cleanup
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Optional: Scroll to top if the user re-taps the current tab
            }
        });

        // 4. Optional: Handle Clicks on Achievement Icons
        // You would typically link these to a data structure, but here is a simple example:
        ImageView perfectionistIcon = findViewById(R.id.ach_perfectionist_icon);
        perfectionistIcon.setOnClickListener(v -> {
            Toast.makeText(ProfileActivity.this, "Achievement: Perfectionist!", Toast.LENGTH_SHORT).show();
        });

        ImageView speedDemonIcon = findViewById(R.id.ach_speed_demon_icon);
        speedDemonIcon.setOnClickListener(v -> {
            Toast.makeText(ProfileActivity.this, "Achievement is Locked! Keep Playing.", Toast.LENGTH_SHORT).show();
        });
    }
}

