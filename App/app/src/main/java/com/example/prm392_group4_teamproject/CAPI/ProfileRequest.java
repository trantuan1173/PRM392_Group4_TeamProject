package com.example.prm392_group4_teamproject.CAPI;

public class ProfileRequest {
    private String name;
    private int age;
    private String gender;
    private String bio;

    public ProfileRequest(String name, int age, String gender, String bio) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.bio = bio;
    }

    // Nếu bạn cần sử dụng Gson cho getter/setter:
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
}
