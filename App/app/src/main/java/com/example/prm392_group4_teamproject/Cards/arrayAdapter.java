package com.example.prm392_group4_teamproject.Cards;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.prm392_group4_teamproject.R;
import com.yuyakaido.android.cardstackview.CardStackView;

import java.util.List;

public class arrayAdapter extends RecyclerView.Adapter<arrayAdapter.ViewHolder> {

    private List<cards> userList;
    private Context context;

    public arrayAdapter(List<cards> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    public cards getItem(int position) {
        return userList.get(position);
    }

    @Override
    public arrayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(arrayAdapter.ViewHolder holder, int position) {
        cards user = userList.get(position);
        holder.nameAge.setText(user.getName());

        Glide.with(context)
                .load(user.getImageUrl())
                .into(holder.profileImage);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView nameAge;

        public ViewHolder(View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            nameAge = itemView.findViewById(R.id.nameAge);
        }
    }
}