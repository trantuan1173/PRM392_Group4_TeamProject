package com.example.prm392_group4_teamproject;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface MessageApi {

    // Lấy danh sách tin nhắn theo matchId
    @GET("messages/{matchId}")
    Call<MessageListResponse> getMessages(
            @Header("Authorization") String token,
            @Path("matchId") String matchId,
            @Query("page") int page,
            @Query("limit") int limit
    );

    // Gửi tin nhắn (text, image, gif)
    @POST("messages/{matchId}")
    Call<SendMessageResponse> sendMessage(
            @Header("Authorization") String token,
            @Path("matchId") String matchId,
            @Body MessageSendRequest message
    );

    // Đánh dấu tin nhắn đã đọc
    @PUT("messages/{matchId}/read")
    Call<ApiResponse> markMessagesAsRead(
            @Header("Authorization") String token,
            @Path("matchId") String matchId
    );

    // Xoá tin nhắn (soft delete)
    @PUT("messages/delete/{messageId}")
    Call<ApiResponse> deleteMessage(
            @Header("Authorization") String token,
            @Path("messageId") String messageId
    );
}
