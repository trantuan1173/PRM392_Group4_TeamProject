package com.example.prm392_group4_teamproject.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.prm392_group4_teamproject.MessageChartActivity;
import com.example.prm392_group4_teamproject.R;
import com.example.prm392_group4_teamproject.model.MatchItem;
import com.example.prm392_group4_teamproject.model.User;
import com.google.android.material.imageview.ShapeableImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NewMatchAdapter extends RecyclerView.Adapter<NewMatchAdapter.NewMatchViewHolder> {
    private final Context context;
    private final List<MatchItem> matchList;

    public NewMatchAdapter(Context context, List<MatchItem> matchList) {
        this.context = context;
        this.matchList = matchList;
    }

    @NonNull
    @Override
    public NewMatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_match_avatar, parent, false);
        return new NewMatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewMatchViewHolder holder, int position) {
        MatchItem match = matchList.get(position);
        User user = match.getFullOtherUser();

        if (user != null) {
            holder.name.setText(user.getName());

            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                Glide.with(context).load(user.getAvatar()).into(holder.avatar);
            } else {
                holder.avatar.setImageResource(R.drawable.girrl);
            }

            holder.itemView.setOnClickListener(v -> {
                if (match.get_id() != null) {
                    Intent intent = new Intent(context, MessageChartActivity.class);
                    intent.putExtra("matchId", match.get_id());
                    intent.putExtra("userId", user.getId());
                    intent.putExtra("userName", user.getName());
                    intent.putExtra("userAvatar", user.getAvatar());
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, match.toString(), Toast.LENGTH_SHORT).show();
                }
            });


        } else {
            holder.name.setText("Ẩn danh");
            holder.avatar.setImageResource(R.drawable.girrl);
        }
    }

    @Override
    public int getItemCount() {
        return matchList.size();
    }

    public static class NewMatchViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView avatar;
        TextView name;

        public NewMatchViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatarImage);
            name = itemView.findViewById(R.id.avatarName);
        }
    }
}
