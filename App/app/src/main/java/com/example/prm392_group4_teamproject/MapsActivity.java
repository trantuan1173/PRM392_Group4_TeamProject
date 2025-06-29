package com.example.prm392_group4_teamproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Marker userMarker;
    private Bitmap avatarBitmap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private RecyclerView rvNearby;
    private MatchAdapter matchAdapter;
    private List<MatchItem> matchList;
    private final String token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiI2ODVjZTY5MDUyN2EzYjU0YzRiN2M4ZGEiLCJpYXQiOjE3NTEyMTI2NjIsImV4cCI6MTc1MTgxNzQ2Mn0.XJQ6iKbHf8tXudX6Fhr2ES-hPvEdbQQLyZGynMX8FDI"; // Đổi lại token của bạn

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_map_screen);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        avatarBitmap = getCircularAvatarBitmap();

        rvNearby = findViewById(R.id.rvNearby);
        rvNearby.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        matchList = new ArrayList<>();
        matchAdapter = new MatchAdapter(this, matchList);
        rvNearby.setAdapter(matchAdapter);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void loadMatches() {
        UserApi userApi = ApiClient.getClient().create(UserApi.class);
        Call<MatchResponse> call = userApi.getMatches(token, 1, 20);

        call.enqueue(new Callback<MatchResponse>() {
            @Override
            public void onResponse(Call<MatchResponse> call, Response<MatchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    matchList.clear(); // Xoá hết để nạp mới

                    List<MatchItem> rawMatches = response.body().getMatches();
                    for (MatchItem item : rawMatches) {
                        OtherUserLite other = item.getOtherUser();
                        if (other != null && other.get_id() != null) {
                            loadUserDetailsAndAddToList(other.get_id());
                        }
                    }
                } else {
                    Toast.makeText(MapsActivity.this, "Load matches failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MatchResponse> call, Throwable t) {
                Toast.makeText(MapsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserDetailsAndAddToList(String userId) {
        UserApi userApi = ApiClient.getClient().create(UserApi.class);
        Call<UserResponse> call = userApi.getUserById(token, userId);

        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body().getUser();
                    if (user != null) {
                        MatchItem item = new MatchItem(user);
                        matchList.add(item);
                        matchAdapter.notifyItemInserted(matchList.size() - 1);

                        // ✅ Nếu có location
                        if (user.getLocation() != null && user.getLocation().getCoordinates() != null) {
                            List<Double> coords = user.getLocation().getCoordinates();
                            if (coords.size() >= 2) {
                                double lng = coords.get(0);
                                double lat = coords.get(1);
                                LatLng matchLatLng = new LatLng(lat, lng);

                                // ✅ Nếu avatar null → dùng ảnh drawable
                                if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
                                    Bitmap avatarBitmap = getCircularAvatarBitmapFromDrawable(R.drawable.boot);
                                    addMarkerWithAvatar(matchLatLng, user.getName(), avatarBitmap);
                                } else {
                                    // ✅ Load avatar từ URL bằng Glide
                                    Glide.with(MapsActivity.this)
                                            .asBitmap()
                                            .load(user.getAvatar())
                                            .circleCrop()
                                            .into(new CustomTarget<Bitmap>() {
                                                @Override
                                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                    addMarkerWithAvatar(matchLatLng, user.getName(), resource);
                                                }

                                                @Override
                                                public void onLoadCleared(@Nullable Drawable placeholder) { }
                                            });
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e("UserFetch", "Failure: " + t.getMessage());
            }
        });
    }


    private void addMarkerWithAvatar(LatLng position, String title, Bitmap avatar) {
        mMap.addMarker(new MarkerOptions()
                .position(position)
                .title(title != null ? title : "Người dùng")
                .icon(BitmapDescriptorFactory.fromBitmap(avatar)));
    }
    private Bitmap getCircularAvatarBitmapFromDrawable(int drawableResId) {
        Drawable drawable = ContextCompat.getDrawable(this, drawableResId);
        int size = 120;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        BitmapShader shader = new BitmapShader(drawableToBitmap(drawable, size, size),
                Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(shader);
        float radius = size / 2f;
        canvas.drawCircle(radius, radius, radius - 4, paint);

        Paint borderPaint = new Paint();
        borderPaint.setAntiAlias(true);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStrokeWidth(6);
        canvas.drawCircle(radius, radius, radius - 4, borderPaint);

        return bitmap;
    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        checkLocationPermission();
        loadMatches(); // Vẫn load danh sách match
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                Location location = locationResult.getLastLocation();
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                if (userMarker == null) {
                    userMarker = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("Bạn đang ở đây")
                            .icon(BitmapDescriptorFactory.fromBitmap(avatarBitmap)));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                } else {
                    userMarker.setPosition(latLng);
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private Bitmap getCircularAvatarBitmap() {
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.avataaaa);
        int size = 120;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        BitmapShader shader = new BitmapShader(drawableToBitmap(drawable, size, size),
                Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(shader);
        float radius = size / 2f;
        canvas.drawCircle(radius, radius, radius - 4, paint);
        Paint borderPaint = new Paint();
        borderPaint.setAntiAlias(true);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStrokeWidth(6);
        canvas.drawCircle(radius, radius, radius - 4, borderPaint);
        return bitmap;
    }

    private Bitmap drawableToBitmap(Drawable drawable, int width, int height) {
        if (drawable instanceof BitmapDrawable) {
            return Bitmap.createScaledBitmap(((BitmapDrawable) drawable).getBitmap(), width, height, false);
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}
