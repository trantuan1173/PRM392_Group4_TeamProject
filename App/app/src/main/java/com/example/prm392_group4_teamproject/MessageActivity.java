package com.example.prm392_group4_teamproject;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    private RecyclerView rvMessages;
    private MessageListAdapter messageAdapter;
    private final List<MatchItem> matchList = new ArrayList<>();

    private MessageApi messageApi; // 👈 để gọi getMessages

    private String currentUserId = null;

    private RecyclerView rvNewMatches;
    private NewMatchAdapter newMatchAdapter;
    private final List<MatchItem> newMatchList = new ArrayList<>();

    private final String token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiI2ODYxNjFmZjYwZDJjNDk2Mjg3NGI5YWEiLCJpYXQiOjE3NTEzOTMxNzksImV4cCI6MTc1MTk5Nzk3OX0.UCM_vW8H6fnvdmhlvOqJxnXuVuMZrKbuu7Mhh8q7c4E";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.center_message); // layout này chứa messagesRecyclerView

        messageApi = ApiClient.getClient().create(MessageApi.class);


        rvNewMatches = findViewById(R.id.newMatchesRecyclerView);
        rvNewMatches.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        newMatchAdapter = new NewMatchAdapter(this, newMatchList);
        rvNewMatches.setAdapter(newMatchAdapter);

        rvMessages = findViewById(R.id.messagesRecyclerView);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        messageAdapter = new MessageListAdapter(this, matchList);
        rvMessages.setAdapter(messageAdapter);

        loadMatches();
        getCurrentUserAndConnectSocket();
    }

    private void getCurrentUserAndConnectSocket() {
        UserApi userApi = ApiClient.getClient().create(UserApi.class);
        Call<UserResponse> call = userApi.getProfile(token);

        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUserId = response.body().getUser().getId();
                    connectSocket();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e("SocketInit", "Không lấy được user hiện tại");
            }
        });
    }

    private void connectSocket() {
        SocketManager.getInstance().connect(token);
        SocketManager.getInstance().setOnConnectedListener(() -> {
            for (MatchItem match : matchList) {
                SocketManager.getInstance().joinMatch(match.get_id());
            }
        });

        SocketManager.getInstance().setOnNewMessageListener(message -> runOnUiThread(() -> {
            updateLatestMessage(message);
        }));
    }

    private void updateLatestMessage(Message message) {
        String matchId = message.getMatchId();

        for (int i = 0; i < matchList.size(); i++) {
            MatchItem item = matchList.get(i);
            if (item.get_id().equals(matchId)) {
                item.setLastMessage(message.getContent());
                item.setLastMessageTime(message.getCreatedAt());
                messageAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    private void loadMatches() {
        UserApi userApi = ApiClient.getClient().create(UserApi.class);
        Call<MatchResponse> call = userApi.getMatches(token, 1, 50);

        call.enqueue(new Callback<MatchResponse>() {
            @Override
            public void onResponse(@NonNull Call<MatchResponse> call, @NonNull Response<MatchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MatchItem> rawMatches = response.body().getMatches();
                    for (MatchItem item : rawMatches) {
                        OtherUserLite other = item.getOtherUser();
                        String matchId = item.get_id();

                        if (other != null && other.get_id() != null && matchId != null) {
                            // 🔹 Gọi user detail để add vào NewMatchList (luôn hiển thị)
                            loadUserForNewMatch(other.get_id(), matchId);

                            // 🔹 Chỉ gọi checkMessages để add vào MessageList nếu có tin nhắn
                            checkMessagesBeforeAdd(matchId, other.get_id());
                        }
                    }
                } else {
                    Toast.makeText(MessageActivity.this, "Không thể load matches", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MatchResponse> call, @NonNull Throwable t) {
                Toast.makeText(MessageActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserForNewMatch(String userId, String matchId) {
        UserApi userApi = ApiClient.getClient().create(UserApi.class);
        Call<UserResponse> call = userApi.getUserById(token, userId);

        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body().getUser();
                    if (user != null) {
                        MatchItem item = new MatchItem(user);
                        item.set_id(matchId);

                        // Kiểm tra tránh trùng người (nếu cần)
                        if (!containsUser(newMatchList, user.getId())) {
                            newMatchList.add(item);
                            newMatchAdapter.notifyItemInserted(newMatchList.size() - 1);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                Log.e("UserFetchNewMatch", "Lỗi: " + t.getMessage());
            }
        });
    }

    private boolean containsUser(List<MatchItem> list, String userId) {
        for (MatchItem item : list) {
            if (item.getFullOtherUser() != null && item.getFullOtherUser().getId().equals(userId)) {
                return true;
            }
        }
        return false;
    }


    private void checkMessagesBeforeAdd(String matchId, String userId) {
        Call<MessageListResponse> call = messageApi.getMessages(token, matchId, 1, 1); // chỉ cần 1 tin nhắn

        call.enqueue(new Callback<MessageListResponse>() {
            @Override
            public void onResponse(@NonNull Call<MessageListResponse> call, @NonNull Response<MessageListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Message> messages = response.body().getMessages();
                    if (messages != null && !messages.isEmpty()) {
                        // Có ít nhất 1 tin nhắn, gọi tiếp loadUserDetails
                        loadUserDetailsAndAddToList(userId, messages.get(0), matchId);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<MessageListResponse> call, @NonNull Throwable t) {
                Log.e("MessageCheck", "Fail: " + t.getMessage());
            }
        });
    }


    private void loadUserDetailsAndAddToList(String userId, Message lastMessage, String matchId) {
        UserApi userApi = ApiClient.getClient().create(UserApi.class);
        Call<UserResponse> call = userApi.getUserById(token, userId);

        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body().getUser();
                    if (user != null) {
                        MatchItem item = new MatchItem(user);
                        item.setLastMessage(lastMessage.getContent()); // 👈 Gán nội dung cuối

                        item.setLastMessageTime(lastMessage.getCreatedAt()); // 👈 Thêm dòng này
                        item.set_id(matchId);

                        matchList.add(item);
                        messageAdapter.notifyItemInserted(matchList.size() - 1);

                        newMatchList.add(item); // new match vẫn add
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                Log.e("UserFetch", "Lỗi: " + t.getMessage());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //SocketManager.getInstance().reconnectIfNeeded(); // 🔁 đúng class

        SocketManager.getInstance().reconnectIfNeeded();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //SocketManager.getInstance().disconnect();
    }

}
