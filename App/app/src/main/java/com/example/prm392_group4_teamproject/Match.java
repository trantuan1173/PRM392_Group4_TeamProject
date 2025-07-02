package com.example.prm392_group4_teamproject;

import java.util.Date;
import java.util.List;

public class Match {
    private String _id;
    private User user1;
    private User user2;

    public Match(String _id, User user1, User user2) {
        this._id = _id;
        this.user1 = user1;
        this.user2 = user2;
    }

    public String get_id() {
        return _id;
    }

    public User getUser1() {
        return user1;
    }

    public void setUser1(User user1) {
        this.user1 = user1;
    }

    public User getUser2() {
        return user2;
    }

    public void setUser2(User user2) {
        this.user2 = user2;
    }
}