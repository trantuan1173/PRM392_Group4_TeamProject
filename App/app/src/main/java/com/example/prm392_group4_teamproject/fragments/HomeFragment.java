//package com.example.prm392_group4_teamproject.fragments;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.ImageView;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//
//import com.example.prm392_group4_teamproject.Cards.arrayAdapter;
//import com.example.prm392_group4_teamproject.Cards.cards;
//import com.example.prm392_group4_teamproject.MessageActivity;
//import com.example.prm392_group4_teamproject.R;
//import com.yuyakaido.android.cardstackview.*;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import android.content.SharedPreferences;
//import android.preference.PreferenceManager;
//import com.example.prm392_group4_teamproject.model.DiscoverResponse;
//import com.example.prm392_group4_teamproject.model.DiscoverUser;
//import com.example.prm392_group4_teamproject.CAPI.ApiClient;
//import com.example.prm392_group4_teamproject.CAPI.ApiService;
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
//public class HomeFragment extends Fragment {
//
//    private CardStackView cardStackView;
//    private CardStackLayoutManager manager;
//    private arrayAdapter adapter;
//
//    public HomeFragment() {
//        // Required empty public constructor
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//
//        return inflater.inflate(R.layout.swipe_screen, container, false);
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        cardStackView = view.findViewById(R.id.card_stack_view);
//        manager = new CardStackLayoutManager(getContext(), new CardStackListener() {
//            @Override
//            public void onCardDragging(Direction direction, float ratio) { }
//
//            @Override
//            public void onCardSwiped(Direction direction) {
//                // Vuốt xong
//            }
//
//            @Override
//            public void onCardRewound() { }
//
//            @Override
//            public void onCardCanceled() { }
//
//            @Override
//            public void onCardAppeared(View view, int position) { }
//
//            @Override
//            public void onCardDisappeared(View view, int position) { }
//        });
//
//        cardStackView.setLayoutManager(manager);
//
//        List<cards> userList = new ArrayList<>();
//        userList.add(new cards("1", "Tuấn", "https://i.imgur.com/Xtq3M7G.jpeg"));
//        userList.add(new cards("2", "Trang", "https://i.imgur.com/fz4BMtW.jpeg"));
//        userList.add(new cards("3", "Minh", "https://i.imgur.com/K6fW3Ou.jpeg"));
//
//        adapter = new arrayAdapter(userList, getContext());
//        cardStackView.setAdapter(adapter);
//        ImageView ivChat = view.findViewById(R.id.ivChat);
//        ivChat.setOnClickListener(v -> {
//            Intent intent = new Intent(getContext(), MessageActivity.class);
//            startActivity(intent);
//        });
//
//
//    }
//}
//
//package com.example.prm392_group4_teamproject.fragments;
//
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//
//import com.example.prm392_group4_teamproject.CAPI.ApiClient;
//import com.example.prm392_group4_teamproject.CAPI.ApiService;
//import com.example.prm392_group4_teamproject.Cards.arrayAdapter;
//import com.example.prm392_group4_teamproject.Cards.cards;
//import com.example.prm392_group4_teamproject.MessageActivity;
//import com.example.prm392_group4_teamproject.R;
//import com.example.prm392_group4_teamproject.model.DiscoverResponse;
//import com.example.prm392_group4_teamproject.model.DiscoverUser;
//import com.yuyakaido.android.cardstackview.*;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
//public class HomeFragment extends Fragment {
//
//    private CardStackView cardStackView;
//    private CardStackLayoutManager manager;
//    private arrayAdapter adapter;
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.swipe_screen, container, false);
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        cardStackView = view.findViewById(R.id.card_stack_view);
//        manager = new CardStackLayoutManager(getContext(), new CardStackListener() {
//            @Override public void onCardDragging(Direction direction, float ratio) {}
//            @Override public void onCardSwiped(Direction direction) {
//                int position = manager.getTopPosition() - 1;
//                if (position >= 0 && position < adapter.getItemCount()) {
//                    cards swipedUser = adapter.getItem(position);
//                    boolean isLike = direction == Direction.Right;
//                    sendSwipeRequest(swipedUser.getId(), isLike);
//                }
//            }
//            @Override public void onCardRewound() {}
//            @Override public void onCardCanceled() {}
//            @Override public void onCardAppeared(View view, int position) {}
//            @Override public void onCardDisappeared(View view, int position) {}
//        });
//
//        cardStackView.setLayoutManager(manager);
//
//        SharedPreferences prefs = getContext().getSharedPreferences("MyPrefs", getContext().MODE_PRIVATE);
//        String token = prefs.getString("auth_token", null);
//
//        ApiService apiService = ApiClient.getClient().create(ApiService.class);
//        Call<DiscoverResponse> call = apiService.getDiscoverUsers("Bearer " + token, 1, 10);
//
//        call.enqueue(new Callback<DiscoverResponse>() {
//            @Override
//            public void onResponse(Call<DiscoverResponse> call, Response<DiscoverResponse> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    List<DiscoverUser> discoverUsers = response.body().getUsers();
//                    List<cards> userCards = new ArrayList<>();
//
//                    for (DiscoverUser user : discoverUsers) {
//                        String photoUrl = null;
//
//                        for (DiscoverUser.Photo photo : user.getPhotos()) {
//                            if (photo.isMain()) {
//                                photoUrl = photo.getUrl();
//                                break;
//                            }
//                        }
//
//                        if (photoUrl == null && !user.getPhotos().isEmpty()) {
//                            photoUrl = user.getPhotos().get(0).getUrl();
//                        }
//
//                        userCards.add(new cards(user.getId(), user.getName(), photoUrl));
//                    }
//
//                    adapter = new arrayAdapter(userCards, getContext());
//                    cardStackView.setAdapter(adapter);
//                } else {
//                    Toast.makeText(getContext(), "Failed to fetch users", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<DiscoverResponse> call, Throwable t) {
//                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        ImageView ivChat = view.findViewById(R.id.ivChat);
//        ivChat.setOnClickListener(v -> {
//            Intent intent = new Intent(getContext(), MessageActivity.class);
//            startActivity(intent);
//        });
//    }
//    private void sendSwipeRequest(String toUserId, boolean isLike) {
//        SharedPreferences prefs = getContext().getSharedPreferences("MyPrefs", getContext().MODE_PRIVATE);
//        String token = prefs.getString("auth_token", null);
//
//        ApiService apiService = ApiClient.getClient().create(ApiService.class);
//
//        java.util.Map<String, Object> body = new java.util.HashMap<>();
//        body.put("toUserId", toUserId);
//        body.put("isLike", isLike);
//
//        apiService.createSwipe("Bearer " + token, body).enqueue(new Callback<Void>() {
//            @Override
//            public void onResponse(Call<Void> call, Response<Void> response) {
//                if (response.isSuccessful()) {
//                    Toast.makeText(getContext(), isLike ? "Liked ❤️" : "Passed ❌", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(getContext(), "Swipe failed: " + response.code(), Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<Void> call, Throwable t) {
//                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//}


package com.example.prm392_group4_teamproject.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.prm392_group4_teamproject.CAPI.ApiClient;
import com.example.prm392_group4_teamproject.CAPI.ApiService;
import com.example.prm392_group4_teamproject.Cards.arrayAdapter;
import com.example.prm392_group4_teamproject.Cards.cards;
import com.example.prm392_group4_teamproject.MessageActivity;
import com.example.prm392_group4_teamproject.R;
import com.example.prm392_group4_teamproject.model.DiscoverResponse;
import com.example.prm392_group4_teamproject.model.DiscoverUser;
import com.yuyakaido.android.cardstackview.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private CardStackView cardStackView;
    private CardStackLayoutManager manager;
    private arrayAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.swipe_screen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cardStackView = view.findViewById(R.id.card_stack_view);

        manager = new CardStackLayoutManager(getContext(), new CardStackListener() {
            @Override
            public void onCardDragging(Direction direction, float ratio) {}

            @Override
            public void onCardSwiped(Direction direction) {
                int position = manager.getTopPosition() - 1;
                if (adapter != null && position >= 0 && position < adapter.getItemCount()) {
                    cards swipedUser = adapter.getItem(position);
                    boolean isLike = direction == Direction.Right;
                    sendSwipeRequest(swipedUser.getUserId(), isLike);
                }
            }

            @Override public void onCardRewound() {}
            @Override public void onCardCanceled() {}
            @Override public void onCardAppeared(View view, int position) {}
            @Override public void onCardDisappeared(View view, int position) {}
        });

        cardStackView.setLayoutManager(manager);

        loadDiscoverUsers();

        ImageView ivChat = view.findViewById(R.id.ivChat);
        ivChat.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MessageActivity.class);
            startActivity(intent);
        });
    }

    private void loadDiscoverUsers() {
        SharedPreferences prefs = getContext().getSharedPreferences("MyPrefs", getContext().MODE_PRIVATE);
        String token = prefs.getString("auth_token", null);

        if (token == null) {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<DiscoverResponse> call = apiService.getDiscoverUsers("Bearer " + token, 1, 10);

        call.enqueue(new Callback<DiscoverResponse>() {
            @Override
            public void onResponse(Call<DiscoverResponse> call, Response<DiscoverResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<DiscoverUser> discoverUsers = response.body().getUsers();
                    List<cards> userCards = new ArrayList<>();

                    for (DiscoverUser user : discoverUsers) {
                        String photoUrl = null;

                        for (DiscoverUser.Photo photo : user.getPhotos()) {
                            if (photo.isMain()) {
                                photoUrl = photo.getUrl();
                                break;
                            }
                        }

                        if (photoUrl == null && !user.getPhotos().isEmpty()) {
                            photoUrl = user.getPhotos().get(0).getUrl();
                        }

                        userCards.add(new cards(user.getId(), user.getName(), photoUrl));
                    }

                    adapter = new arrayAdapter(userCards, getContext());
                    cardStackView.setAdapter(adapter);
                } else {
                    Toast.makeText(getContext(), "Failed to fetch users", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DiscoverResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showMatchDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_match, null);
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        dialogView.findViewById(R.id.btnCloseMatch).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void sendSwipeRequest(String toUserId, boolean isLike) {
        SharedPreferences prefs = getContext().getSharedPreferences("MyPrefs", getContext().MODE_PRIVATE);
        String token = prefs.getString("auth_token", null);

        if (token == null) return;

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        Map<String, Object> body = new HashMap<>();
        body.put("toUserId", toUserId);
        body.put("isLike", isLike);

        apiService.createSwipe("Bearer " + token, body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
//                    Toast.makeText(getContext(), isLike ? "❤️ Liked" : "❌ Passed", Toast.LENGTH_SHORT).show();
                    if (isLike && response.code() == 201) {
                        boolean isMatch = response.headers().get("X-Match") != null;
                        if (isMatch) {
                            showMatchDialog();
                        } else {
                            Toast.makeText(getContext(), "❤️ Liked", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "❌ Passed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Swipe failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Swipe error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}