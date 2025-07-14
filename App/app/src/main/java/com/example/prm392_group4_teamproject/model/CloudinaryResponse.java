package com.example.prm392_group4_teamproject.model;

import com.google.gson.annotations.SerializedName;

public class CloudinaryResponse {

    @SerializedName("secure_url")
    public String secureUrl;

    // Optional: thêm các field khác nếu bạn cần
    @SerializedName("public_id")
    public String publicId;

    @SerializedName("width")
    public int width;

    @SerializedName("height")
    public int height;
}