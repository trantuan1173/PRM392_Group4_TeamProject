package com.example.prm392_group4_teamproject.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.prm392_group4_teamproject.CAPI.ApiClient;
import com.example.prm392_group4_teamproject.CAPI.UserApi;
import com.example.prm392_group4_teamproject.FriendsActivity;
import com.example.prm392_group4_teamproject.R;
import com.example.prm392_group4_teamproject.adapters.LocationHelper;
import com.example.prm392_group4_teamproject.adapters.MatchAdapter;
import com.example.prm392_group4_teamproject.model.*;

import com.google.android.gms.location.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import org.json.*;

import java.io.IOException;
import java.util.*;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Marker userMarker;
    private Bitmap avatarBitmap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private RecyclerView rvNearby;
    private MatchAdapter matchAdapter;
    private List<MatchItem> matchList = new ArrayList<>();
    private String token = "";
    private double currentLat = 0, currentLng = 0;
    private boolean hasLoadedMatches = false;

    private SupportMapFragment mapFragment;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvSeeAll = view.findViewById(R.id.tvSeeAll);
        tvSeeAll.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), FriendsActivity.class);
            startActivity(intent);
        });
    }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.friend_map_screen, container, false);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        fetchMyProfileAvatar();
        mapFragment.getMapAsync(this);

        SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        token = "Bearer " + prefs.getString("auth_token", "");

        rvNearby = view.findViewById(R.id.rvNearby);
        rvNearby.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
        matchAdapter = new MatchAdapter(requireContext(), matchList);
        rvNearby.setAdapter(matchAdapter);

        // Thêm map fragment thủ công vào FrameLayout
        mapFragment = SupportMapFragment.newInstance();
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.mapContainer, mapFragment)
                .commit();

        mapFragment.getMapAsync(this);


        LocationHelper.updateLocationToServer(requireContext(), fusedLocationClient, token);

        return view;
    }

    private void fetchMyProfileAvatar() {
        UserApi userApi = ApiClient.getClient().create(UserApi.class);
        userApi.getProfile2(token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String myAvatar = response.body().getAvatar();
                    if (myAvatar != null && !myAvatar.isEmpty()) {
                        Glide.with(MapsFragment.this)
                                .asBitmap()
                                .load(myAvatar)
                                .circleCrop()
                                .into(new CustomTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                        avatarBitmap = resource;
                                    }

                                    @Override
                                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                                });
                    } else {
                        avatarBitmap = getCircularAvatarBitmap(); // fallback
                    }
                } else {
                    avatarBitmap = getCircularAvatarBitmap(); // fallback
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                avatarBitmap = getCircularAvatarBitmap(); // fallback
            }
        });
    }

    private Bitmap getCircularAvatarBitmap() {
        Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.girrl);// data user
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
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
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

                if (!hasLoadedMatches) {
                    hasLoadedMatches = true;
                    loadMatches();
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void loadMatches() {
        UserApi userApi = ApiClient.getClient().create(UserApi.class);
        Call<MatchMapsResponse> call = userApi.getMatches(token, 1, 20);
        call.enqueue(new Callback<MatchMapsResponse>() {
            @Override
            public void onResponse(Call<MatchMapsResponse> call, Response<MatchMapsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    matchList.clear();
                    for (MatchItem item : response.body().getMatches()) {
                        if (item.getOtherUser() != null) {
                            loadUserDetailsAndAddToList(item.getOtherUser().get_id());
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<MatchMapsResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
    private void drawStraightLine(LatLng origin, LatLng destination) {
        mMap.addPolyline(new PolylineOptions()
                .add(origin, destination)
                .width(10f)
                .color(Color.BLUE));
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
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Route fetch failed - vẽ đường thẳng", Toast.LENGTH_SHORT).show();
                    drawStraightLine(origin, destination); // fallback
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
                            requireActivity().runOnUiThread(() -> mMap.addPolyline(new PolylineOptions()
                                    .addAll(points)
                                    .width(10f)
                                    .color(Color.BLUE)));
                        } else {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Không tìm thấy tuyến đường - vẽ đường thẳng", Toast.LENGTH_SHORT).show();
                                drawStraightLine(origin, destination);
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Phân tích tuyến đường lỗi - vẽ đường thẳng", Toast.LENGTH_SHORT).show();
                            drawStraightLine(origin, destination);
                        });
                    }
                } else {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Phản hồi lỗi - vẽ đường thẳng", Toast.LENGTH_SHORT).show();
                        drawStraightLine(origin, destination);
                    });
                }
            }
        });
    }

    private Bitmap getCircularAvatarBitmapFromDrawable(int drawableResId) {
        Drawable drawable = ContextCompat.getDrawable(requireContext(), drawableResId);
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
    private void addMarkerWithAvatar(LatLng position, String title, Bitmap avatar) {
        mMap.addMarker(new MarkerOptions()
                .position(position)
                .title(title != null ? title : "Người dùng")
                .icon(BitmapDescriptorFactory.fromBitmap(avatar)));
    }

    private void loadUserDetailsAndAddToList(String userId) {
        UserApi userApi = ApiClient.getClient().create(UserApi.class);
        Call<UserResponse> call = userApi.getUserById(token, userId);

        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body().getUser();
                    if (user != null && user.isOnline()) { //  chỉ xử lý nếu đang online
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
                                    Bitmap avatarBitmap = getCircularAvatarBitmapFromDrawable(R.drawable.boot);// data người dùng
                                    addMarkerWithAvatar(matchLatLng, user.getName(), avatarBitmap);
                                } else {
                                    Glide.with(MapsFragment.this)
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
                        Collections.sort(matchList, new NearbyUserDistanceComparator());
                        if (matchList.size() > 3) {
                            matchList = new ArrayList<>(matchList.subList(0, 3));
                        }
                        matchAdapter = new MatchAdapter(requireContext(), matchList);
                        rvNearby.setAdapter(matchAdapter);

                    }

                }
            }


            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e("UserFetch", "Failure: " + t.getMessage());
            }
        });
    }
    class NearbyUserDistanceComparator implements Comparator<MatchItem> {
        @Override
        public int compare(MatchItem o1, MatchItem o2) {
            try {
                float d1 = parseDistance(o1.getFullOtherUser().getDistanceText());
                float d2 = parseDistance(o2.getFullOtherUser().getDistanceText());
                return Float.compare(d1, d2);
            } catch (Exception e) {
                return 0;
            }
        }

        private float parseDistance(String distanceText) {
            if (distanceText == null) return Float.MAX_VALUE;
            if (distanceText.contains("km")) {
                return Float.parseFloat(distanceText.replace("km", "").trim()) * 1000;
            } else if (distanceText.contains("m")) {
                return Float.parseFloat(distanceText.replace("m", "").trim());
            }
            return Float.MAX_VALUE;
        }
    }


}
