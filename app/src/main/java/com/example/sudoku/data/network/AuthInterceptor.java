// Relative Path: Sudoku-App/app/src/main/java/com/example/sudoku/data/network/AuthInterceptor.java
package com.example.sudoku.data.network;

import android.content.Context;
import androidx.annotation.NonNull;
import com.example.sudoku.data.local.SessionManager;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Interceptor to add the Authorization header to requests.
 */
public class AuthInterceptor implements Interceptor {

    private SessionManager sessionManager;

    public AuthInterceptor(Context context) {
        sessionManager = new SessionManager(context.getApplicationContext());
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request.Builder requestBuilder = chain.request().newBuilder();

        // Get the token from SessionManager
        String token = sessionManager.fetchAuthToken();

        if (token != null) {
            // Add the Authorization header
            requestBuilder.addHeader("Authorization", "Bearer " + token);
        }

        return chain.proceed(requestBuilder.build());
    }
}
