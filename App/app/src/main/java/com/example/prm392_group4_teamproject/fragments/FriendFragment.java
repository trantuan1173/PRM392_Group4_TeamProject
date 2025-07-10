package com.example.prm392_group4_teamproject.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.example.prm392_group4_teamproject.*;
import com.example.prm392_group4_teamproject.CAPI.*;
import com.example.prm392_group4_teamproject.adapters.FriendAdapter;
import com.example.prm392_group4_teamproject.model.FriendUser;
import com.example.prm392_group4_teamproject.model.MatchResponse;
import com.google.gson.Gson;

import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendFragment extends Fragment {

    private RecyclerView recyclerView;
    private FriendAdapter adapter;
    private List<FriendUser> friendList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d("FriendFragment", "onCreateView called");

        View view = inflater.inflate(R.layout.fragment_friend, container, false);
        recyclerView = view.findViewById(R.id.friendRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new FriendAdapter(requireContext(), friendList, friend -> {
            Intent intent = new Intent(requireContext(), DetailFriendActivity.class);
            intent.putExtra("userId", friend.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
        fetchMatches();
        return view;
    }

    private void fetchMatches() {
        Log.d("FriendFragment", "Fetching matches...");

        SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String currentUserId = prefs.getString("user_id", null);

        if (currentUserId == null) {
            Log.e("FriendFragment", "Current user ID is null");
            return;
        }

        ApiService service = ApiClient.getClientWithToken(requireContext()).create(ApiService.class);
        service.getMatches().enqueue(new Callback<MatchResponse>() {
            @Override
            public void onResponse(Call<MatchResponse> call, Response<MatchResponse> response) {
                Log.d("FriendFragment", "API Response received");
                if (response.isSuccessful() && response.body() != null) {
                    friendList.clear();

                    for (MatchResponse.Match match : response.body().getMatches()) {
                        FriendUser friend;
                        Log.d("FriendFragment", "Match: user1=" + match.getUser1().getId() + ", user2=" + match.getUser2().getId());

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
                    Log.d("FriendFragment", "Friend list updated: " + friendList.size() + " items");
                } else {
                    Log.e("FriendFragment", "API error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<MatchResponse> call, Throwable t) {
                Log.e("FriendFragment", "API call failed: " + t.getMessage());
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
// code dep roi nhung ma backend dang sai nha pro oi
//user lay cung 1 nguoi dung nen tam thoi t de nhu nay sua lai backend la chay duoc