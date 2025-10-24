// Relative Path: Sudoku-App/app/src/main/java/com/example/sudoku/data/network/RetrofitClient.java
package com.example.sudoku.data.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class RetrofitClient {

    // Use "10.0.2.2" to connect from Android Emulator to localhost
    // Replace with your production server's URL when deploying
    private static final String BASE_URL = "http://10.0.2.2:3001/"; // Use port 3001 as in your .env

    private static Retrofit retrofit = null;

    public static ApiService getApiService() {
        if (retrofit == null) {
            // Create a logging interceptor to see request/response logs
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}

