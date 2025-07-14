package com.example.prm392_group4_teamproject.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;

import com.example.prm392_group4_teamproject.CAPI.ApiClient;
import com.example.prm392_group4_teamproject.CAPI.ApiService;
import com.example.prm392_group4_teamproject.model.ProfileResponse;
import com.example.prm392_group4_teamproject.EditProfileActivity;
import com.example.prm392_group4_teamproject.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private TextView tvName, tvEmail, tvAge, tvGender, tvBio, tvAddress, tvPreferences;
    private ImageView avatarImageView;
    private Button btnEditProfile;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvAge = view.findViewById(R.id.tvAge);
        tvGender = view.findViewById(R.id.tvGender);
        tvBio = view.findViewById(R.id.tvBio);
        tvAddress = view.findViewById(R.id.tvAddress);
        tvPreferences = view.findViewById(R.id.tvPreferences);
        avatarImageView = view.findViewById(R.id.avatarImageView);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        Button btnLogout = view.findViewById(R.id.btnLogout);

        getUserProfile();

        btnEditProfile.setOnClickListener(v -> {
            // Chuyển sang màn hình Edit Profile
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            // Xóa token và chuyển về màn hình đăng nhập
            requireActivity().getSharedPreferences("MyPrefs", getContext().MODE_PRIVATE)
                    .edit()
                    .remove("auth_token")
                    .apply();

            Intent intent = new Intent(getActivity(), com.example.prm392_group4_teamproject.LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
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

                    tvName.setText(user.getName());
                    tvEmail.setText(user.getEmail());
                    tvAge.setText(String.valueOf(user.getAge()));
                    tvGender.setText(user.getGender());
                    tvBio.setText(user.getBio());
                    tvAddress.setText(user.getLocation().getAddress());
                    tvPreferences.setText(user.getPreferences().getInterestedIn());

                    Glide.with(requireContext())
                            .load(user.getAvatar())
                            .placeholder(R.drawable.user)
                            .into(avatarImageView);

                } else {
                    Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }}


