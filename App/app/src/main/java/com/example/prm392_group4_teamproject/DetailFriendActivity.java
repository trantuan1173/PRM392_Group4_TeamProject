package com.example.prm392_group4_teamproject;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.prm392_group4_teamproject.CAPI.ApiClient;
import com.example.prm392_group4_teamproject.CAPI.UserApi;
import com.example.prm392_group4_teamproject.R;
import com.example.prm392_group4_teamproject.model.User;
import com.example.prm392_group4_teamproject.model.UserResponse;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailFriendActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private UserApi userApi;
    private User user, currentUser;

    private CircleImageView avatarImage;
    private TextView nameText, locationText, distanceText, updatedText;
    private Button btnNavigate;

    private String userId, token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_friend_screen);

        avatarImage = findViewById(R.id.avatarImage);
        nameText = findViewById(R.id.nameText);
        locationText = findViewById(R.id.locationtext);
        distanceText = findViewById(R.id.distancetext);
        updatedText = findViewById(R.id.tvUpdatedText);
        btnNavigate = findViewById(R.id.btnNavigate);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        userApi = ApiClient.getClient().create(UserApi.class);

        userId = getIntent().getStringExtra("userId");
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        token = "Bearer " + prefs.getString("auth_token", "");

        if (userId == null || token == null || token.trim().isEmpty()) {
            Toast.makeText(this, "Thiếu token hoặc userId", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchUserById(userId, token);
    }

    private void fetchUserById(String userId, String token) {
        userApi.getUserById(token, userId).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    user = response.body().getUser();
                    updateUI();
                    fetchMyProfileAndInitMap();
                } else {
                    Toast.makeText(DetailFriendActivity.this, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(DetailFriendActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void fetchMyProfileAndInitMap() {
        userApi.getProfile2(token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();
                }
                loadMap();
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                loadMap();
            }
        });
    }

    private void updateUI() {
        nameText.setText(user.getName());
        btnNavigate.setText("Navigate to " + user.getName());

        TextView onlineStatus = findViewById(R.id.onlineStatus);
        if (user.isOnline()) {
            onlineStatus.setText("● Online now");
            onlineStatus.setTextColor(Color.parseColor("#00C853")); // Màu xanh
        } else {
            onlineStatus.setText("● Offline");
            onlineStatus.setTextColor(Color.GRAY);
        }

        btnNavigate.setOnClickListener(v -> {
            if (user.getLocation() != null) {
                Intent intent = new Intent(this, MapFragment.class);
                intent.putExtra("targetLat", user.getLocation().getLatitude());
                intent.putExtra("targetLng", user.getLocation().getLongitude());
                intent.putExtra("targetName", user.getName());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Người này chưa chia sẻ vị trí", Toast.LENGTH_SHORT).show();
            }
        });

        // Avatar
        if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
            avatarImage.setImageResource(R.drawable.girrl);
        } else {
            Glide.with(this).load(user.getAvatar()).into(avatarImage);
        }

        // Vị trí
        if (user.getLocation() != null) {
            String address = getAddressFromLatLng(
                    user.getLocation().getLatitude(),
                    user.getLocation().getLongitude()
            );
            locationText.setText(address);
            updatedText.setText("Updated just now");
        } else {
            locationText.setText("Không rõ vị trí");
        }
    }


    private void loadMap() {
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (user == null || user.getLocation() == null) return;
        LatLng userLatLng = new LatLng(user.getLocation().getLatitude(), user.getLocation().getLongitude());

        // Marker user khác
        if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
            Bitmap avatar = getCircularAvatarBitmapFromDrawable(R.drawable.girrl);
            addMarkerWithAvatar(userLatLng, user.getName(), avatar);
        } else {
            Glide.with(this)
                    .asBitmap()
                    .load(user.getAvatar())
                    .circleCrop()
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            addMarkerWithAvatar(userLatLng, user.getName(), resource);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });
        }

        // Marker bản thân
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    LatLng myLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                    String myAvatar = (currentUser != null) ? currentUser.getAvatar() : null;
                    if (myAvatar == null || myAvatar.isEmpty()) {
                        Bitmap avatar = getCircularAvatarBitmapFromDrawable(R.drawable.girrl);
                        addMarkerWithAvatar(myLatLng, "Bạn", avatar);
                    } else {
                        Glide.with(this)
                                .asBitmap()
                                .load(myAvatar)
                                .circleCrop()
                                .into(new CustomTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                        addMarkerWithAvatar(myLatLng, "Bạn", resource);
                                    }

                                    @Override
                                    public void onLoadCleared(@Nullable Drawable placeholder) {
                                    }
                                });
                    }

                    // Tính khoảng cách
                    float[] results = new float[1];
                    Location.distanceBetween(
                            location.getLatitude(), location.getLongitude(),
                            user.getLocation().getLatitude(), user.getLocation().getLongitude(),
                            results
                    );
                    float distanceInKm = results[0] / 1000f;
                    distanceText.setText(String.format(Locale.getDefault(), "%.1f km away from you", distanceInKm));
                }
            });
        }
    }

    private void addMarkerWithAvatar(LatLng position, String title, Bitmap avatarBitmap) {
        mMap.addMarker(new MarkerOptions()
                .position(position)
                .title(title)
                .icon(BitmapDescriptorFactory.fromBitmap(avatarBitmap)));
        if (!"Bạn".equals(title)) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
        }
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

    private String getAddressFromLatLng(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder sb = new StringBuilder("\uD83D\uDCCD ");
                if (address.getThoroughfare() != null)
                    sb.append(address.getThoroughfare()).append(", ");
                if (address.getSubLocality() != null)
                    sb.append(address.getSubLocality()).append(", ");
                if (address.getLocality() != null)
                    sb.append(address.getLocality()).append(", ");
                if (address.getCountryName() != null)
                    sb.append(address.getCountryName());
                return sb.toString().replaceAll(", $", "");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Không rõ vị trí";
    }
}
