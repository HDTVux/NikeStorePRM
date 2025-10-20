package com.example.nikestore.func;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nikestore.R;
import com.example.nikestore.model.Order;
import com.example.nikestore.model.OrderDetailResponse;
import com.example.nikestore.model.OrderItem;
import com.example.nikestore.net.RetrofitClient;
import com.example.nikestore.util.SessionManager;
import com.example.nikestore.adapter.OrderItemAdapter;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailActivity extends AppCompatActivity {
    private TextView tvOrderId, tvStatus, tvTotal, tvPayment, tvAddress, tvPhone;
    private RecyclerView rvItems;
    private OrderItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        tvOrderId = findViewById(R.id.tvOrderId);
        tvStatus = findViewById(R.id.tvStatus);
        tvTotal = findViewById(R.id.tvTotal);
        tvPayment = findViewById(R.id.tvPayment);
        tvAddress = findViewById(R.id.tvAddress);
        tvPhone = findViewById(R.id.tvPhone);
        rvItems = findViewById(R.id.rvItems);

        adapter = new OrderItemAdapter();
        rvItems.setAdapter(adapter);
        rvItems.setLayoutManager(new LinearLayoutManager(this));

        int orderId = getIntent().getIntExtra("order_id", 0);
        int userId = SessionManager.getInstance(this).getUserId();

        loadOrderDetail(orderId, userId);
    }

    private void loadOrderDetail(int orderId, int userId) {
        RetrofitClient.api().getOrderDetail("get_order_detail", orderId, userId)
                .enqueue(new Callback<OrderDetailResponse>() {
                    @Override
                    public void onResponse(Call<OrderDetailResponse> call, Response<OrderDetailResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().success) {
                            Order order = response.body().order;
                            tvOrderId.setText("Order #" + order.id);
                            tvStatus.setText("Status: " + order.status);
                            tvTotal.setText("Total: $" + order.total_price);
                            tvPayment.setText("Payment: " + order.payment_method);
                            tvAddress.setText("Address: " + order.shipping_address);
                            tvPhone.setText("Phone: " + order.phone);
                            adapter.submit((List<OrderItem>) response.body().items);
                        } else {
                            Toast.makeText(OrderDetailActivity.this, "Không thể tải chi tiết đơn", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<OrderDetailResponse> call, Throwable t) {
                        Toast.makeText(OrderDetailActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
