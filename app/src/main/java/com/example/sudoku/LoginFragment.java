package com.example.sudoku;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;

public class LoginFragment extends Fragment {

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView showRegister = view.findViewById(R.id.show_register);
        TextView forgotPassword = view.findViewById(R.id.forgot_password);
        TextView loginTitle = view.findViewById(R.id.login_title);
        Button loginButton = view.findViewById(R.id.login_button);

        // Apply gradient safely after layout
        loginTitle.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                loginTitle.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                applyGradientToText(loginTitle);
            }
        });

        // Handle navigation to RegisterFragment
        showRegister.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new RegisterFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Handle navigation to ForgotPasswordFragment
        forgotPassword.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ForgotPasswordFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Handle login button click
        loginButton.setOnClickListener(v -> {
            // TODO: Add actual login validation here
            Intent intent = new Intent(getActivity(), HomeActivity.class);
            startActivity(intent);
            getActivity().finish(); // Finish MainActivity so user can't go back to it
        });

        // Safer way to color "Register here"
        String text = "New challenger? Register here";
        SpannableString spannableString = new SpannableString(text);
        String targetText = "Register here";
        int start = text.indexOf(targetText);
        int end = start + targetText.length();

        if (start != -1) { // Check if the target text was found
            ForegroundColorSpan fcs = new ForegroundColorSpan(Color.parseColor("#00FFD1"));
            spannableString.setSpan(fcs, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        showRegister.setText(spannableString);
    }

    private void applyGradientToText(TextView textView) {
        if (textView.getWidth() == 0) return; // Avoid crash if view not measured yet
        android.text.TextPaint paint = textView.getPaint();
        float width = paint.measureText(textView.getText().toString());

        android.graphics.Shader textShader = new android.graphics.LinearGradient(
                0, 0, width, textView.getTextSize(),
                new int[]{
                        Color.parseColor("#38B2AC"), // Teal
                        Color.parseColor("#D53F8C")  // Pink
                },
                null,
                android.graphics.Shader.TileMode.CLAMP
        );

        textView.getPaint().setShader(textShader);
    }
}

