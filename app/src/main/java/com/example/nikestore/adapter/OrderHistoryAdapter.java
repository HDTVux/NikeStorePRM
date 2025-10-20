package com.example.nikestore.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nikestore.R;
import com.example.nikestore.model.OrdersResponse;
import com.example.nikestore.model.Order; 
import com.example.nikestore.model.Payment; 

import java.util.List;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {

    private List<OrdersResponse.OrderSummary> orders;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onOrderClick(OrdersResponse.OrderSummary order);
    }

    public OrderHistoryAdapter(List<OrdersResponse.OrderSummary> orders) {
        this.orders = orders;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void submit(List<OrdersResponse.OrderSummary> newOrders) {
        this.orders = newOrders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_history, parent, false);
        return new OrderViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrdersResponse.OrderSummary item = orders.get(position);
        Order order = item.order; 
        Payment payment = item.payment; 

        holder.tvOrderId.setText("Đơn #" + order.id);
        holder.tvOrderDate.setText("Ngày: " + (order.created_at != null ? order.created_at.substring(0, 10) : "-"));
        holder.tvOrderTotal.setText("Tổng: " + formatCurrency(order.total_price) + "₫");
        holder.tvStatus.setText("Trạng thái: " + getStatusText(order.status)); 
        holder.tvOrderPayment.setText("Thanh toán: " + getPaymentMethod(order.payment_method));

        // Click mở chi tiết đơn hàng
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onOrderClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return orders != null ? orders.size() : 0;
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderDate, tvOrderTotal, tvStatus, tvOrderPayment; 
        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderTotal = itemView.findViewById(R.id.tvTotal); // Đã sửa từ tvOrderTotal thành tvTotal
            tvStatus = itemView.findViewById(R.id.tvStatus); 
            tvOrderPayment = itemView.findViewById(R.id.tvPayment); // Đã sửa từ tvOrderPayment thành tvPayment
        }
    }

    private String formatCurrency(double amount) {
        return String.format("%,.0f", amount);
    }

    private String getStatusText(String status) {
        if (status == null) return "-";
        switch (status) {
            case "pending":
                return "Chờ xử lý";
            case "paid":
                return "Đã thanh toán";
            case "cancelled":
                return "Đã huỷ";
            default:
                return status;
        }
    }

    private String getPaymentMethod(String method) {
        if (method == null) return "-";
        switch (method) {
            case "cod":
                return "COD";
            case "vnpay":
                return "VNPay";
            case "momo":
                return "Momo";
            case "paypal":
                return "Paypal";
            default:
                return method;
        }
    }
}
