package com.example.nikestore.func;

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
import com.example.nikestore.R;
import com.example.nikestore.adapter.BannerAdapter;
import com.example.nikestore.adapter.CategoryAdapter;
import com.example.nikestore.adapter.ProductNewAdapter;
import com.example.nikestore.data.CartManager;
import com.example.nikestore.model.Banner;
import com.example.nikestore.model.BannerResponse;
import com.example.nikestore.model.CartResponse;
import com.example.nikestore.model.CategoriesResponse;
import com.example.nikestore.model.Category;
import com.example.nikestore.model.NewProductsResponse;
import com.example.nikestore.model.Product;
import com.example.nikestore.net.RetrofitClient;
import com.example.nikestore.util.SessionManager;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomePage extends BaseActivity {

    // ... (Your existing member variables)
    private ViewPager2 viewPagerSlider;
    private RecyclerView rvCategories;
    private CategoryAdapter categoryAdapter;
    private RecyclerView rvNewProducts;
    private ProductNewAdapter newAdapter;
    private ImageButton btnCart;
    private TextView tvCartBadge;
    private SessionManager session;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        
        // This will set up the listeners for the new BottomNavigationView
        setupBottomNav();

        EditText edtSearch = findViewById(R.id.edtSearch);
        session = new SessionManager(this);

        // The old cart button in the header might be redundant now, but we keep its logic
        btnCart = findViewById(R.id.btnCart);
        tvCartBadge = findViewById(R.id.tvCartBadge); // This is also likely in the header

        if (btnCart != null) {
            btnCart.setOnClickListener(v -> {
                startActivity(new Intent(HomePage.this, com.example.nikestore.func.CartActivity.class));
            });
        }
        if (edtSearch != null) {
            edtSearch.setFocusable(false);
            edtSearch.setOnClickListener(v -> {
                Intent i = new Intent(HomePage.this, com.example.nikestore.func.SearchActivity.class);
                startActivity(i);
            });
        }

        // ----- Banner, Categories, New Releases Setup (Your existing code) -----
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

        // Load data
        loadBanners();
        loadCategories();
        loadNewProducts();
        updateCartBadgeFromServer();
    }

    // IMPLEMENT THE REQUIRED METHOD FROM BASEACTIVITY
    @Override
    protected int getNavigationMenuItemId() {
        return R.id.nav_home;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadgeFromServer();
    }

    // Renamed to avoid confusion with the new updateCartBadge(int) in BaseActivity
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
                    if (tvCartBadge == null) return; // Update header badge (optional)
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

    // ... (Your loadBanners, loadCategories, loadNewProducts methods remain unchanged)
    private void loadBanners() {
        RetrofitClient.api().getBanners().enqueue(new Callback<BannerResponse>() {
            @Override
            public void onResponse(Call<BannerResponse> call, Response<BannerResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    if (viewPagerSlider != null) viewPagerSlider.setAdapter(new BannerAdapter(response.body().banners));
                }
            }
            @Override
            public void onFailure(Call<BannerResponse> call, Throwable t) {
                Log.e("BANNER", "Error: " + t.getMessage(), t);
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
            }
            @Override
            public void onFailure(Call<CategoriesResponse> call, Throwable t) {
                Log.e("CATEGORIES", "Network error: " + t.getMessage(), t);
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
            }
            @Override
            public void onFailure(Call<NewProductsResponse> call, Throwable t) {
                Log.e("NEW_PRODUCTS", "Network error: " + t.getMessage(), t);
            }
        });
    }
}
