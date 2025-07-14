package com.example.prm392_group4_teamproject.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    private String message;
    private String token;
    private User user;

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }

    public static class User {
        @SerializedName("_id")
        private String id;

        private String name;
        private String email;

        @SerializedName("isUpdated") // đảm bảo nếu JSON trả về là "isUpdated"
        private boolean isUpdated;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }
        public boolean isUpdated() {return isUpdated;}
    }
}
