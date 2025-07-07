package com.example.prm392_group4_teamproject.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.prm392_group4_teamproject.Cards.cards;
import com.example.prm392_group4_teamproject.Cards.arrayAdapter;

import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;

import java.util.ArrayList;
import java.util.List;

import com.example.prm392_group4_teamproject.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.swipe_screen);

        CardStackView cardStackView = findViewById(R.id.card_stack_view);
        CardStackLayoutManager manager = new CardStackLayoutManager(this, new CardStackListener() {
            @Override
            public void onCardDragging(Direction direction, float ratio) {
                // xử lý khi vuốt
            }

            @Override
            public void onCardSwiped(Direction direction) {
                // xử lý khi vuốt xong
            }

            @Override
            public void onCardRewound() {
                // xử lý khi card quay lại
            }

            @Override
            public void onCardCanceled() {
                // khi hủy vuốt (card trở về giữa)
            }

            @Override
            public void onCardAppeared(View view, int position) {
                // khi 1 card bắt đầu hiển thị
            }

            @Override
            public void onCardDisappeared(View view, int position) {
                // khi 1 card biến mất sau khi vuốt
            }
        });
        cardStackView.setLayoutManager(manager);

        List<cards> userList = new ArrayList<>();
        userList.add(new cards("1", "Tuấn", "https://i.imgur.com/Xtq3M7G.jpeg"));
        userList.add(new cards("2", "Trang", "https://i.imgur.com/fz4BMtW.jpeg"));
        userList.add(new cards("3", "Minh", "https://i.imgur.com/K6fW3Ou.jpeg"));

        arrayAdapter adapter = new arrayAdapter(userList, this);
        cardStackView.setAdapter(adapter);
        Button goToRegisterButton = findViewById(R.id.goToRegisterButton);
        goToRegisterButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });
    }
}