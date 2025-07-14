package com.example.prm392_group4_teamproject.CAPI;

import com.example.prm392_group4_teamproject.model.CloudinaryResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface CloudinaryService {
    @Multipart
    @POST("image/upload")
    Call<CloudinaryResponse> uploadImage(
            @Part MultipartBody.Part file,
            @Part("upload_preset") RequestBody uploadPreset
    );
}