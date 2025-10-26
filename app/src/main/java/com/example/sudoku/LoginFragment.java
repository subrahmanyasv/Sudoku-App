// Relative Path: app/src/main/java/com/example/sudoku/LoginFragment.java
package com.example.sudoku;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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
        // Use requireContext() for safer context handling in fragments
        sessionManager = new SessionManager(requireContext());

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
                // Check if the view is still attached before removing listener
                if (loginTitle.getViewTreeObserver().isAlive()) {
                    try {
                        loginTitle.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        applyGradientToText(loginTitle);
                    } catch (IllegalStateException e) {
                        Log.e("LoginFragment", "Error removing layout listener", e);
                    }
                }
            }
        });


        // Handle navigation to RegisterFragment
        showRegister.setOnClickListener(v -> {
            // Use getParentFragmentManager() for fragment transactions within an activity
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new RegisterFragment())
                    .addToBackStack(null) // Allows user to press back to return to login
                    .commit();
        });


        // Handle navigation to ForgotPasswordFragment
        forgotPassword.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ForgotPasswordFragment())
                    .addToBackStack(null) // Allows user to press back
                    .commit();
        });


        // Handle login button click with API call
        loginButton.setOnClickListener(v -> {
            handleLogin();
        });

        // Safer way to color "Register here"
        String text = showRegister.getText().toString(); // Get text dynamically
        SpannableString spannableString = new SpannableString(text);
        String targetText = "Register here";
        int start = text.indexOf(targetText);

        if (start != -1) { // Check if the target text was found
            int end = start + targetText.length();
            try {
                // Use color resource for better theme support
                int color = ContextCompat.getColor(requireContext(), R.color.button_primary); // Or your accent color
                ForegroundColorSpan fcs = new ForegroundColorSpan(color);
                spannableString.setSpan(fcs, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } catch (Exception e) {
                Log.e("LoginFragment", "Error applying color span", e);
                // Fallback if color resource fails
                try {
                    ForegroundColorSpan fcsFallback = new ForegroundColorSpan(Color.parseColor("#00FFD1"));
                    spannableString.setSpan(fcsFallback, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } catch (IllegalArgumentException iae) {
                    Log.e("LoginFragment", "Fallback color parsing failed", iae);
                }
            }
        }
        showRegister.setText(spannableString);
    }


    private void handleLogin() {
        // Ensure fragment is attached before accessing context or inputs
        if (!isAdded() || getContext() == null) {
            Log.w("LoginFragment", "handleLogin called when fragment not attached.");
            return;
        }

        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Basic validation
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), "Email and Password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- ADDED: Email format validation ---
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getContext(), "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state (e.g., disable button)
        loginButton.setEnabled(false);
        loginButton.setText("Logging in...");

        // Create API request
        ApiService apiService = RetrofitClient.getApiService(requireContext());
        LoginRequest loginRequest = new LoginRequest(email, password);

        Log.d("LoginFragment", "Attempting login for email: " + email); // Log attempt

        Call<AuthResponse> call = apiService.loginUser(loginRequest);

        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                // Ensure fragment is still attached before updating UI
                if (!isAdded() || getContext() == null) return;

                // Re-enable button
                loginButton.setEnabled(true);
                loginButton.setText("Login");

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    // *** MODIFIED: Check for userId as well ***
                    if ("success".equals(authResponse.getStatus()) && authResponse.getToken() != null && authResponse.getUserId() != null) {
                        Log.d("LoginSuccess", "Token: " + authResponse.getToken() + ", UserID: " + authResponse.getUserId());
                        Toast.makeText(getContext(), "Login Successful!", Toast.LENGTH_SHORT).show();

                        // --- SAVE THE TOKEN and USER ID---
                        sessionManager.saveAuthToken(authResponse.getToken());
                        sessionManager.saveUserId(authResponse.getUserId()); // Save the user ID

                        navigateToHome();

                    } else {
                        // Log detailed failure info from backend
                        Log.w("LoginFragment", "Login failed (API success=false or missing data): Status=" + authResponse.getStatus() + ", Message=" + authResponse.getMessage() + ", HasToken=" + (authResponse.getToken()!=null) + ", HasUserId=" + (authResponse.getUserId()!=null));
                        String failMsg = authResponse.getMessage() != null ? authResponse.getMessage() : "Login failed. Please check credentials.";
                        Toast.makeText(getContext(), failMsg, Toast.LENGTH_LONG).show();
                    }
                } else {
                    // HTTP error (404, 401, 500, etc.)
                    String errorMsg = "Login failed";
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string(); // Read error body once
                            // Basic check if error body might be JSON containing 'detail'
                            if (errorBody.trim().startsWith("{") && errorBody.contains("\"detail\"")) {
                                // Attempt to parse detail (simple parsing, consider using Gson for robustness)
                                String detail = errorBody.substring(errorBody.indexOf("\"detail\":\"") + 10);
                                detail = detail.substring(0, detail.indexOf("\""));
                                errorMsg += ": " + detail;
                            } else {
                                // Otherwise, append raw error body if short, or just code
                                errorMsg += " (Code: " + response.code() + ")";
                                Log.e("LoginErrorBody", errorBody); // Log the full error body
                            }
                        } else {
                            errorMsg += " (Code: " + response.code() + ", " + response.message() + ")";
                        }
                    } catch (Exception e) {
                        Log.e("LoginError", "Error parsing/reading error body", e);
                        errorMsg += " (Code: " + response.code() + ")"; // Fallback
                    }
                    Log.e("LoginError", "HTTP Error: " + response.code() + " - " + response.message() + " | Body: " + errorBody);
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                // Ensure fragment is still attached
                if (!isAdded() || getContext() == null) return;

                // Network failure (no internet, host unreachable)
                loginButton.setEnabled(true);
                loginButton.setText("Login");
                Log.e("LoginFailure", "Network error: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Login failed: Network error. Please check connection.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void navigateToHome() {
        if (getActivity() == null) return;
        Intent intent = new Intent(getActivity(), HomeActivity.class);
        // Clear back stack so user cannot go back to login screen
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish(); // Finish MainActivity
    }


    private void applyGradientToText(TextView textView) {
        // Add null checks for safety
        if (textView == null || textView.getWidth() == 0 || textView.getText() == null) return;
        android.text.TextPaint paint = textView.getPaint();
        if(paint == null) return;

        float width = paint.measureText(textView.getText().toString());
        if(width <= 0) return; // Avoid issues if text is empty or width is zero

        try {
            // Use color resources
            int startColor = ContextCompat.getColor(requireContext(), R.color.gradient_start);
            int endColor = ContextCompat.getColor(requireContext(), R.color.gradient_end);

            android.graphics.Shader textShader = new android.graphics.LinearGradient(
                    0, 0, width, textView.getTextSize(),
                    new int[]{startColor, endColor},
                    null,
                    android.graphics.Shader.TileMode.CLAMP
            );

            paint.setShader(textShader);
            textView.invalidate(); // Force redraw with shader
        } catch (Exception e) {
            Log.e("LoginFragment", "Error applying gradient shader", e);
            // Fallback to solid color if gradient fails
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        }
    }

}
