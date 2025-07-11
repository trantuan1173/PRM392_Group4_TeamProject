package com.example.prm392_group4_teamproject.CAPI;

import com.example.prm392_group4_teamproject.model.ApiResponse;
import com.example.prm392_group4_teamproject.model.MessageListResponse;
import com.example.prm392_group4_teamproject.model.MessageSendRequest;
import com.example.prm392_group4_teamproject.model.SendMessageResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;


import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

    public interface MessageApi {

        @GET("/api/messages/{matchId}")
        Call<MessageListResponse> getMessages(
                @Header("Authorization") String token,
                @Path("matchId") String matchId,
                @Query("page") int page,
                @Query("limit") int limit
        );

        // Gửi tin nhắn (text, image, gif)
        @POST("/api/messages/{matchId}")
        Call<SendMessageResponse> sendMessage(
                @Header("Authorization") String token,
                @Path("matchId") String matchId,
                @Body MessageSendRequest message
        );

        // Đánh dấu tin nhắn đã đọc
        @PUT("/api/messages/{matchId}/read")
        Call<ApiResponse> markMessagesAsRead(
                @Header("Authorization") String token,
                @Path("matchId") String matchId
        );

        // Xoá tin nhắn (soft delete)
        @PUT("/api/messages/delete/{messageId}")
        Call<ApiResponse> deleteMessage(
                @Header("Authorization") String token,
                @Path("messageId") String messageId
        );
    }


