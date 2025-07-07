package com.example.prm392_group4_teamproject;

public class Message {
    private String _id;
    private String matchId;
    private User fromUser;
    private String content;
    private String messageType; // text, image, gif
    private boolean isRead;
    private String createdAt;
    // ... getter/setter

    public Message(String _id, String createdAt, String matchId, String content, User fromUser, String messageType, boolean isRead) {
        this._id = _id;
        this.createdAt = createdAt;
        this.matchId = matchId;
        this.content = content;
        this.fromUser = fromUser;
        this.messageType = messageType;
        this.isRead = isRead;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public User getFromUser() {
        return fromUser;
    }

    public void setFromUser(User fromUser) {
        this.fromUser = fromUser;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
