// Relative Path: Sudoku-App/app/src/main/java/com/example/sudoku/data/local/SessionManager.java
package com.example.sudoku.data.local;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "SudokuArenaSession";
    private static final String KEY_AUTH_TOKEN = "auth_token";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /**
     * Saves the user's authentication token to SharedPreferences.
     * @param token The JWT received from the backend.
     */
    public void saveAuthToken(String token) {
        editor.putString(KEY_AUTH_TOKEN, token);
        editor.apply();
    }

    /**
     * Fetches the saved authentication token.
     * @return The saved token, or null if no token is found.
     */
    public String fetchAuthToken() {
        return prefs.getString(KEY_AUTH_TOKEN, null);
    }

    /**
     * Clears the saved authentication token (for logout).
     */
    public void clearAuthToken() {
        editor.remove(KEY_AUTH_TOKEN);
        editor.apply();
    }

    /**
     * Checks if a user is currently logged in (i.e., has a token).
     * @return true if a token is saved, false otherwise.
     */
    public boolean isLoggedIn() {
        return fetchAuthToken() != null;
    }
}
