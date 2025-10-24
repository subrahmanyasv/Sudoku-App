// Relative Path: Sudoku-App/app/src/main/java/com/example/sudoku/MainActivity.java
package com.example.sudoku;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent; // Import Intent
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.sudoku.data.local.SessionManager; // Import SessionManager

public class MainActivity extends AppCompatActivity {

    private LinearLayout splashScreen;
    private FrameLayout fragmentContainer;
    private SessionManager sessionManager; // Add SessionManager

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        splashScreen = findViewById(R.id.splash_screen_layout);
        fragmentContainer = findViewById(R.id.fragment_container);
        TextView splashTitle = findViewById(R.id.splash_title);

        // Initialize SessionManager
        sessionManager = new SessionManager(this);

        // Safely apply the gradient after the view is laid out
        splashTitle.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                splashTitle.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                applyGradientToText(splashTitle);
            }
        });

        fragmentContainer.setVisibility(View.GONE);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        splashScreen.startAnimation(fadeIn);

        new Handler().postDelayed(() -> {
            // --- CHECK IF LOGGED IN ---
            if (sessionManager.isLoggedIn()) {
                // User is already logged in, go straight to Home
                navigateToHome();
            } else {
                // User is not logged in, show login fragments
                showAuthFragments();
            }
        }, 3000); // 3-second splash screen
    }

    private void showAuthFragments() {
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        splashScreen.startAnimation(fadeOut);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                splashScreen.setVisibility(View.GONE);
                fragmentContainer.setVisibility(View.VISIBLE);
                loadFragment(new LoginFragment());
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }

    private void navigateToHome() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);
        finish(); // Finish this activity
    }

    public void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void applyGradientToText(TextView textView) {
        if (textView.getWidth() == 0) return;
        android.text.TextPaint paint = textView.getPaint();
        float width = paint.measureText(textView.getText().toString());

        android.graphics.Shader textShader = new android.graphics.LinearGradient(0, 0, width, textView.getTextSize(),
                new int[]{
                        Color.parseColor("#38B2AC"),
                        Color.parseColor("#D53F8C")
                }, null, android.graphics.Shader.TileMode.CLAMP);
        textView.getPaint().setShader(textShader);
    }
}
