package com.example.prm392_group4_teamproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_group4_teamproject.CAPI.*;
import com.example.prm392_group4_teamproject.adapters.FriendAdapter;
import com.example.prm392_group4_teamproject.model.FriendUser;
import com.example.prm392_group4_teamproject.model.MatchResponse;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FriendAdapter adapter;
    private List<FriendUser> friendList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_friend);

        recyclerView = findViewById(R.id.friendRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new FriendAdapter(this, friendList, friend -> {
            Intent intent = new Intent(this, DetailFriendActivity.class);
            intent.putExtra("userId", friend.getId());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
        fetchMatches();
    }

    private void fetchMatches() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String currentUserId = prefs.getString("user_id", null);

        if (currentUserId == null) {
            Log.e("FriendActivity", "Current user ID is null");
            return;
        }

        ApiService service = ApiClient.getClientWithToken(this).create(ApiService.class);
        service.getMatches().enqueue(new Callback<MatchResponse>() {
            @Override
            public void onResponse(Call<MatchResponse> call, Response<MatchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    friendList.clear();

                    for (MatchResponse.Match match : response.body().getMatches()) {
                        FriendUser friend;

                        if (match.getUser1().getId().equals(currentUserId)) {
                            friend = match.getUser2();
                        } else if (match.getUser2().getId().equals(currentUserId)) {
                            friend = match.getUser1();
                        } else {
                            continue;
                        }

                        friendList.add(friend);
                    }

                    adapter.notifyDataSetChanged();
                } else {
                    Log.e("FriendActivity", "API error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<MatchResponse> call, Throwable t) {
                Toast.makeText(FriendsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
