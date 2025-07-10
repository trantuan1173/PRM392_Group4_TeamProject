package com.example.prm392_group4_teamproject.CAPI;

import retrofit2.Call;
import retrofit2.http.*;


public interface ApiService {
    @POST("/api/auth/register")
    Call<Void> registerUser(@Body RegisterRequest request);

    @POST("/api/auth/login")
    Call<LoginResponse> loginUser(@Body LoginRequest request);

    @GET("/api/users/profile")
    Call<ProfileResponse> getProfile();

    @PUT("/api/users/profile")
    Call<Void> updateProfile(@Body ProfileRequest request);

    @GET("/api/matches")
    Call<MatchResponse> getMatches();
    @GET("/api/notifications")
    Call<NotificationResponse> getNotifications();

}


