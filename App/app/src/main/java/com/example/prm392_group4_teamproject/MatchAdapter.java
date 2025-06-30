package com.example.prm392_group4_teamproject;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.MatchViewHolder> {
    private final Context context;
    private final List<MatchItem> matchList;

    public MatchAdapter(Context context, List<MatchItem> matchList) {
        this.context = context;
        this.matchList = matchList;
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_nearby_friend, parent, false);
        return new MatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        MatchItem match = matchList.get(position);
        User user = match.getFullOtherUser();

        if (user == null || user.getName() == null) {
            holder.tvName.setText("Ẩn danh");
            holder.tvDistance.setText("Không rõ vị trí");
            holder.imgAvatar.setImageResource(R.drawable.avataaaa);
        } else {
            holder.tvName.setText(user.getName());
            holder.tvDistance.setText(user.getDistanceText() + " · Distance location");


            if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
                holder.imgAvatar.setImageResource(R.drawable.avataaaa);
            } else {
                Glide.with(context).load(user.getAvatar()).into(holder.imgAvatar);
            }
        }


        holder.btnCall.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailFriendActivity.class);
            intent.putExtra("userId", user.getId()); // hoặc putExtra các dữ liệu khác cần thiết
            Log.e("Id",user.getId());
            context.startActivity(intent);
        });
    }



    @Override
    public int getItemCount() {
        return matchList.size();
    }

    public static class MatchViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvName, tvDistance;
        ImageButton btnCall, btnMatch;

        public MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            btnCall = itemView.findViewById(R.id.btnCall);
            btnMatch = itemView.findViewById(R.id.btnMatch);
        }
    }
}
