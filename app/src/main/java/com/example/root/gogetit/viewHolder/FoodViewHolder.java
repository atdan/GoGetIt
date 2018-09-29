package com.example.root.gogetit.viewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.root.gogetit.Interface.ItemClickListener;
import com.example.root.gogetit.R;

public class FoodViewHolder extends RecyclerView.ViewHolder
                implements View.OnClickListener{

    public TextView food_name, food_price;
    public ImageView food_image, fav_image, shareToFb;
    private ItemClickListener itemClickListener;


    public FoodViewHolder(@NonNull View itemView) {
        super(itemView);

        food_name = itemView.findViewById(R.id.food_name);
        food_image = itemView.findViewById(R.id.food_image);
        fav_image = itemView.findViewById(R.id.fav);
        shareToFb = itemView.findViewById(R.id.share_to_fb);
        food_price = itemView.findViewById(R.id.food_price);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        itemClickListener.onClick(view,getAdapterPosition(),false);

    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
}
