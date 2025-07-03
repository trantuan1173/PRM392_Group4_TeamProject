package com.example.prm392_group4_teamproject.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.example.prm392_group4_teamproject.CAPI.ApiClient;
import com.example.prm392_group4_teamproject.CAPI.ApiService;
import com.example.prm392_group4_teamproject.CAPI.NotificationItem;
import com.example.prm392_group4_teamproject.CAPI.NotificationResponse;
import com.example.prm392_group4_teamproject.R;
import com.example.prm392_group4_teamproject.adapters.NotificationAdapter;

import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView noNotificationText;
    private NotificationAdapter adapter;
    private List<NotificationItem> notificationList = new ArrayList<>();
    private static final String TAG = "NotificationFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        recyclerView = view.findViewById(R.id.notificationRecyclerView);
        noNotificationText = view.findViewById(R.id.noNotificationText); // TextView hiển thị khi rỗng

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(adapter);

        loadNotifications();

        return view;
    }

    private void loadNotifications() {
        ApiService service = ApiClient.getClientWithToken(requireContext()).create(ApiService.class);
        service.getNotifications().enqueue(new Callback<NotificationResponse>() {
            @Override
            public void onResponse(Call<NotificationResponse> call, Response<NotificationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    notificationList.clear();
                    notificationList.addAll(response.body().getNotifications());
                    adapter.notifyDataSetChanged();

                    if (notificationList.isEmpty()) {
                        noNotificationText.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        noNotificationText.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }

                    Log.d(TAG, "Loaded " + notificationList.size() + " notifications.");
                } else {
                    Log.e(TAG, "Failed to load notifications: " + response.code());
                    Toast.makeText(getContext(), "Không thể tải thông báo", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<NotificationResponse> call, Throwable t) {
                Log.e(TAG, "Error loading notifications", t);
                Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}


//-------------------Test MOCKUP-----------------------
//package com.example.prm392_group4_teamproject.fragments;
//
//import android.os.Bundle;
//import android.util.Log;
//import android.view.*;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.*;
//
//import com.example.prm392_group4_teamproject.CAPI.ApiClient;
//import com.example.prm392_group4_teamproject.CAPI.ApiService;
//import com.example.prm392_group4_teamproject.CAPI.NotificationItem;
//import com.example.prm392_group4_teamproject.CAPI.NotificationResponse;
//import com.example.prm392_group4_teamproject.R;
//import com.example.prm392_group4_teamproject.adapters.NotificationAdapter;
//
//import java.util.*;
//
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
//public class NotificationFragment extends Fragment {
//
//    private RecyclerView recyclerView;
//    private TextView noNotificationText;
//    private NotificationAdapter adapter;
//    private List<NotificationItem> notificationList = new ArrayList<>();
//    private static final String TAG = "NotificationFragment";
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             @Nullable ViewGroup container,
//                             @Nullable Bundle savedInstanceState) {
//
//        View view = inflater.inflate(R.layout.fragment_notification, container, false);
//
//        recyclerView = view.findViewById(R.id.notificationRecyclerView);
//        noNotificationText = view.findViewById(R.id.noNotificationText);
//
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        adapter = new NotificationAdapter(notificationList);
//        recyclerView.setAdapter(adapter);
//
//        loadNotifications();
//
//        return view;
//    }
//
//    private void loadNotifications() {
//        ApiService service = ApiClient.getClientWithToken(requireContext()).create(ApiService.class);
//        service.getNotifications().enqueue(new Callback<NotificationResponse>() {
//            @Override
//            public void onResponse(Call<NotificationResponse> call, Response<NotificationResponse> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    notificationList.clear();
//                    notificationList.addAll(response.body().getNotifications());
//
//                    // Thêm dữ liệu giả nếu danh sách rỗng (test giao diện)
//                    if (notificationList.isEmpty()) {
//                        Log.d(TAG, "No real notifications found, inserting mock data.");
//
//                        notificationList.add(createMockNotification(
//                                "Chào mừng bạn!",
//                                "Cảm ơn bạn đã sử dụng ứng dụng của chúng tôi.",
//                                "2025-07-03T08:00:00.000Z"
//                        ));
//                        notificationList.add(createMockNotification(
//                                "Thông báo cập nhật",
//                                "Tính năng mới đã được thêm vào.",
//                                "2025-07-03T12:30:00.000Z"
//                        ));
//                    }
//
//                    adapter.notifyDataSetChanged();
//
//                    if (notificationList.isEmpty()) {
//                        noNotificationText.setVisibility(View.VISIBLE);
//                        recyclerView.setVisibility(View.GONE);
//                    } else {
//                        noNotificationText.setVisibility(View.GONE);
//                        recyclerView.setVisibility(View.VISIBLE);
//                    }
//
//                    Log.d(TAG, "Loaded " + notificationList.size() + " notifications.");
//                } else {
//                    Log.e(TAG, "Failed to load notifications: " + response.code());
//                    Toast.makeText(getContext(), "Không thể tải thông báo", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<NotificationResponse> call, Throwable t) {
//                Log.e(TAG, "Error loading notifications", t);
//                Toast.makeText(getContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private NotificationItem createMockNotification(String title, String message, String createdAt) {
//        return new NotificationItem() {
//            @Override public String getTitle() { return title; }
//            @Override public String getMessage() { return message; }
//            @Override public String getCreatedAt() { return createdAt; }
//            @Override public String getId() { return UUID.randomUUID().toString(); }
//        };
//    }
//}
