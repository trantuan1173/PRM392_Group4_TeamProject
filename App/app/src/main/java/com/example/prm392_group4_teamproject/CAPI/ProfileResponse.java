package com.example.prm392_group4_teamproject.CAPI;

import com.google.gson.annotations.SerializedName;

public class ProfileResponse {
    @SerializedName("user")
    private User user;

    public User getUser() {
        return user;
    }

    public static class User {
        private String name;
        private String email;
        private String gender;
        private String bio;
        private int age;

        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getGender() { return gender; }
        public String getBio() { return bio; }
        public int getAge() { return age; }
    }

    @Override
    public String toString() {
        return "ProfileResponse{" +
                "user=" + user +
                '}';
    }
}
