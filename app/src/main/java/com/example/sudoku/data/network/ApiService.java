// Relative Path: Sudoku-App/app/src/main/java/com/example/sudoku/data/network/ApiService.java
package com.example.sudoku.data.network;

import com.example.sudoku.data.model.AuthResponse;
import com.example.sudoku.data.model.LoginRequest;
import com.example.sudoku.data.model.RegisterRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    /**
     * Corresponds to the backend's /api/auth/login route
     */
    @POST("api/auth/login")
    Call<AuthResponse> loginUser(@Body LoginRequest loginRequest);

    /**
     * Corresponds to the backend's /api/auth/register route
     */
    @POST("api/auth/register")
    Call<AuthResponse> registerUser(@Body RegisterRequest registerRequest);

    // --- We will add game and user routes here later ---

}

