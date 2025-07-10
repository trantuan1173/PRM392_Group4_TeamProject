package com.example.prm392_group4_teamproject.CAPI;

import com.example.prm392_group4_teamproject.model.LocationUpdate;
import com.example.prm392_group4_teamproject.model.ApiResponse;
import com.example.prm392_group4_teamproject.model.MatchDetailResponse;
import com.example.prm392_group4_teamproject.model.MatchMapsResponse;
import com.example.prm392_group4_teamproject.model.MatchResponse;
import com.example.prm392_group4_teamproject.model.MatchResponse2;
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
    Call<UserResponse> getProfile(@Header("Authorization") String token);
    @GET("users/profile")
    Call<User> getProfile2(@Header("Authorization") String token);

    @PUT("users/location")
    Call<ApiResponse> updateLocation(@Header("Authorization") String token, @Body LocationUpdate location);
    @GET("matches")
    Call<MatchMapsResponse> getMatches(@Header("Authorization") String token, @Query("page") int page, @Query("limit") int limit);
    @GET("matches")
    Call<MatchResponse2> getMatches2(@Header("Authorization") String token, @Query("page") int page, @Query("limit") int limit);
    @GET("users/{userId}")
    Call<UserResponse> getUserById(@Header("Authorization") String token, @Path("userId") String userId);

    @GET("matches/{matchId}")
    Call<MatchDetailResponse> getMatchById(@Header("Authorization") String token, @Path("matchId") String matchId);
}
