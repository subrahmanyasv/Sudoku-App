// Relative Path: Sudoku-App/app/src/main/java/com/example/sudoku/data/network/RetrofitClient.java
package com.example.sudoku.data.network;

import android.content.Context; // Import Context
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class RetrofitClient {

    // Use "10.0.2.2" to connect from Android Emulator to localhost
    private static final String BASE_URL = "https://sudokuapp-backend.onrender.com/";

    private static Retrofit retrofit = null;
    private static ApiService apiService = null; // Cache the ApiService

    /**
     * Gets the singleton ApiService instance.
     * Requires Context to build the AuthInterceptor.
     */
    public static ApiService getApiService(Context context) {
        if (apiService == null) {
            if (retrofit == null) {
                // Create a logging interceptor to see request/response logs
                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);

                // --- Create the AuthInterceptor ---
                // Pass application context to avoid memory leaks
                AuthInterceptor authInterceptor = new AuthInterceptor(context.getApplicationContext());

                OkHttpClient client = new OkHttpClient.Builder()
                        .addInterceptor(logging)
                        .addInterceptor(authInterceptor) // --- Add the AuthInterceptor ---
                        .build();

                retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .client(client)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            }
            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }

    /**
     * Clears the cached ApiService and Retrofit instances.
     * Call this on logout to reset the client.
     */
    public static void clearInstance() {
        retrofit = null;
        apiService = null;
    }
}

