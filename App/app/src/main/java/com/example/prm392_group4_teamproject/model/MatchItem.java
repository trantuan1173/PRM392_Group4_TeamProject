package com.example.prm392_group4_teamproject.model;


import com.google.gson.annotations.SerializedName;

public class MatchItem {
    @SerializedName("_id")
    private String _id;
    private OtherUserLite otherUser;
    private User fullOtherUser;

    private String lastMessageContent;

    private String lastMessageTime; // 🕒

    public void setLastMessageTime(String time) {
        this.lastMessageTime = time;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }


    public void setLastMessage(String content) {
        this.lastMessageContent = content;
    }

    public String getLastMessage() {
        return lastMessageContent;
    }

    public MatchItem(User fullOtherUser) {
        this.fullOtherUser = fullOtherUser;
    }

    public MatchItem() {
        // Default constructor for Retrofit/Gson
    }

    public MatchItem(String _id, OtherUserLite otherUser) {
        this._id = _id;
        this.otherUser = otherUser;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public OtherUserLite getOtherUser() {
        return otherUser;
    }

    public void setOtherUser(OtherUserLite otherUser) {
        this.otherUser = otherUser;
    }

    @Override
    public String toString() {
        return "MatchItem{" +
                "_id='" + _id + '\'' +
                ", otherUser=" + (fullOtherUser != null ? fullOtherUser.getId() : "null") +
                '}';
    }

    public User getFullOtherUser() {
        return fullOtherUser;
    }

    public void setFullOtherUser(User fullOtherUser) {
        this.fullOtherUser = fullOtherUser;
    }
}
