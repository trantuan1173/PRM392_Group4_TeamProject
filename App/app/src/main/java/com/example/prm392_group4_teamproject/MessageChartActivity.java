package com.example.prm392_group4_teamproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.prm392_group4_teamproject.CAPI.ApiClient;
import com.example.prm392_group4_teamproject.CAPI.MessageApi;
import com.example.prm392_group4_teamproject.CAPI.UserApi;
import com.example.prm392_group4_teamproject.Cards.SocketManager;
import com.example.prm392_group4_teamproject.adapters.MessageAdapter;
import com.example.prm392_group4_teamproject.model.Match;
import com.example.prm392_group4_teamproject.model.MatchDetailResponse;
import com.example.prm392_group4_teamproject.model.Message;
import com.example.prm392_group4_teamproject.model.MessageListResponse;
import com.example.prm392_group4_teamproject.model.User;
import com.example.prm392_group4_teamproject.model.UserResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageChartActivity extends AppCompatActivity {
    private ImageView imageAvatar, iconCall, iconVideo, btnSend;
    private TextView textName, textStatus;
    private EditText editMessage;
    private RecyclerView recyclerMessages;

    private MessageAdapter adapter;
    private List<Message> messageList = new ArrayList<>();

    private String matchId;


    private String currentUserId = null;

    private User matchedUser;

    private void loadCurrentUserProfile(String token) {

        UserApi api = ApiClient.getClient().create(UserApi.class);
        Call<UserResponse> call = api.getProfile(token);

        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUserId = response.body().getUser().getId();
                    Log.d("Chart", "currentUserId: " + currentUserId);

                    adapter = new MessageAdapter(messageList, currentUserId);
                    recyclerMessages.setAdapter(adapter);

                    // Tiếp tục các bước
                    loadMatchDetails(token);
                    loadMessages(token);
                    connectSocket(token);
                } else {
                    Toast.makeText(MessageChartActivity.this, "Không lấy được thông tin người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(MessageChartActivity.this, "Lỗi khi lấy profile: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.chat_activity);
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String token = "Bearer " + prefs.getString("auth_token", "");
        Intent intent = getIntent();
        matchId = intent.getStringExtra("matchId");


        // Ánh xạ View
        imageAvatar = findViewById(R.id.imageAvatar);
        textName = findViewById(R.id.textName);
        textStatus = findViewById(R.id.textStatus);
        editMessage = findViewById(R.id.editMessage);
        btnSend = findViewById(R.id.btnSend);
        recyclerMessages = findViewById(R.id.recyclerMessages);

        // RecyclerView
        adapter = new MessageAdapter(messageList, currentUserId);
        recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerMessages.setAdapter(adapter);

        loadCurrentUserProfile(token);

        // Gửi tin nhắn
        btnSend.setOnClickListener(v -> {
            String text = editMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                sendMessageViaSocket(text);
                editMessage.setText("");
            }
        });
    }

    private void connectSocket(String token) {
        SocketManager.getInstance().connect(token);
        SocketManager.getInstance().setOnConnectedListener(() -> {
            SocketManager.getInstance().joinMatch(matchId);
        });

        SocketManager.getInstance().setOnNewMessageListener(message -> runOnUiThread(() -> {
            messageList.add(message);
            adapter.notifyItemInserted(messageList.size() - 1);
            recyclerMessages.scrollToPosition(messageList.size() - 1);
        }));
    }

    private void sendMessageViaSocket(String content) {
        if (SocketManager.getInstance().isConnected()) {
            SocketManager.getInstance().sendMessage(matchId, content);
        } else {
            Toast.makeText(this, "Chưa kết nối socket", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadMatchDetails(String token) {
        UserApi api = ApiClient.getClient().create(UserApi.class);
        Call<MatchDetailResponse> call = api.getMatchById(token, matchId);

        call.enqueue(new Callback<MatchDetailResponse>() {
            @Override
            public void onResponse(Call<MatchDetailResponse> call, Response<MatchDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Match match = response.body().getMatch();
                    User user1 = match.getUser1();
                    User user2 = match.getUser2();

                    matchedUser = user1.getId().equals(currentUserId) ? user2 : user1;

                    textName.setText(matchedUser.getName());
                    textStatus.setText(matchedUser.isOnline() ? "Online now" : "Offline");
                    textStatus.setTextColor(getResources().getColor(
                            matchedUser.isOnline() ? android.R.color.holo_green_dark : android.R.color.darker_gray));

                    String avatarUrl = matchedUser.getAvatar();
                    if (avatarUrl == null || avatarUrl.isEmpty()) {
                        imageAvatar.setImageResource(R.drawable.girrl);
                    } else {
                        Glide.with(MessageChartActivity.this)
                                .load(avatarUrl)
                                .apply(RequestOptions.circleCropTransform())
                                .placeholder(R.drawable.girrl)
                                .into(imageAvatar);
                    }
                } else {
                    Toast.makeText(MessageChartActivity.this, "Không tìm thấy match", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MatchDetailResponse> call, Throwable t) {
                Toast.makeText(MessageChartActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMessages(String token) {
        MessageApi api = ApiClient.getClient().create(MessageApi.class);
        Call<MessageListResponse> call = api.getMessages(token, matchId, 1, 100);

        call.enqueue(new Callback<MessageListResponse>() {
            @Override
            public void onResponse(Call<MessageListResponse> call, Response<MessageListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    messageList.clear();
                    messageList.addAll(response.body().getMessages());
                    adapter.notifyDataSetChanged();
                    recyclerMessages.scrollToPosition(messageList.size() - 1);
                } else {
                    Toast.makeText(MessageChartActivity.this, "Không tải được tin nhắn", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MessageListResponse> call, Throwable t) {
                Toast.makeText(MessageChartActivity.this, "Lỗi tải tin nhắn", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SocketManager.getInstance().reconnectIfNeeded();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //SocketManager.getInstance().disconnect();
    }
}
