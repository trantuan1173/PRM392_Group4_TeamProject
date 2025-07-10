package com.example.prm392_group4_teamproject;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.prm392_group4_teamproject.model.FriendUser;
import com.google.gson.Gson;
import com.bumptech.glide.Glide;

public class FriendDetailActivity extends AppCompatActivity {

    private ImageView avatar;
    private TextView name, bio, address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_detail);

        avatar = findViewById(R.id.detailAvatar);
        name = findViewById(R.id.detailName);
        bio = findViewById(R.id.detailBio);
        address = findViewById(R.id.detailAddress);

        String json = getIntent().getStringExtra("friend");
        FriendUser friend = new Gson().fromJson(json, FriendUser.class);

        name.setText(friend.getName());
        bio.setText(friend.getBio());
        address.setText(friend.getLocation().getAddress());

        Glide.with(this)
                .load(friend.getAvatar())
                .placeholder(R.drawable.user)
                .into(avatar);
    }
}
