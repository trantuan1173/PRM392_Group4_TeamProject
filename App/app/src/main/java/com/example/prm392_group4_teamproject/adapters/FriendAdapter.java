package com.example.prm392_group4_teamproject.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.prm392_group4_teamproject.model.FriendUser;
import com.example.prm392_group4_teamproject.DetailFriendActivity;
import com.example.prm392_group4_teamproject.R;

import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

    private final List<FriendUser> friendList;
    private final Context context;
    private final OnFriendClickListener listener;

    public interface OnFriendClickListener {
        void onClick(FriendUser friend);
    }

    public FriendAdapter(Context context, List<FriendUser> friendList, OnFriendClickListener listener) {
        this.context = context;
        this.friendList = friendList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        FriendUser friend = friendList.get(position);

        holder.name.setText(friend.getName());
        holder.bio.setText(friend.getBio());
        holder.address.setText(friend.getLocation().getAddress());

        Glide.with(context)
                .load(friend.getAvatar())
                .placeholder(R.drawable.user)
                .into(holder.avatar);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(friend);
            }
        });
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView name, bio, address;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            name = itemView.findViewById(R.id.name);
            bio = itemView.findViewById(R.id.bio);
            address = itemView.findViewById(R.id.address);
        }
    }
}
