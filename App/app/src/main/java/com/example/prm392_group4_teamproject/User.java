package com.example.prm392_group4_teamproject;

import java.util.List;

public class User {
    private String _id; // 👈 THÊM
    private String name;
    private int age;
    private String gender;
    private String bio;
    private String avatar;
    private List<Photo> photos;
    private LocationData location;

    // ✅ Constructor có đầy đủ tham số
    public User(String _id, String name, int age, String gender, String bio, String avatar, List<Photo> photos, LocationData location) {
        this._id = _id;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.bio = bio;
        this.avatar = avatar;
        this.photos = photos;
        this.location = location;
    }

    public User() {

    }

    // ✅ Getter & Setter cho _id
    public String getId() {
        return _id;
    }

    public void setId(String _id) {
        this._id = _id;
    }

    // Các getter & setter khác vẫn giữ nguyên:
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }

    public LocationData getLocation() {
        return location;
    }

    public void setLocation(LocationData location) {
        this.location = location;
    }
}
