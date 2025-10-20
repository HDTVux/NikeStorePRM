package com.example.nikestore.func;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nikestore.R;
import com.example.nikestore.model.OrdersResponse;
import com.example.nikestore.net.RetrofitClient;
import com.example.nikestore.util.SessionManager;
import com.example.nikestore.adapter.OrderHistoryAdapter; 

import java.util.ArrayList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryActivity extends AppCompatActivity {
    private RecyclerView rvOrders;
    private OrderHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        rvOrders = findViewById(R.id.rvOrders);
        adapter = new OrderHistoryAdapter(new ArrayList<>()); // Sửa lỗi ở đây, truyền một ArrayList rỗng
        rvOrders.setAdapter(adapter);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));

        adapter.setOnItemClickListener(order -> {
            Intent i = new Intent(this, OrderDetailActivity.class);
            i.putExtra("order_id", order.order.id); // Sửa lỗi ở đây: truy cập order.order.id
            startActivity(i);
        });

        int userId = SessionManager.getInstance(this).getUserId();
        loadOrders(userId);
    }

    private void loadOrders(int userId) {
        RetrofitClient.api().getUserOrders("get_user_orders", userId)
                .enqueue(new Callback<OrdersResponse>() {
                    @Override
                    public void onResponse(Call<OrdersResponse> call, Response<OrdersResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().success) {
                            adapter.submit(response.body().orders);
                        } else {
                            Toast.makeText(OrderHistoryActivity.this, "Không có đơn hàng nào", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<OrdersResponse> call, Throwable t) {
                        Toast.makeText(OrderHistoryActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
