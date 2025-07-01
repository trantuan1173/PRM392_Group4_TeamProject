package com.example.prm392_group4_teamproject.view;

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
import com.example.prm392_group4_teamproject.R;
import com.example.prm392_group4_teamproject.api.ApiClient;
import com.example.prm392_group4_teamproject.api.UserApi;
import com.example.prm392_group4_teamproject.controller.LocationHelper;
import com.example.prm392_group4_teamproject.controller.MatchAdapter;
import com.example.prm392_group4_teamproject.model.MatchItem;
import com.example.prm392_group4_teamproject.model.MatchResponse;
import com.example.prm392_group4_teamproject.model.OtherUserLite;
import com.example.prm392_group4_teamproject.model.User;
import com.example.prm392_group4_teamproject.model.UserResponse;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
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
    private final String token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiI2ODVjZTY5MDUyN2EzYjU0YzRiN2M4ZGEiLCJpYXQiOjE3NTEzMDU4NDIsImV4cCI6MTc1MTkxMDY0Mn0.QYHb2gNk203sV-op-3PavCV742Esgm2-5iKoZmDWAHk";

    private double currentLat = 0;
    private double currentLng = 0;
    private boolean hasLoadedMatches = false;

    private double targetLat = 0;
    private double targetLng = 0;
    private String targetName = null;
    private boolean isNavigateMode = false;


    @SuppressLint("MissingPermission")
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

        targetLat = getIntent().getDoubleExtra("targetLat", 0);
        targetLng = getIntent().getDoubleExtra("targetLng", 0);
        targetName = getIntent().getStringExtra("targetName");
        isNavigateMode = targetLat != 0 && targetLng != 0;

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        LocationHelper.updateLocationToServer(this, fusedLocationClient, token);
    }

    private void loadMatches() {
        UserApi userApi = ApiClient.getClient().create(UserApi.class);
        Call<MatchResponse> call = userApi.getMatches(token, 1, 20);

        call.enqueue(new Callback<MatchResponse>() {
            @Override
            public void onResponse(Call<MatchResponse> call, Response<MatchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    matchList.clear();
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
                    if (user != null && user.isOnline()) { // ✅ chỉ xử lý nếu đang online
                        if (user.getLocation() != null && user.getLocation().getCoordinates() != null) {
                            List<Double> coords = user.getLocation().getCoordinates();
                            if (coords.size() >= 2) {
                                double lng = coords.get(0);
                                double lat = coords.get(1);

                                float[] results = new float[1];
                                Location.distanceBetween(currentLat, currentLng, lat, lng, results);
                                float distanceInMeters = results[0];

                                String distanceText = distanceInMeters < 1000
                                        ? String.format("%dm", (int) distanceInMeters)
                                        : String.format("%.1fkm", distanceInMeters / 1000f);

                                user.setDistanceText(distanceText);

                                LatLng matchLatLng = new LatLng(lat, lng);
                                if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
                                    Bitmap avatarBitmap = getCircularAvatarBitmapFromDrawable(R.drawable.boot);
                                    addMarkerWithAvatar(matchLatLng, user.getName(), avatarBitmap);
                                } else {
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
                                                public void onLoadCleared(@Nullable Drawable placeholder) {
                                                }
                                            });
                                }
                            }
                        }

                        MatchItem item = new MatchItem(user);
                        matchList.add(item);
                        matchAdapter.notifyItemInserted(matchList.size() - 1);
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

                if (location != null) {
                    currentLat = location.getLatitude();
                    currentLng = location.getLongitude();

                    LatLng latLng = new LatLng(currentLat, currentLng);

                    if (userMarker == null) {
                        userMarker = mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title("Bạn đang ở đây")
                                .icon(BitmapDescriptorFactory.fromBitmap(avatarBitmap)));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    } else {
                        userMarker.setPosition(latLng);
                    }

                    if (isNavigateMode) {
                        LatLng targetLatLng = new LatLng(targetLat, targetLng);
                        drawRouteUsingGoogleAPI(latLng, targetLatLng);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(targetLatLng, 14));
                        isNavigateMode = false;
                        return;
                    }

                    if (!hasLoadedMatches) {
                        hasLoadedMatches = true;
                        loadMatches();
                    }
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }
    private void drawRouteUsingGoogleAPI(LatLng origin, LatLng destination) {
        String apiKey = "AIzaSyCsSPp2vj3uEo4wgp2wwkj051CLr04NeFE";
        String url = String.format(
                "https://maps.googleapis.com/maps/api/directions/json?origin=%f,%f&destination=%f,%f&key=%s",
                origin.latitude, origin.longitude, destination.latitude, destination.longitude, apiKey);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                Log.e("RouteAPI", "Route fetch failed: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(MapsActivity.this, "Route fetch failed - vẽ đường thẳng", Toast.LENGTH_SHORT).show();
                    drawStraightLine(origin, destination);  // fallback
                });
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseData);
                        JSONArray routes = json.getJSONArray("routes");
                        if (routes.length() > 0) {
                            JSONObject route = routes.getJSONObject(0);
                            JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                            String encodedPoints = overviewPolyline.getString("points");

                            List<LatLng> points = decodePolyline(encodedPoints);
                            runOnUiThread(() -> mMap.addPolyline(new PolylineOptions()
                                    .addAll(points)
                                    .width(10f)
                                    .color(Color.BLUE)));
                        } else {
                            // Không có route → fallback
                            runOnUiThread(() -> {
                                Toast.makeText(MapsActivity.this, "Không tìm thấy tuyến đường - vẽ đường thẳng", Toast.LENGTH_SHORT).show();
                                drawStraightLine(origin, destination);
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            Toast.makeText(MapsActivity.this, "Phân tích tuyến đường lỗi - vẽ đường thẳng", Toast.LENGTH_SHORT).show();
                            drawStraightLine(origin, destination);
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(MapsActivity.this, "Phản hồi lỗi - vẽ đường thẳng", Toast.LENGTH_SHORT).show();
                        drawStraightLine(origin, destination);
                    });
                }
            }
        });
    }
    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng(lat / 1E5, lng / 1E5);
            poly.add(p);
        }

        return poly;
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
    private void drawStraightLine(LatLng origin, LatLng destination) {
        mMap.addPolyline(new PolylineOptions()
                .add(origin, destination)
                .width(10f)
                .color(Color.BLUE));
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}
