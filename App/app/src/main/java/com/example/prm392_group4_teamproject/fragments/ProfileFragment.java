package com.example.prm392_group4_teamproject.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.prm392_group4_teamproject.CAPI.ApiClient;
import com.example.prm392_group4_teamproject.CAPI.ApiService;
import com.example.prm392_group4_teamproject.CAPI.ProfileRequest;
import com.example.prm392_group4_teamproject.CAPI.ProfileResponse;
import com.example.prm392_group4_teamproject.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.util.Log;

public class ProfileFragment extends Fragment {

    EditText nameEdit, ageEdit, genderEdit, bioEdit;
    Button updateButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        nameEdit = view.findViewById(R.id.editName);
        ageEdit = view.findViewById(R.id.editAge);
        genderEdit = view.findViewById(R.id.editGender);
        bioEdit = view.findViewById(R.id.editBio);
        updateButton = view.findViewById(R.id.btnUpdateProfile);

        getUserProfile(); // load dữ liệu

        updateButton.setOnClickListener(v -> {
            String name = nameEdit.getText().toString();
            int age = Integer.parseInt(ageEdit.getText().toString());
            String gender = genderEdit.getText().toString();
            String bio = bioEdit.getText().toString();

            ProfileRequest request = new ProfileRequest(name, age, gender, bio);

            ApiService service = ApiClient.getClient().create(ApiService.class);
            service.updateProfile(request).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    Toast.makeText(getContext(), "Updated successfully", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        return view;
    }

    private void getUserProfile() {
        ApiService apiService = ApiClient.getClientWithToken(requireContext()).create(ApiService.class);

        apiService.getProfile().enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProfileResponse.User user = response.body().getUser();

                    nameEdit.setText(user.getName());
                    ageEdit.setText(String.valueOf(user.getAge()));
                    genderEdit.setText(user.getGender());
                    bioEdit.setText(user.getBio());

                    Log.d("USER_PROFILE", "Name: " + user.getName() + ", Email: " + user.getEmail());
                }else {
                    try {
                        Log.e("USER_PROFILE", "Response code: " + response.code());
                        Toast.makeText(getContext(), "11111111" + response.code(), Toast.LENGTH_SHORT).show();
                        if (response.errorBody() != null) {
                            Toast.makeText(getContext(), "222222" + response.errorBody().string(), Toast.LENGTH_SHORT).show();

                            Log.e("USER_PROFILE", "Error body: " + response.errorBody().string());
                        }
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "333333" + e, Toast.LENGTH_SHORT).show();
                        Log.e("USER_PROFILE", "Exception when reading error body", e);
                    }

                    Toast.makeText(getContext(), "Failed to load profile (response error)", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                Toast.makeText(getContext(), "API call failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
