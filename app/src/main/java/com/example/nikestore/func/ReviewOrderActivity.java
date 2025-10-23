package com.example.nikestore.func;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nikestore.R;
import com.example.nikestore.adapter.ProductReviewAdapter;
import com.example.nikestore.model.OrderDetailResponse;
import com.example.nikestore.model.OrderItem;
import com.example.nikestore.net.RetrofitClient;
import com.example.nikestore.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewOrderActivity extends AppCompatActivity {

    private TextView tvReviewTitle;
    private RecyclerView rvProductsForReview;
    private Button btnSubmitAllReviews;
    private ProductReviewAdapter adapter;
    private int orderId;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_order);

        tvReviewTitle = findViewById(R.id.tvReviewTitle);
        rvProductsForReview = findViewById(R.id.rvProductsForReview);
        btnSubmitAllReviews = findViewById(R.id.btnSubmitAllReviews);

        orderId = getIntent().getIntExtra("order_id", 0);
        userId = SessionManager.getInstance(this).getUserId();

        tvReviewTitle.setText("Đánh giá đơn hàng #" + orderId);

        adapter = new ProductReviewAdapter(new ArrayList<>(), userId);
        rvProductsForReview.setAdapter(adapter);
        rvProductsForReview.setLayoutManager(new LinearLayoutManager(this));

        loadOrderItemsForReview();

        btnSubmitAllReviews.setOnClickListener(v -> {
            // Logic để thu thập tất cả đánh giá và gửi lên server
            // Sẽ được triển khai sau khi ProductReviewAdapter hoàn thành
            Toast.makeText(this, "Tính năng gửi tất cả đánh giá chưa triển khai", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadOrderItemsForReview() {
        if (orderId <= 0 || userId <= 0) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID đơn hàng hoặc người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        RetrofitClient.api().getOrderDetail("get_order_detail", orderId, userId)
                .enqueue(new Callback<OrderDetailResponse>() {
                    @Override
                    public void onResponse(Call<OrderDetailResponse> call, Response<OrderDetailResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().success) {
                            adapter.submitList(response.body().items);
                        } else {
                            Toast.makeText(ReviewOrderActivity.this, "Không thể tải chi tiết đơn hàng", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<OrderDetailResponse> call, Throwable t) {
                        Toast.makeText(ReviewOrderActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }
}
