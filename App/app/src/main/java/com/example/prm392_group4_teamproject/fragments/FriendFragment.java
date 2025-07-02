package com.example.prm392_group4_teamproject.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_group4_teamproject.CAPI.ApiClient;
import com.example.prm392_group4_teamproject.CAPI.ApiService;
import com.example.prm392_group4_teamproject.CAPI.FriendUser;
import com.example.prm392_group4_teamproject.CAPI.MatchResponse;
import com.example.prm392_group4_teamproject.FriendDetailActivity;
import com.example.prm392_group4_teamproject.R;
import com.example.prm392_group4_teamproject.adapters.FriendAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendFragment extends Fragment {

    private RecyclerView recyclerView;
    private FriendAdapter adapter;
    private List<FriendUser> friendList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend, container, false);
        recyclerView = view.findViewById(R.id.friendRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new FriendAdapter(friendList, friend -> {
            // Khi click vào bạn bè, chuyển sang FriendDetailActivity
            Intent intent = new Intent(getContext(), FriendDetailActivity.class);
            intent.putExtra("friend_name", friend.getName());
            intent.putExtra("friend_bio", friend.getBio());
            intent.putExtra("friend_avatar", friend.getAvatar());
            intent.putExtra("friend_address", friend.getLocation().getAddress());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        fetchFriends();

        return view;
    }

    private void fetchFriends() {
        ApiService apiService = ApiClient.getClientWithToken(requireContext()).create(ApiService.class);

        apiService.getMatches().enqueue(new Callback<MatchResponse>() {
            @Override
            public void onResponse(Call<MatchResponse> call, Response<MatchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MatchResponse.Match> matches = response.body().getMatches();
                    friendList.clear();

                    // Lấy user hiện tại từ SharedPreferences
                    SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                    String currentUserId = prefs.getString("user_id", "");

                    for (MatchResponse.Match match : matches) {
                        FriendUser user1 = match.getUser1();
                        FriendUser user2 = match.getUser2();

                        if (user1 != null && user2 != null) {
                            if (user1.getId().equals(currentUserId)) {
                                friendList.add(user2);  // user2 là bạn
                            } else if (user2.getId().equals(currentUserId)) {
                                friendList.add(user1);  // user1 là bạn
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Failed to load matches", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MatchResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
