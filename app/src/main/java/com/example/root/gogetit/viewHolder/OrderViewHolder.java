package com.example.root.gogetit.viewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.root.gogetit.Interface.ItemClickListener;
import com.example.root.gogetit.R;

public class OrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView textOrderId, textOrderStatus,textOrderPhone,textOrderAddress;

    private ItemClickListener itemClickListener;

    public OrderViewHolder(@NonNull View itemView) {
        super(itemView);

        textOrderAddress = itemView.findViewById(R.id.order_address);
        textOrderId = itemView.findViewById(R.id.order_id);
        textOrderPhone = itemView.findViewById(R.id.order_phone);
        textOrderStatus = itemView.findViewById(R.id.order_status);

        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {

        itemClickListener.onClick(view,getAdapterPosition(),false);
    }
}
