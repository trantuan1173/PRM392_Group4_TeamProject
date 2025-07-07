package com.example.prm392_group4_teamproject.model;

public class OtherUserLite {
    private String _id;
    private String name;
    private String avatar;

    public OtherUserLite() {}

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
