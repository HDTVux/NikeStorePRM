package com.vux.store.func;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager; // Changed to GridLayoutManager for better display
import androidx.recyclerview.widget.RecyclerView;

import com.vux.store.R;
import com.vux.store.adapter.ProductNewAdapter;
import com.vux.store.model.Product;
import com.vux.store.model.WishlistResponse;
import com.vux.store.net.RetrofitClient;
import com.vux.store.util.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WishlistActivity extends BaseActivity {

    private RecyclerView rvWishlistProducts;
    private TextView tvEmptyWishlist;
    private ProductNewAdapter productAdapter;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_wishlist, findViewById(R.id.container_main_content));

        sessionManager = SessionManager.getInstance(this);

        rvWishlistProducts = findViewById(R.id.rvWishlistProducts);
        tvEmptyWishlist = findViewById(R.id.tvEmptyWishlist);

        setupRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWishlist(); // Load wishlist every time the activity comes to foreground
    }

    @Override
    protected int getNavigationMenuItemId() {
        return R.id.nav_wishlist;
    }

    private void setupRecyclerView() {
        rvWishlistProducts.setLayoutManager(new GridLayoutManager(this, 2)); // Use GridLayoutManager
        // UPDATED: Use the new constructor with item_product_wishlist layout
        productAdapter = new ProductNewAdapter(this, R.layout.item_product_wishlist);
        productAdapter.setOnItemClickListener(item -> {
            Log.d("WISHLIST_CLICK", "Wishlist -> open product id=" + item.id);
            Intent i = new Intent(WishlistActivity.this, ProductDetailActivity.class);
            i.putExtra("product_id", item.id);
            startActivity(i);
        });
        rvWishlistProducts.setAdapter(productAdapter);
    }

    private void loadWishlist() {
        int userId = sessionManager.getUserId();
        if (userId <= 0) {
            tvEmptyWishlist.setText("Bạn cần đăng nhập để xem danh sách yêu thích.");
            tvEmptyWishlist.setVisibility(View.VISIBLE);
            rvWishlistProducts.setVisibility(View.GONE);
            return;
        }

        RetrofitClient.api().getFavorites(userId).enqueue(new Callback<WishlistResponse>() {
            @Override
            public void onResponse(Call<WishlistResponse> call, Response<WishlistResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        if (response.body().success) {
                            List<Product> wishlistProducts = response.body().wishlist;
                            if (wishlistProducts != null && !wishlistProducts.isEmpty()) {
                                productAdapter.submit(wishlistProducts);
                                tvEmptyWishlist.setVisibility(View.GONE);
                                rvWishlistProducts.setVisibility(View.VISIBLE);
                            } else {
                                tvEmptyWishlist.setText("Danh sách yêu thích của bạn đang trống.");
                                tvEmptyWishlist.setVisibility(View.VISIBLE);
                                rvWishlistProducts.setVisibility(View.GONE);
                            }
                        } else {
                            String errorMessage = (response.body().message != null && !response.body().message.isEmpty()) ? response.body().message : "Lỗi không xác định từ server";
                            Log.e("WishlistActivity", "API Error: " + errorMessage);
                            tvEmptyWishlist.setText("Không thể tải danh sách yêu thích. Lỗi: " + errorMessage);
                            tvEmptyWishlist.setVisibility(View.VISIBLE);
                            rvWishlistProducts.setVisibility(View.GONE);
                        }
                    } else {
                        String errorBody = "";
                        try { if (response.errorBody() != null) errorBody = response.errorBody().string(); } catch (Exception e) { Log.e("WishlistActivity", "Error parsing error body", e); }
                        Log.e("WishlistActivity", "Empty response body. Code: " + response.code() + ", Raw error: " + errorBody);
                        tvEmptyWishlist.setText("Không thể tải danh sách yêu thích. Lỗi: Phản hồi trống (Mã: " + response.code() + ")");
                        tvEmptyWishlist.setVisibility(View.VISIBLE);
                        rvWishlistProducts.setVisibility(View.GONE);
                    }
                } else {
                    String errorBody = "";
                    try { if (response.errorBody() != null) errorBody = response.errorBody().string(); } catch (Exception e) { Log.e("WishlistActivity", "Error parsing error body", e); }
                    Log.e("WishlistActivity", "Unsuccessful response. Code: " + response.code() + ", Raw error: " + errorBody);
                    tvEmptyWishlist.setText("Không thể tải danh sách yêu thích. Lỗi: " + response.code() + " - " + errorBody);
                    tvEmptyWishlist.setVisibility(View.VISIBLE);
                    rvWishlistProducts.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<WishlistResponse> call, Throwable t) {
                Log.e("WishlistActivity", "Network error loading wishlist: " + t.getMessage(), t);
                tvEmptyWishlist.setText("Lỗi mạng khi tải danh sách yêu thích: " + t.getMessage());
                tvEmptyWishlist.setVisibility(View.VISIBLE);
                rvWishlistProducts.setVisibility(View.GONE);
            }
        });
    }
}
