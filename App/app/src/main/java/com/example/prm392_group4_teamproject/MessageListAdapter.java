package com.example.prm392_group4_teamproject;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MessageViewHolder> {
    private final Context context;
    private final List<MatchItem> messageList;

    public MessageListAdapter(Context context, List<MatchItem> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message_card, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        MatchItem message = messageList.get(position);
        User user = message.getFullOtherUser();

        if (user != null) {
            holder.name.setText(user.getName());
            holder.message.setText(message.getLastMessage()); // hoặc message.getLastMessage()
            holder.time.setText(TimeUtils.formatRelativeTime(message.getLastMessageTime()));

            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                Glide.with(context).load(user.getAvatar()).into(holder.avatar);
            } else {
                holder.avatar.setImageResource(R.drawable.girrl);
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, MessageChart.class);
                intent.putExtra("matchId", message.get_id()); // 👈 truyền matchId
                intent.putExtra("userId", user.getId());          // 👈 (nếu cần)
                intent.putExtra("userName", user.getName());
                intent.putExtra("userAvatar", user.getAvatar());
                context.startActivity(intent);
            });


        } else {
            holder.name.setText("Ẩn danh");
            holder.message.setText("Không có nội dung");
            holder.time.setText("");
            holder.avatar.setImageResource(R.drawable.girrl);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView avatar;
        TextView name, message, time;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatarMessage);
            name = itemView.findViewById(R.id.userName);
            message = itemView.findViewById(R.id.messageText);
            time = itemView.findViewById(R.id.messageTime);
        }
    }
}
