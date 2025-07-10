package com.example.prm392_group4_teamproject.adapters;

import android.Manifest;
import android.content.Context;
import android.util.Log;

import androidx.annotation.RequiresPermission;

import com.example.prm392_group4_teamproject.CAPI.ApiClient;
import com.example.prm392_group4_teamproject.CAPI.UserApi;
import com.example.prm392_group4_teamproject.model.ApiResponse;
import com.example.prm392_group4_teamproject.model.LocationUpdate;
import com.google.android.gms.location.FusedLocationProviderClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationHelper {
    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    public static void updateLocationToServer(Context context, FusedLocationProviderClient fusedClient, String token) {
        if (context == null || fusedClient == null || token == null) return;

        fusedClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                String address = "";

                LocationUpdate request = new LocationUpdate(latitude, longitude, address);
                UserApi userApi = ApiClient.getClient().create(UserApi.class);
                Call<ApiResponse> call = userApi.updateLocation(token, request);

                call.enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                        if (!response.isSuccessful()) {
                            Log.e("LocationHelper", "Update failed: " + response.code());
                        } else {
                            Log.e("LocationHelper", "Update not failed: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                        Log.e("LocationHelper", "API error: " + t.getMessage());
                    }
                });
            }
        });
    }
}
