package com.example.prm392_group4_teamproject.model;

public class CreateProfileRequest {
    private Preferences preferences;
    private String bio;
    private String imageUrl;

    public CreateProfileRequest(Preferences preferences, String bio, String imageUrl) {
        this.preferences = preferences;
        this.bio = bio;
        this.imageUrl = imageUrl;
    }

    public static class Preferences {
        private AgeRange ageRange;
        private int maxDistance;
        private String interestedIn;

        public Preferences(AgeRange ageRange, int maxDistance, String interestedIn) {
            this.ageRange = ageRange;
            this.maxDistance = maxDistance;
            this.interestedIn = interestedIn;
        }
    }

    public static class AgeRange {
        private int min;
        private int max;

        public AgeRange(int min, int max) {
            this.min = min;
            this.max = max;
        }
    }
}