package com.example.prm392_group4_teamproject.model;

import com.google.gson.annotations.SerializedName;

public class FriendUser {
    @SerializedName("_id")
    private String id;
    private String name;
    private String email;
    private String bio;
    private String gender;
    private int age;
    private String avatar;
    private Location location;


    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getBio() { return bio; }
    public String getGender() { return gender; }
    public int getAge() { return age; }
    public String getAvatar() { return avatar; }
    public Location getLocation() { return location; }

    public static class Location {
        private String address;
        public String getAddress() { return address; }
    }
}
