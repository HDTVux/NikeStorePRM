package com.vux.store.func;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout; // Import SwipeRefreshLayout

import com.vux.store.R;
import com.vux.store.model.OrdersResponse;
import com.vux.store.net.RetrofitClient;
import com.vux.store.util.SessionManager;
import com.vux.store.adapter.OrderHistoryAdapter;

import java.util.ArrayList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryActivity extends BaseActivity {
    private RecyclerView rvOrders;
    private OrderHistoryAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout; // NEW: Declare SwipeRefreshLayout

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        rvOrders = findViewById(R.id.rvOrders);
        adapter = new OrderHistoryAdapter(new ArrayList<>()); 
        rvOrders.setAdapter(adapter);
        rvOrders.setLayoutManager(new LinearLayoutManager(this)); // ĐÃ SỬA LỖI TẠI ĐÂY

        adapter.setOnItemClickListener(order -> {
            Intent i = new Intent(this, OrderDetailActivity.class);
            i.putExtra("order_id", order.order.id); 
            startActivity(i);
        });

        int userId = SessionManager.getInstance(this).getUserId();

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                loadOrders(userId);
            });
        }

        loadOrders(userId);
    }

    @Override
    protected int getNavigationMenuItemId() {
        return R.id.nav_orders;
    }
    private void loadOrders(int userId) {
        if (swipeRefreshLayout != null && !swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
        }

        RetrofitClient.api().getUserOrders("get_user_orders", userId)
                .enqueue(new Callback<OrdersResponse>() {
                    @Override
                    public void onResponse(Call<OrdersResponse> call, Response<OrdersResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().success) {
                            adapter.submit(response.body().orders);
                        } else {
                            Toast.makeText(OrderHistoryActivity.this, "Không có đơn hàng nào", Toast.LENGTH_SHORT).show();
                        }
                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }

                    @Override
                    public void onFailure(Call<OrdersResponse> call, Throwable t) {
                        Toast.makeText(OrderHistoryActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                });
    }
}
