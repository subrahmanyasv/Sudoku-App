// Relative Path: app/src/main/java/com/example/sudoku/RegisterFragment.java
package com.example.sudoku;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat; // Import ContextCompat
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

// Imports for networking
import com.example.sudoku.data.local.SessionManager; // Import SessionManager
import com.example.sudoku.data.model.AuthResponse;
import com.example.sudoku.data.model.RegisterRequest;
import com.example.sudoku.data.network.ApiService;
import com.example.sudoku.data.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterFragment extends Fragment {

    // UI elements
    private EditText usernameInput, emailInput, passwordInput, confirmPasswordInput;
    private Button createAccountButton;

    // SessionManager
    private SessionManager sessionManager;

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

        // Initialize SessionManager
        sessionManager = new SessionManager(requireContext());

        // Find UI elements
        usernameInput = view.findViewById(R.id.username_input);
        emailInput = view.findViewById(R.id.email_input);
        passwordInput = view.findViewById(R.id.password_input);
        confirmPasswordInput = view.findViewById(R.id.confirm_password_input);
        createAccountButton = view.findViewById(R.id.create_account_button);
        TextView showLogin = view.findViewById(R.id.show_login);
        TextView registerTitle = view.findViewById(R.id.register_title);

        // Apply gradient safely after layout
        registerTitle.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Add a check to ensure the view observer is alive
                if (registerTitle.getViewTreeObserver().isAlive()) {
                    try {
                        registerTitle.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        applyGradientToText(registerTitle);
                    } catch (IllegalStateException e) {
                        Log.e("RegisterFragment", "Error removing layout listener", e);
                    }
                }
            }
        });


        // Handle navigation back to LoginFragment
        showLogin.setOnClickListener(v -> {
            // Check if fragment manager is available
            if (getParentFragmentManager() != null) {
                // Use popBackStack() if RegisterFragment was added via addToBackStack()
                getParentFragmentManager().popBackStack();
                // If not added via backstack, replace fragment:
                // getParentFragmentManager().beginTransaction()
                //        .replace(R.id.fragment_container, new LoginFragment())
                //        .commit();
            }
        });


        // Handle create account button click
        createAccountButton.setOnClickListener(v -> {
            handleRegister();
        });

        // Safer way to color the "Login" link
        String text = showLogin.getText().toString(); // Get text dynamically
        SpannableString spannableString = new SpannableString(text);
        String targetText = "Login";
        int start = text.indexOf(targetText);

        if (start != -1) { // Check if the target text was found
            int end = start + targetText.length();
            try {
                int color = ContextCompat.getColor(requireContext(), R.color.button_primary); // Or your accent color
                ForegroundColorSpan fcs = new ForegroundColorSpan(color);
                spannableString.setSpan(fcs, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } catch (Exception e) {
                Log.e("RegisterFragment", "Error applying color span", e);
                try {
                    // Fallback color
                    ForegroundColorSpan fcsFallback = new ForegroundColorSpan(Color.parseColor("#00FFD1"));
                    spannableString.setSpan(fcsFallback, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } catch (IllegalArgumentException iae) {
                    Log.e("RegisterFragment", "Fallback color parsing failed", iae);
                }
            }
        }
        showLogin.setText(spannableString);
    }


    private void handleRegister() {
        // Ensure fragment is attached before accessing context or inputs
        if (!isAdded() || getContext() == null) {
            Log.w("RegisterFragment", "handleRegister called when fragment not attached.");
            return;
        }

        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Basic validation
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- ADDED: Email format validation ---
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getContext(), "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- ADDED: Minimum password length ---
        if (password.length() < 6) { // Example: require at least 6 characters
            Toast.makeText(getContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }


        // Show loading state
        createAccountButton.setEnabled(false);
        createAccountButton.setText("Creating Account...");

        // Create API request
        ApiService apiService = RetrofitClient.getApiService(requireContext());
        RegisterRequest registerRequest = new RegisterRequest(username, email, password);

        Log.d("RegisterFragment", "Attempting registration for email: " + email); // Log attempt

        Call<AuthResponse> call = apiService.registerUser(registerRequest);

        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                // Ensure fragment is still attached
                if (!isAdded() || getContext() == null) return;

                // Re-enable button
                createAccountButton.setEnabled(true);
                createAccountButton.setText("Create Account");

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    // *** MODIFIED: Check for userId as well ***
                    if ("success".equals(authResponse.getStatus()) && authResponse.getToken() != null && authResponse.getUserId() != null) {
                        Log.d("RegisterSuccess", "Token: " + authResponse.getToken() + ", UserID: " + authResponse.getUserId());
                        Toast.makeText(getContext(), "Registration Successful!", Toast.LENGTH_SHORT).show();

                        // --- SAVE THE TOKEN and USER ID ---
                        sessionManager.saveAuthToken(authResponse.getToken());
                        sessionManager.saveUserId(authResponse.getUserId()); // Save the user ID

                        navigateToHome();

                    } else {
                        // Log detailed failure info from backend
                        Log.w("RegisterFragment", "Registration failed (API success=false or missing data): Status=" + authResponse.getStatus() + ", Message=" + authResponse.getMessage() + ", HasToken=" + (authResponse.getToken()!=null) + ", HasUserId=" + (authResponse.getUserId()!=null));
                        String failMsg = authResponse.getMessage() != null ? authResponse.getMessage() : "Registration failed. Please try again.";
                        Toast.makeText(getContext(), failMsg, Toast.LENGTH_LONG).show();
                    }
                } else {
                    // HTTP error (409 Conflict, 500, etc.)
                    String errorMsg = "Registration failed";
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string(); // Read error body once
                            // Basic check if error body might be JSON containing 'detail'
                            if (errorBody.trim().startsWith("{") && errorBody.contains("\"detail\"")) {
                                // Attempt to parse detail
                                String detail = errorBody.substring(errorBody.indexOf("\"detail\":\"") + 10);
                                detail = detail.substring(0, detail.indexOf("\""));
                                errorMsg += ": " + detail;
                            } else {
                                errorMsg += " (Code: " + response.code() + ")";
                                Log.e("RegisterErrorBody", errorBody);
                            }
                        } else {
                            errorMsg += " (Code: " + response.code() + ", " + response.message() + ")";
                        }
                    } catch (Exception e) {
                        Log.e("RegisterError", "Error parsing/reading error body", e);
                        errorMsg += " (Code: " + response.code() + ")"; // Fallback
                    }
                    Log.e("RegisterError", "HTTP Error: " + response.code() + " - " + response.message() + " | Body: " + errorBody);
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                // Ensure fragment is still attached
                if (!isAdded() || getContext() == null) return;

                // Network failure
                createAccountButton.setEnabled(true);
                createAccountButton.setText("Create Account");
                Log.e("RegisterFailure", "Network error: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Registration failed: Network error. Please check connection.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void navigateToHome() {
        if (getActivity() == null) return;
        Intent intent = new Intent(getActivity(), HomeActivity.class);
        // Clear back stack
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
        if(width <= 0) return; // Avoid issues

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
            textView.invalidate(); // Force redraw
        } catch (Exception e) {
            Log.e("RegisterFragment", "Error applying gradient shader", e);
            // Fallback to solid color
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        }
    }

}
