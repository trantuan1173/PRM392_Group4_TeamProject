package com.example.prm392_group4_teamproject.model;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.example.prm392_group4_teamproject.CAPI.CloudinaryService;


import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.*;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;

public class CloudinaryUploader {

    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onFailure(String errorMessage);
    }

    public static void uploadImage(Context context, Uri imageUri, UploadCallback callback) {
        String filePath = getRealPathFromURI(context, imageUri);
        if (filePath == null) {
            callback.onFailure("Invalid file path");
            return;
        }

        File file = new File(filePath);
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
        RequestBody uploadPreset = RequestBody.create(MediaType.parse("text/plain"), "Tinder"); // <- bạn tự tạo trên cloudinary

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.cloudinary.com/v1_1/dvdnw79tk/") // <- đổi đúng cloud_name
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        CloudinaryService service = retrofit.create(CloudinaryService.class);
        Call<CloudinaryResponse> call = service.uploadImage(body, uploadPreset);

        call.enqueue(new Callback<CloudinaryResponse>() {
            @Override
            public void onResponse(Call<CloudinaryResponse> call, Response<CloudinaryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().secureUrl);
                } else {
                    callback.onFailure("Upload failed");
                }
            }

            @Override
            public void onFailure(Call<CloudinaryResponse> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }

    private static String getRealPathFromURI(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) return uri.getPath();
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        String result = cursor.getString(idx);
        cursor.close();
        return result;
    }
}