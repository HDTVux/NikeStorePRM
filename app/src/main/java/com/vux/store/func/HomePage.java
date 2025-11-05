package com.vux.store.func;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout; // Import SwipeRefreshLayout
import com.vux.store.R;
import com.vux.store.adapter.BannerAdapter;
import com.vux.store.adapter.CategoryAdapter;
import com.vux.store.adapter.ProductNewAdapter;
import com.vux.store.model.BannerResponse;
import com.vux.store.model.CartResponse;
import com.vux.store.model.CategoriesResponse;
import com.vux.store.model.NewProductsResponse;
import com.vux.store.net.RetrofitClient;
import com.vux.store.util.SessionManager;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomePage extends BaseActivity {

    private ViewPager2 viewPagerSlider;
    private RecyclerView rvCategories;
    private CategoryAdapter categoryAdapter;
    private RecyclerView rvNewProducts;
    private ProductNewAdapter newAdapter;
    private ImageButton btnCart;
    private TextView tvCartBadge;
    private SessionManager session;
    private SwipeRefreshLayout swipeRefreshLayout; // NEW: Declare SwipeRefreshLayout

    // NEW: Counter for pending API requests to manage SwipeRefreshLayout
    private int pendingApiRequests = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        
        setupBottomNav();

        EditText edtSearch = findViewById(R.id.edtSearch);
        session = new SessionManager(this);

        btnCart = findViewById(R.id.btnCart);
        tvCartBadge = findViewById(R.id.tvCartBadge);

        if (btnCart != null) {
            btnCart.setOnClickListener(v -> {
                startActivity(new Intent(HomePage.this, com.vux.store.func.CartActivity.class));
            });
        }
        if (edtSearch != null) {
            edtSearch.setFocusable(false);
            edtSearch.setOnClickListener(v -> {
                Intent i = new Intent(HomePage.this, com.vux.store.func.SearchActivity.class);
                startActivity(i);
            });
        }

        viewPagerSlider = findViewById(R.id.viewPagerSlider);
        if (viewPagerSlider != null) {
            viewPagerSlider.setAdapter(new BannerAdapter(new ArrayList<>()));
            viewPagerSlider.setOffscreenPageLimit(1);
        }
        rvCategories = findViewById(R.id.rvCategories);
        categoryAdapter = new CategoryAdapter();
        if (rvCategories != null) {
            rvCategories.setAdapter(categoryAdapter);
            rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rvCategories.setHasFixedSize(true);
        }
        categoryAdapter.setOnItemClickListener(item -> {
            Intent i = new Intent(HomePage.this, CategoryProductsActivity.class);
            i.putExtra(CategoryProductsActivity.EXTRA_CAT_ID, item.getId());
            i.putExtra(CategoryProductsActivity.EXTRA_CAT_NAME, item.getName());
            startActivity(i);
        });
        rvNewProducts = findViewById(R.id.rvNewProducts);
        newAdapter = new ProductNewAdapter(this);
        newAdapter.setOnItemClickListener(item -> {
            Intent i = new Intent(this, ProductDetailActivity.class);
            i.putExtra("product_id", item.id);
            startActivity(i);
        });
        if (rvNewProducts != null) {
            rvNewProducts.setAdapter(newAdapter);
            rvNewProducts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rvNewProducts.setHasFixedSize(true);
        }

        // NEW: Initialize SwipeRefreshLayout and set its listener
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                // When user pulls to refresh, reload all data
                loadAllData();
            });
        }

        // Load data initially
        loadAllData();
    }

    @Override
    protected int getNavigationMenuItemId() {
        return R.id.nav_home;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadgeFromServer();
    }

    private void loadAllData() {
        pendingApiRequests = 3; // We have 3 API calls: banners, categories, new products
        // Ensure refresh indicator is visible when initiating refresh via code
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }
        loadBanners();
        loadCategories();
        loadNewProducts();
        updateCartBadgeFromServer(); // Also update cart badge
    }

    private void checkAndStopRefreshing() {
        pendingApiRequests--;
        if (pendingApiRequests <= 0) {
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    private void updateCartBadgeFromServer() {
        int uid = session != null ? session.getUserId() : 0;
        if (uid <= 0) {
            updateCartBadge(0); // Update bottom nav badge
            if (tvCartBadge != null) tvCartBadge.setVisibility(View.GONE); // Update header badge
            return;
        }

        RetrofitClient.api().getCart(uid).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(Call<CartResponse> call, Response<CartResponse> response) {
                int count = 0;
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    count = response.body().count;
                }
                final int finalCount = count;
                runOnUiThread(() -> {
                    updateCartBadge(finalCount); // Update bottom nav badge
                    if (tvCartBadge == null) return;
                    if (finalCount <= 0) {
                        tvCartBadge.setVisibility(View.GONE);
                    } else {
                        tvCartBadge.setVisibility(View.VISIBLE);
                        tvCartBadge.setText(finalCount > 99 ? "99+" : String.valueOf(finalCount));
                    }
                });
            }

            @Override
            public void onFailure(Call<CartResponse> call, Throwable t) {
                Log.w("HomePage", "updateCartBadge failed: " + t.getMessage());
            }
        });
    }

    private void loadBanners() {
        RetrofitClient.api().getBanners().enqueue(new Callback<BannerResponse>() {
            @Override
            public void onResponse(Call<BannerResponse> call, Response<BannerResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    if (viewPagerSlider != null) viewPagerSlider.setAdapter(new BannerAdapter(response.body().banners));
                }
                checkAndStopRefreshing(); // Call to check and potentially stop refreshing
            }
            @Override
            public void onFailure(Call<BannerResponse> call, Throwable t) {
                Log.e("BANNER", "Error: " + t.getMessage(), t);
                checkAndStopRefreshing(); // Call to check and potentially stop refreshing even on failure
            }
        });
    }
    private void loadCategories() {
        RetrofitClient.api().getCategories().enqueue(new Callback<CategoriesResponse>() {
            @Override
            public void onResponse(Call<CategoriesResponse> call, Response<CategoriesResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    categoryAdapter.submitList(response.body().data);
                }
                checkAndStopRefreshing(); // Call to check and potentially stop refreshing
            }
            @Override
            public void onFailure(Call<CategoriesResponse> call, Throwable t) {
                Log.e("CATEGORIES", "Network error: " + t.getMessage(), t);
                checkAndStopRefreshing(); // Call to check and potentially stop refreshing even on failure
            }
        });
    }
    private void loadNewProducts() {
        RetrofitClient.api().getNewProducts().enqueue(new Callback<NewProductsResponse>() {
            @Override
            public void onResponse(Call<NewProductsResponse> call, Response<NewProductsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    newAdapter.submit(response.body().products);
                }
                checkAndStopRefreshing(); // Call to check and potentially stop refreshing
            }
            @Override
            public void onFailure(Call<NewProductsResponse> call, Throwable t) {
                Log.e("NEW_PRODUCTS", "Network error: " + t.getMessage(), t);
                checkAndStopRefreshing(); // Call to check and potentially stop refreshing even on failure
            }
        });
    }
}
