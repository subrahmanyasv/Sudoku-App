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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ForgotPasswordFragment extends Fragment {

    public ForgotPasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_forgot_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find all UI elements
        Button sendOtpButton = view.findViewById(R.id.send_otp_button);
        Button verifyButton = view.findViewById(R.id.verify_button);
        TextView resendOtp = view.findViewById(R.id.resend_otp);
        TextView backToLogin = view.findViewById(R.id.back_to_login);
        TextView title = view.findViewById(R.id.forgot_password_title);

        // Safely apply gradient to title
        title.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                title.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                applyGradientToText(title);
            }
        });

        // Set click listeners
        sendOtpButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "OTP sent to your email!", Toast.LENGTH_SHORT).show();
        });

        verifyButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Verification logic goes here.", Toast.LENGTH_SHORT).show();
        });

        backToLogin.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LoginFragment())
                    .commit();
        });

        // Style the "Resend OTP" part of the text
        String text = "Didn't receive code? Resend OTP";
        SpannableString spannableString = new SpannableString(text);
        int start = text.indexOf("Resend OTP");
        if (start != -1) {
            int end = start + "Resend OTP".length();
            ForegroundColorSpan fcs = new ForegroundColorSpan(Color.parseColor("#00FFD1"));
            spannableString.setSpan(fcs, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        resendOtp.setText(spannableString);

        resendOtp.setOnClickListener(v -> {
            Toast.makeText(getContext(), "OTP resent!", Toast.LENGTH_SHORT).show();
        });
    }

    private void applyGradientToText(TextView textView) {
        if (textView.getWidth() == 0) return;
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

