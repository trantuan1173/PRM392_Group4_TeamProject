package com.example.prm392_group4_teamproject;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.prm392_group4_teamproject.ApiClient;
import com.example.prm392_group4_teamproject.UserApi;
import com.example.prm392_group4_teamproject.ApiResponse;
import com.example.prm392_group4_teamproject.LocationUpdate;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationPermisstionActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private FusedLocationProviderClient fusedLocationClient;
    private Button allowButton;

    private String token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiI2ODVjZTc4NjUyN2EzYjU0YzRiN2M4ZTAiLCJpYXQiOjE3NTEyMTA4MzIsImV4cCI6MTc1MTgxNTYzMn0.8QQAbuTNNdhBdQxQKGXy6w0JNbIZYSDhIMNqb-1_t8w"; // TODO: Thay bằng token thực tế (JWT hoặc từ SharedPreferences)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_permission);

        allowButton = findViewById(R.id.allowButton);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        allowButton.setOnClickListener(v -> {
            if (hasLocationPermission()) {
                getCurrentLocationAndUpdate();
            } else {
                requestLocationPermission();
            }
        });
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE
        );
    }

    private void getCurrentLocationAndUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                sendLocationToServer(location);
            } else {
                Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendLocationToServer(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        String address = ""; // Optional, bạn có thể dùng reverse geocoding nếu muốn

        LocationUpdate request = new LocationUpdate(latitude, longitude, address);
        UserApi userApi = ApiClient.getClient().create(UserApi.class);
        Call<ApiResponse> call = userApi.updateLocation(token, request);

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(LocationPermisstionActivity.this, "Location updated!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LocationPermisstionActivity.this, MapsActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Toast.makeText(LocationPermisstionActivity.this, "Failed: " + errorBody, Toast.LENGTH_LONG).show();
                        Log.e("API_ERROR", "Code: " + response.code() + " - " + errorBody);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }


            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(LocationPermisstionActivity.this, "API error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Khi người dùng bấm "Cho phép" hoặc "Từ chối"
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocationAndUpdate();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
