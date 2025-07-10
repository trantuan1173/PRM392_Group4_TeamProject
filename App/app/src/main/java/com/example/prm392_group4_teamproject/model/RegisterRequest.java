package com.example.prm392_group4_teamproject.model;

public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private int age;
    private String gender;

    public RegisterRequest(String name, String email, String password, int age, String gender) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.age = age;
        this.gender = gender;
    }
}
