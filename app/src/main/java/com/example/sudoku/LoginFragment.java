// Relative Path: Sudoku-App/app/src/main/java/com/example/sudoku/LoginFragment.java
package com.example.sudoku;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText; // Import EditText
import android.widget.TextView;
import android.widget.Toast; // Import Toast

// Imports for networking
import com.example.sudoku.data.local.SessionManager; // Import SessionManager
import com.example.sudoku.data.model.AuthResponse;
import com.example.sudoku.data.model.LoginRequest;
import com.example.sudoku.data.network.ApiService;
import com.example.sudoku.data.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    // UI elements
    private EditText emailInput;
    private EditText passwordInput;
    private Button loginButton;

    // SessionManager
    private SessionManager sessionManager;

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

        // Initialize SessionManager
        sessionManager = new SessionManager(getContext());

        // Find UI elements
        emailInput = view.findViewById(R.id.email_input);
        passwordInput = view.findViewById(R.id.password_input);
        loginButton = view.findViewById(R.id.login_button);
        TextView showRegister = view.findViewById(R.id.show_register);
        TextView forgotPassword = view.findViewById(R.id.forgot_password);
        TextView loginTitle = view.findViewById(R.id.login_title);

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

        // Handle login button click with API call
        loginButton.setOnClickListener(v -> {
            handleLogin();
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

    private void handleLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Basic validation
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), "Email and Password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state (e.g., disable button)
        loginButton.setEnabled(false);
        loginButton.setText("Logging in...");

        // Create API request
        ApiService apiService = RetrofitClient.getApiService();
        LoginRequest loginRequest = new LoginRequest(email, password);
        Call<AuthResponse> call = apiService.loginUser(loginRequest);

        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                // Re-enable button
                loginButton.setEnabled(true);
                loginButton.setText("Login");

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    if ("success".equals(authResponse.getStatus()) && authResponse.getToken() != null) {
                        // --- SUCCESS ---
                        Log.d("LoginSuccess", "Token: " + authResponse.getToken());
                        Toast.makeText(getContext(), "Login Successful!", Toast.LENGTH_SHORT).show();

                        // --- SAVE THE TOKEN ---
                        sessionManager.saveAuthToken(authResponse.getToken());

                        navigateToHome();

                    } else {
                        // API returned success=false or other error
                        Toast.makeText(getContext(), "Login failed: " + authResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    // HTTP error (404, 500, etc.)
                    String errorMsg = "Login failed. Code: " + response.code();
                    try {
                        // Try to parse the error body
                        if (response.errorBody() != null) {
                            errorMsg += ", " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e("LoginError", "Error parsing error body", e);
                    }
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                // Network failure (no internet, host unreachable)
                loginButton.setEnabled(true);
                loginButton.setText("Login");
                Log.e("LoginFailure", "Network error: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Login failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void navigateToHome() {
        if (getActivity() == null) return;
        Intent intent = new Intent(getActivity(), HomeActivity.class);
        startActivity(intent);
        getActivity().finish(); // Finish MainActivity so user can't go back to it
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

