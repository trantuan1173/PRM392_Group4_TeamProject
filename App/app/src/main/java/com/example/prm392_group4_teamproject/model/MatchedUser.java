package com.example.prm392_group4_teamproject.model;

public class MatchedUser {
    private String _id;
    private String name;
    private String email;
    private int age;
    private String gender;
    private String bio;
    private String avatar;
    private Location location;

    public String getId() {
        return _id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public String getBio() {
        return bio;
    }

    public String getAvatar() {
        return avatar;
    }

    public Location getLocation() {
        return location;
    }

    public static class Location {
        private String address;

        public String getAddress() {
            return address;
        }
    }
}
