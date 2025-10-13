package com.example.sudoku;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    private LinearLayout splashScreen;
    private FrameLayout mainContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        splashScreen = findViewById(R.id.splash_screen_layout);
        mainContent = findViewById(R.id.main_content);

        // Hide main content initially
        mainContent.setVisibility(View.GONE);

        // Start splash screen animation
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        splashScreen.startAnimation(fadeIn);

        // Use a Handler to delay the transition to the main content
        new Handler().postDelayed(() -> {
            Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
            splashScreen.startAnimation(fadeOut);

            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    splashScreen.setVisibility(View.GONE);
                    mainContent.setVisibility(View.VISIBLE);
                    // Load the LoginFragment by default
                    loadFragment(new LoginFragment());
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });

        }, 3000); // 3-second delay for the splash screen
    }

    // Helper method to load fragments
    public void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_content, fragment);
        transaction.commit();
    }
}
