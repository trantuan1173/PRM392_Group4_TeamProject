package com.example.prm392_group4_teamproject;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class FriendDetailActivity extends AppCompatActivity {

    private ImageView avatarImageView;
    private TextView nameTextView, bioTextView, addressTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_detail);

        avatarImageView = findViewById(R.id.detailAvatar);
        nameTextView = findViewById(R.id.detailName);
        bioTextView = findViewById(R.id.detailBio);
        addressTextView = findViewById(R.id.detailAddress);

        // Lấy dữ liệu từ Intent
        String name = getIntent().getStringExtra("friend_name");
        String bio = getIntent().getStringExtra("friend_bio");
        String avatar = getIntent().getStringExtra("friend_avatar");
        String address = getIntent().getStringExtra("friend_address");

        nameTextView.setText(name);
        bioTextView.setText(bio);
        addressTextView.setText(address);

        Glide.with(this)
                .load(avatar)
                .placeholder(R.drawable.user)
                .into(avatarImageView);
    }
}
