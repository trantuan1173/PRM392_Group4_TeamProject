package com.example.prm392_group4_teamproject.model;

public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String dob; // ISO date string
    private String gender;

    public RegisterRequest(String name, String email, String password, String dob, String gender) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.dob = dob;
        this.gender = gender;
    }
}
