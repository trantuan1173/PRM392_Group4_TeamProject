package com.example.prm392_group4_teamproject.CAPI;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NotificationResponse {

    @SerializedName("notifications")
    private List<NotificationItem> notifications;

    @SerializedName("unreadCount")
    private int unreadCount;

    public List<NotificationItem> getNotifications() {
        return notifications;
    }

    public int getUnreadCount() {
        return unreadCount;
    }
}
