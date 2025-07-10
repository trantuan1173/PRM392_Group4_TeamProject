package com.example.prm392_group4_teamproject;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_group4_teamproject.CAPI.*;
import com.example.prm392_group4_teamproject.model.ProfileRequest;
import com.example.prm392_group4_teamproject.model.ProfileResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    EditText editName, editAge, editBio, editAddress;
    Spinner genderSpinner;
    Button btnSave;

    String[] genderOptions = {"male", "female", "other"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        editName = findViewById(R.id.editName);
        editAge = findViewById(R.id.editAge);
        editBio = findViewById(R.id.editBio);
        editAddress = findViewById(R.id.editAddress);
        genderSpinner = findViewById(R.id.editGender);
        btnSave = findViewById(R.id.btnSave);

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genderOptions);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(genderAdapter);

        loadProfile();

        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void loadProfile() {
        ApiService service = ApiClient.getClientWithToken(this).create(ApiService.class);
        service.getProfile().enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProfileResponse.User user = response.body().getUser();
                    editName.setText(user.getName());
                    editAge.setText(String.valueOf(user.getAge()));
                    editBio.setText(user.getBio());
                    editAddress.setText(user.getLocation().getAddress());

                    int genderIndex = java.util.Arrays.asList(genderOptions).indexOf(user.getGender());
                    if (genderIndex >= 0) {
                        genderSpinner.setSelection(genderIndex);
                    }
                } else {
                    Toast.makeText(EditProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfile() {
        String name = editName.getText().toString().trim();
        int age = Integer.parseInt(editAge.getText().toString().trim());
        String gender = genderSpinner.getSelectedItem().toString();
        String bio = editBio.getText().toString().trim();
        String address = editAddress.getText().toString().trim();

        ProfileRequest.Location location = new ProfileRequest.Location(address);
        ProfileRequest.Preferences.AgeRange ageRange = new ProfileRequest.Preferences.AgeRange(20, 30); // default
        ProfileRequest.Preferences preferences = new ProfileRequest.Preferences(ageRange, 50, "female"); // default

        ProfileRequest request = new ProfileRequest(name, age, gender, bio, location, preferences);

        ApiService service = ApiClient.getClientWithToken(this).create(ApiService.class);
        service.updateProfile(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EditProfileActivity.this, "Update successful", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Update failed: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
