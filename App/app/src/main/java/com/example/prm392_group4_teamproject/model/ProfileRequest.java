package com.example.prm392_group4_teamproject.model;

public class ProfileRequest {
    private String name;
    private int age;
    private String gender;
    private String bio;
    private Location location;
    private Preferences preferences;

    public ProfileRequest(String name, int age, String gender, String bio, Location location, Preferences preferences) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.bio = bio;
        this.location = location;
        this.preferences = preferences;
    }

    public static class Location {
        private String address;

        public Location(String address) {
            this.address = address;
        }
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

        public static class AgeRange {
            private int min;
            private int max;

            public AgeRange(int min, int max) {
                this.min = min;
                this.max = max;
            }
        }
    }
}
