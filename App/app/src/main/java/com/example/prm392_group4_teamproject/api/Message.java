package com.example.prm392_group4_teamproject.api;

import com.example.prm392_group4_teamproject.model.User;

public class Message {
    public String _id;
    public String text;
    public String createdAt;
    public User fromUser;

    public Message(String _id, String text, String createdAt, User fromUser) {
        this._id = _id;
        this.text = text;
        this.createdAt = createdAt;
        this.fromUser = fromUser;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public User getFromUser() {
        return fromUser;
    }

    public void setFromUser(User fromUser) {
        this.fromUser = fromUser;
    }
}
