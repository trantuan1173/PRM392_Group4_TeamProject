package com.example.prm392_group4_teamproject.CAPI;

import com.google.gson.annotations.SerializedName;

public class NotificationItem {

    @SerializedName("_id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("message")
    private String message;

    @SerializedName("createdAt")
    private String createdAt;

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
