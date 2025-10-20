package com.example.nikestore.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nikestore.R;
import com.example.nikestore.model.OrderItem;

import java.util.ArrayList;
import java.util.List;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {

    private List<OrderItem> items = new ArrayList<>();

    public void submit(List<OrderItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_item, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        OrderItem item = items.get(position);
        holder.tvProductName.setText(item.product_name);
        holder.tvQuantity.setText("Số lượng: " + item.quantity);
        holder.tvPrice.setText("Giá: $" + item.price);
        holder.tvTotal.setText("Tổng: $" + item.total);
        
        // Đã xóa logic tải ảnh và Log.d ở đây
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class OrderItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvQuantity, tvPrice, tvTotal;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvOrderItemProductName);
            tvQuantity = itemView.findViewById(R.id.tvOrderItemQuantity);
            tvPrice = itemView.findViewById(R.id.tvOrderItemPrice);
            tvTotal = itemView.findViewById(R.id.tvOrderItemTotal);
        }
    }
}
