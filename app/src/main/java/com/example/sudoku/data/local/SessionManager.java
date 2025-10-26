// Relative Path: app/src/main/java/com/example/sudoku/data/local/SessionManager.java
package com.example.sudoku.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.annotation.Nullable; // Import Nullable

public class SessionManager {

    private static final String PREF_NAME = "SudokuArenaSession";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_USER_ID = "user_id"; // *** ADDED KEY ***
    private static final String TAG = "SessionManager";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    // Removed context member variable as it's only needed in constructor

    public SessionManager(Context context) {
        // Use application context to avoid leaks
        Context appContext = context.getApplicationContext();
        prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit(); // Get editor instance once
    }

    /**
     * Saves the user's authentication token to SharedPreferences.
     * @param token The JWT received from the backend.
     */
    public void saveAuthToken(@Nullable String token) {
        if (token != null) {
            editor.putString(KEY_AUTH_TOKEN, token);
            Log.d(TAG, "Auth token saved.");
        } else {
            editor.remove(KEY_AUTH_TOKEN); // Clear if token is null
            Log.d(TAG, "Auth token cleared (null provided).");
        }
        editor.apply(); // Apply changes
    }

    /**
     * Fetches the saved authentication token.
     * @return The saved token, or null if no token is found.
     */
    @Nullable // Indicate that the return value can be null
    public String fetchAuthToken() {
        return prefs.getString(KEY_AUTH_TOKEN, null);
    }

    // *** ADDED METHOD to save User ID ***
    /**
     * Saves the user's ID to SharedPreferences.
     * @param userId The User ID string received from the backend.
     */
    public void saveUserId(@Nullable String userId) {
        if (userId != null && !userId.isEmpty()) {
            editor.putString(KEY_USER_ID, userId);
            Log.d(TAG, "User ID saved: " + userId);
        } else {
            editor.remove(KEY_USER_ID); // Clear if ID is null or empty
            Log.d(TAG, "User ID cleared (null or empty provided).");
        }
        editor.apply(); // Apply changes
    }

    // *** ADDED METHOD to fetch User ID ***
    /**
     * Fetches the saved User ID.
     * @return The saved User ID string, or null if no ID is found.
     */
    @Nullable // Indicate that the return value can be null
    public String fetchUserId() {
        String userId = prefs.getString(KEY_USER_ID, null);
        Log.d(TAG, "Fetched User ID: " + userId);
        return userId;
    }


    /**
     * Clears all saved session data (token and user ID). Use for logout.
     */
    public void clear() {
        editor.remove(KEY_AUTH_TOKEN);
        editor.remove(KEY_USER_ID); // Also clear user ID
        editor.apply();
        Log.d(TAG, "Session data (Auth token, User ID) cleared.");
    }

    /**
     * Checks if a user is currently logged in (i.e., has a token).
     * @return true if a token is saved, false otherwise.
     */
    public boolean isLoggedIn() {
        return fetchAuthToken() != null;
    }

    // clearAuthToken() is now effectively replaced by clear()
    // public void clearAuthToken() { ... }

}
