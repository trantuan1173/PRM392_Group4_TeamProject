package com.example.prm392_group4_teamproject.CAPI;

import com.example.prm392_group4_teamproject.model.CreateProfileRequest;
import com.example.prm392_group4_teamproject.model.LoginRequest;
import com.example.prm392_group4_teamproject.model.LoginResponse;
import com.example.prm392_group4_teamproject.model.MatchResponse;
import com.example.prm392_group4_teamproject.model.ProfileRequest;
import com.example.prm392_group4_teamproject.model.ProfileResponse;
import com.example.prm392_group4_teamproject.model.RegisterRequest;

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

    @PUT("/api/users/profile")
    Call<Void> createProfile(@Header("Authorization") String token, @Body CreateProfileRequest request);

    @GET("/api/matches")
    Call<MatchResponse> getMatches();
    @GET("/api/notifications")
    Call<NotificationResponse> getNotifications();

}


