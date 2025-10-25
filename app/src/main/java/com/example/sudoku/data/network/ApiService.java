// Relative Path: Sudoku-App/app/src/main/java/com/example/sudoku/data/network/ApiService.java
package com.example.sudoku.data.network;

import com.example.sudoku.data.model.AuthResponse;
import com.example.sudoku.data.model.GameResponse;
import com.example.sudoku.data.model.GameUpdateRequest; // Import GameUpdateRequest
import com.example.sudoku.data.model.LoginRequest;
import com.example.sudoku.data.model.PuzzleResponse;
import com.example.sudoku.data.model.RegisterRequest;
import com.example.sudoku.data.model.UpdateResponse; // Import UpdateResponse
import com.example.sudoku.data.model.UserResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT; // Import PUT
import retrofit2.http.Path;

public interface ApiService {

    @POST("/api/auth/register")
    Call<AuthResponse> registerUser(@Body RegisterRequest registerRequest);

    @POST("/api/auth/login")
    Call<AuthResponse> loginUser(@Body LoginRequest loginRequest);

    @GET("/api/user/")
    Call<UserResponse> getUser(); // Requires Auth Header provided by Interceptor

    @GET("/api/game/new_game/{difficulty}")
    Call<PuzzleResponse> getNewGame(@Path("difficulty") String difficulty);

    // --- NEW ---
    @PUT("/api/game/update_game") // Use PUT for updates
    Call<UpdateResponse> updateGame(@Body GameUpdateRequest gameUpdate); // Send update data in body

    // --- New Endpoint for In-Progress Game ---
    @GET("/api/user/in_progress_game")
    Call<GameResponse> getInProgressGame();

}

