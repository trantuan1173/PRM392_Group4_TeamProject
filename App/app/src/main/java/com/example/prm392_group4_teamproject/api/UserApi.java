package com.example.prm392_group4_teamproject.api;

import com.example.prm392_group4_teamproject.model.LocationUpdate;
import com.example.prm392_group4_teamproject.model.ApiResponse;
import com.example.prm392_group4_teamproject.model.MatchResponse;
import com.example.prm392_group4_teamproject.model.User;
import com.example.prm392_group4_teamproject.model.UserResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UserApi {
    @GET("users/profile")
    Call<User> getProfile(@Header("Authorization") String token);


    @PUT("users/location")
    Call<ApiResponse> updateLocation(@Header("Authorization") String token, @Body LocationUpdate location);
    @GET("matches")
    Call<MatchResponse> getMatches(@Header("Authorization") String token, @Query("page") int page, @Query("limit") int limit);
    @GET("users/{userId}")
    Call<UserResponse> getUserById(@Header("Authorization") String token, @Path("userId") String userId);
}
