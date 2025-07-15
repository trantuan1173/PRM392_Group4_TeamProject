package com.example.prm392_group4_teamproject.model;

import java.util.List;

public class PhotoUploadResponse {
    private String message;
    private List<Photo> photos;

    public String getMessage() { return message; }
    public List<Photo> getPhotos() { return photos; }
}