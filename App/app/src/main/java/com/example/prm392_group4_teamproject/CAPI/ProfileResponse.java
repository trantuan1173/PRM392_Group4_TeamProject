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
        private String avatar;
        private Location location;
        private Preferences preferences;

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public String getGender() {
            return gender;
        }

        public String getBio() {
            return bio;
        }

        public int getAge() {
            return age;
        }

        public String getAvatar() {
            return avatar;
        }

        public Location getLocation() {
            return location;
        }


        public Preferences getPreferences() {
            return preferences;
        }

        public static class Location {
            private String address;

            public String getAddress() {
                return address;
            }
        }

        public static class Preferences {
            private int maxDistance;
            private AgeRange ageRange;
            private String interestedIn;

            public String getInterestedIn() {
                return interestedIn;
            }

            public static class AgeRange {
                private int min;
                private int max;
            }
        }
    }
}