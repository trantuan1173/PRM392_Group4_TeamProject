package com.example.prm392_group4_teamproject.CAPI;

import retrofit2.Call;
import retrofit2.http.*;


public interface ApiService {
    @POST("/api/auth/register")
    Call<Void> registerUser(@Body RegisterRequest request);
    @POST("/api/auth/login")
    Call<Void> loginUser(@Body LoginRequest request);
}


