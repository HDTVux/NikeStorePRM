package com.example.nikestore.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nikestore.R;
import com.example.nikestore.model.OrderItem;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {

    private List<OrderItem> items = new ArrayList<>();
    private final DecimalFormat money = new DecimalFormat("#,##0.##");

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

        // NEW: Logic để hiển thị giá gốc và giá giảm giá
        double displayPrice;
        if (item.discount_percent > 0) {
            // Có khuyến mãi
            holder.tvOrderItemOriginalPrice.setText("$" + money.format(item.price));
            holder.tvOrderItemOriginalPrice.setPaintFlags(holder.tvOrderItemOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvOrderItemOriginalPrice.setVisibility(View.VISIBLE);
            holder.tvPrice.setText("Giá: $" + money.format(item.final_price));
            holder.tvPrice.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.red)); // Màu đỏ cho giá sale
            displayPrice = item.final_price; // Sử dụng giá cuối cùng để tính tổng
        } else {
            // Không có khuyến mãi
            holder.tvOrderItemOriginalPrice.setVisibility(View.GONE);
            holder.tvPrice.setText("Giá: $" + money.format(item.price));
            holder.tvPrice.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.black)); // Màu đen cho giá thường
            displayPrice = item.price; // Sử dụng giá gốc để tính tổng
        }
        
        // Calculate total for each item directly
        double itemTotal = item.quantity * displayPrice;
        holder.tvTotal.setText("Tổng: $" + String.format("%.1f", itemTotal));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class OrderItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvQuantity, tvPrice, tvOrderItemOriginalPrice, tvTotal;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvOrderItemProductName);
            tvQuantity = itemView.findViewById(R.id.tvOrderItemQuantity);
            tvOrderItemOriginalPrice = itemView.findViewById(R.id.tvOrderItemOriginalPrice); // NEW: Find tvOrderItemOriginalPrice
            tvPrice = itemView.findViewById(R.id.tvOrderItemPrice);
            tvTotal = itemView.findViewById(R.id.tvOrderItemTotal);
        }
    }
}