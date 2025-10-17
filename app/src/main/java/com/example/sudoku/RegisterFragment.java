package com.example.sudoku;

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
import android.widget.TextView;

public class RegisterFragment extends Fragment {

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView showLogin = view.findViewById(R.id.show_login);
        TextView registerTitle = view.findViewById(R.id.register_title);

        // Apply gradient safely after layout
        registerTitle.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                registerTitle.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                applyGradientToText(registerTitle);
            }
        });

        // Handle navigation back to LoginFragment
        showLogin.setOnClickListener(v -> {
            // This will take the user back to the previous screen (LoginFragment)
            getParentFragmentManager().popBackStack();
        });

        // Safer way to color the "Login" link
        String text = "Already have an account? Login";
        SpannableString spannableString = new SpannableString(text);
        String targetText = "Login";
        int start = text.indexOf(targetText);
        int end = start + targetText.length();

        if (start != -1) { // Check if the target text was found
            ForegroundColorSpan fcs = new ForegroundColorSpan(Color.parseColor("#00FFD1"));
            spannableString.setSpan(fcs, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        showLogin.setText(spannableString);
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

