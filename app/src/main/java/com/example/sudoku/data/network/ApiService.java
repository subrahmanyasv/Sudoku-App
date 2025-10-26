// Relative Path: Sudoku-App/app/src/main/java/com/example/sudoku/data/network/ApiService.java
package com.example.sudoku.data.network;

import com.example.sudoku.data.model.AuthResponse;
import com.example.sudoku.data.model.GameResponse;
import com.example.sudoku.data.model.GameUpdateRequest; // Import GameUpdateRequest
import com.example.sudoku.data.model.LeaderboardResponse;
import com.example.sudoku.data.model.LoginRequest;
import com.example.sudoku.data.model.PuzzleResponse;
import com.example.sudoku.data.model.RegisterRequest;
import com.example.sudoku.data.model.UpdateResponse; // Import UpdateResponse
import com.example.sudoku.data.model.UserResponse;
import com.example.sudoku.data.model.ChallengeCompleteRequest;
import com.example.sudoku.data.model.ChallengeCreateRequest;
import com.example.sudoku.data.model.ChallengeRespondRequest;
import com.example.sudoku.data.model.ChallengeResponse;
import com.example.sudoku.data.model.UserBase;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT; // Import PUT
import retrofit2.http.Path;
import retrofit2.http.Query;

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

    @GET("/api/leaderboard/")
    Call<LeaderboardResponse> getLeaderboard(); // Requires Auth Header

    @GET("/api/user/game_history")
    Call<List<GameResponse>> getGameHistory(); // Returns a list of completed games

    // --- NEW CHALLENGE ENDPOINTS ---

    /**
     * Create a new challenge after finishing a game.
     */
    @POST("api/challenges/")
    Call<ChallengeResponse> createChallenge(@Body ChallengeCreateRequest challengeCreateRequest);

    /**
     * Get all pending challenges for the current user (where user is the opponent).
     */
    @GET("api/challenges/")
    Call<List<ChallengeResponse>> getChallenges();

    /**
     * Accept or reject a pending challenge.
     */
    @POST("api/challenges/{challenge_id}/respond")
    Call<ChallengeResponse> respondToChallenge(
            @Path("challenge_id") String challengeId,
            @Body ChallengeRespondRequest respondRequest
    );

    /**
     * Complete an accepted challenge (submits opponent's score).
     */
    @POST("api/challenges/{challenge_id}/complete")
    Call<ChallengeResponse> completeChallenge(
            @Path("challenge_id") String challengeId,
            @Body ChallengeCompleteRequest completeRequest
    );

    @GET("/api/user/user_list")
    Call<List<UserBase>> getUserList(@Query("username") String usernameQuery); // usernameQuery can be null

}

