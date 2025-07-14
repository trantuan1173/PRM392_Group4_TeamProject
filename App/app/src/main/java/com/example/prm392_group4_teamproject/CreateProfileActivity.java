package com.example.prm392_group4_teamproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_group4_teamproject.CAPI.ApiClient;
import com.example.prm392_group4_teamproject.CAPI.ApiService;
import com.example.prm392_group4_teamproject.model.CloudinaryUploader;
import com.example.prm392_group4_teamproject.model.CreateProfileRequest;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateProfileActivity extends AppCompatActivity {

    private EditText inputName, inputBio;
    private Spinner spinnerDay, spinnerMonth, spinnerYear;
    private Button showMen, showWomen, showEveryone, btnContinue, btnPickImage;
    private RangeSlider sliderAgeRange;
    private Slider sliderDistance;
    private TextView ageRangeText, distanceText;

    private String interestedIn = "both"; // default
    private ApiService apiService;
    private String token;

    private Uri selectedImageUri;
    private String uploadedImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_profile_screen);

        inputName = findViewById(R.id.inputName);
        inputBio = findViewById(R.id.inputBio);
        spinnerDay = findViewById(R.id.spinnerDay);
        spinnerMonth = findViewById(R.id.spinnerMonth);
        spinnerYear = findViewById(R.id.spinnerYear);
        showMen = findViewById(R.id.showMen);
        showWomen = findViewById(R.id.showWomen);
        showEveryone = findViewById(R.id.showEveryone);
        sliderAgeRange = findViewById(R.id.sliderAgeRange);
        sliderDistance = findViewById(R.id.sliderDistance);
        ageRangeText = findViewById(R.id.ageRangeText);
        distanceText = findViewById(R.id.distanceText);
        btnContinue = findViewById(R.id.btnContinue);
        btnPickImage = findViewById(R.id.btnAddPhotos); // Button chọn ảnh

        // Setup spinner values
        setupSpinners();

        // Token
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        token = prefs.getString("auth_token", null);

        apiService = ApiClient.getClient().create(ApiService.class);

        setupGenderButtons();
        setupSliders();

        btnContinue.setOnClickListener(v -> submitProfile());

        btnPickImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 1001);
        });
    }

    private void setupSpinners() {
        List<String> days = new ArrayList<>();
        for (int i = 1; i <= 31; i++) days.add(String.valueOf(i));
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        List<String> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int y = currentYear - 18; y >= 1970; y--) years.add(String.valueOf(y));

        spinnerDay.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, days));
        spinnerMonth.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, months));
        spinnerYear.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, years));
    }

    private void setupGenderButtons() {
        showMen.setOnClickListener(v -> {
            interestedIn = "male";
            highlightGender(showMen);
        });
        showWomen.setOnClickListener(v -> {
            interestedIn = "female";
            highlightGender(showWomen);
        });
        showEveryone.setOnClickListener(v -> {
            interestedIn = "both";
            highlightGender(showEveryone);
        });
    }

    private void highlightGender(Button selected) {
        showMen.setAlpha(0.5f);
        showWomen.setAlpha(0.5f);
        showEveryone.setAlpha(0.5f);
        selected.setAlpha(1f);
    }

    private void setupSliders() {
        sliderAgeRange.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = sliderAgeRange.getValues();
            ageRangeText.setText("Age: " + values.get(0).intValue() + " - " + values.get(1).intValue());
        });

        sliderDistance.addOnChangeListener((slider, value, fromUser) -> {
            distanceText.setText("Distance: " + (int) value + " km");
        });
    }

    private void submitProfile() {
        List<Float> ageValues = sliderAgeRange.getValues();
        int minAge = ageValues.get(0).intValue();
        int maxAge = ageValues.get(1).intValue();
        int maxDistance = (int) sliderDistance.getValue();
        String bioText = inputBio.getText().toString().trim();

        if (uploadedImageUrl == null || uploadedImageUrl.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ảnh và chờ upload xong!", Toast.LENGTH_SHORT).show();
            return;
        }

        CreateProfileRequest.AgeRange ageRange = new CreateProfileRequest.AgeRange(minAge, maxAge);
        CreateProfileRequest.Preferences preferences = new CreateProfileRequest.Preferences(ageRange, maxDistance, interestedIn);
        CreateProfileRequest request = new CreateProfileRequest(preferences, bioText, uploadedImageUrl);

        if (token == null) {
            Toast.makeText(this, "No token found", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.createProfile("Bearer " + token, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CreateProfileActivity.this, "Profile created", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(CreateProfileActivity.this, DashboardActivity.class));
                    finish();
                } else {
                    Toast.makeText(CreateProfileActivity.this, "Failed to create profile", Toast.LENGTH_SHORT).show();
                    Log.e("CreateProfile", "Error code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(CreateProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("CreateProfile", "Network error", t);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();

            CloudinaryUploader.uploadImage(this, selectedImageUri, new CloudinaryUploader.UploadCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    uploadedImageUrl = imageUrl;
                    Toast.makeText(CreateProfileActivity.this, "Upload ảnh thành công!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(CreateProfileActivity.this, "Upload thất bại: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}