package com.example.prm392_group4_teamproject.Cards;

public class cards {
    private String userId;
    private String name;
    private String imageUrl;

    public cards(String userId, String name, String imageUrl) {
        this.userId = userId;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}