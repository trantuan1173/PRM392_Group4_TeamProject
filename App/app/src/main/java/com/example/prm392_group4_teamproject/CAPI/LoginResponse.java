package com.example.prm392_group4_teamproject.CAPI;

public class LoginResponse {
    private String message;
    private String token;
    private ProfileResponse.User user;

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    public ProfileResponse.User getUser() {
        return user;
    }
}
