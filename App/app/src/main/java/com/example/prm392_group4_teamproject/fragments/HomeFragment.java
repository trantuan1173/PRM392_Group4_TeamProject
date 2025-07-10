package com.example.prm392_group4_teamproject.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.prm392_group4_teamproject.Cards.arrayAdapter;
import com.example.prm392_group4_teamproject.Cards.cards;
import com.example.prm392_group4_teamproject.R;
import com.yuyakaido.android.cardstackview.*;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private CardStackView cardStackView;
    private CardStackLayoutManager manager;
    private arrayAdapter adapter;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.swipe_screen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cardStackView = view.findViewById(R.id.card_stack_view);
        manager = new CardStackLayoutManager(getContext(), new CardStackListener() {
            @Override
            public void onCardDragging(Direction direction, float ratio) { }

            @Override
            public void onCardSwiped(Direction direction) {
                // Vuốt xong
            }

            @Override
            public void onCardRewound() { }

            @Override
            public void onCardCanceled() { }

            @Override
            public void onCardAppeared(View view, int position) { }

            @Override
            public void onCardDisappeared(View view, int position) { }
        });

        cardStackView.setLayoutManager(manager);

        List<cards> userList = new ArrayList<>();
        userList.add(new cards("1", "Tuấn", "https://i.imgur.com/Xtq3M7G.jpeg"));
        userList.add(new cards("2", "Trang", "https://i.imgur.com/fz4BMtW.jpeg"));
        userList.add(new cards("3", "Minh", "https://i.imgur.com/K6fW3Ou.jpeg"));

        adapter = new arrayAdapter(userList, getContext());
        cardStackView.setAdapter(adapter);

    }
}