package com.example.prm392_group4_teamproject.model;

public class MatchItem {
    private String _id;
    private OtherUserLite otherUser;
    private User fullOtherUser;
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
                ", otherUser=" + ( fullOtherUser != null  ? fullOtherUser.getId() : "null") +
                '}';
    }
    public User getFullOtherUser() {
        return fullOtherUser;
    }
    public void setFullOtherUser(User fullOtherUser) {
        this.fullOtherUser = fullOtherUser;
    }
}
