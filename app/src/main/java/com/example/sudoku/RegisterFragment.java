// Relative Path: Sudoku-App/app/src/main/java/com/example/sudoku/RegisterFragment.java
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
        sessionManager = new SessionManager(getContext());

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
                registerTitle.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                applyGradientToText(registerTitle);
            }
        });

        // Handle navigation back to LoginFragment
        showLogin.setOnClickListener(v -> {
            // This will take the user back to the previous screen (LoginFragment)
            getParentFragmentManager().popBackStack();
        });

        // Handle create account button click
        createAccountButton.setOnClickListener(v -> {
            handleRegister();
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

    private void handleRegister() {
        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Basic validation
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
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
        ApiService apiService = RetrofitClient.getApiService();
        RegisterRequest registerRequest = new RegisterRequest(username, email, password);
        Call<AuthResponse> call = apiService.registerUser(registerRequest);

        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                // Re-enable button
                createAccountButton.setEnabled(true);
                createAccountButton.setText("Create Account");

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    if ("success".equals(authResponse.getStatus()) && authResponse.getToken() != null) {
                        // --- SUCCESS ---
                        Log.d("RegisterSuccess", "Token: " + authResponse.getToken());
                        Toast.makeText(getContext(), "Registration Successful!", Toast.LENGTH_SHORT).show();

                        // --- SAVE THE TOKEN ---
                        sessionManager.saveAuthToken(authResponse.getToken());

                        navigateToHome();

                    } else {
                        // API returned success=false or other error
                        Toast.makeText(getContext(), "Registration failed: " + authResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    // HTTP error (409 Conflict, 500, etc.)
                    String errorMsg = "Registration failed. Code: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            // The backend sends detail on 409
                            errorMsg = "Registration failed: User already exists.";
                        }
                    } catch (Exception e) {
                        Log.e("RegisterError", "Error parsing error body", e);
                    }
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                // Network failure
                createAccountButton.setEnabled(true);
                createAccountButton.setText("Create Account");
                Log.e("RegisterFailure", "Network error: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Registration failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void navigateToHome() {
        if (getActivity() == null) return;
        Intent intent = new Intent(getActivity(), HomeActivity.class);
        startActivity(intent);
        getActivity().finish(); // Finish MainActivity
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

