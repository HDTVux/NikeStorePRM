package com.example.nikestore.func;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
import com.example.nikestore.model.Category;
import com.example.nikestore.model.CategoriesResponse;
import com.example.nikestore.model.NewProductsResponse;
import com.example.nikestore.model.Product;
import com.example.nikestore.model.CartResponse;
import com.example.nikestore.net.RetrofitClient;
import com.example.nikestore.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Home page activity (banner, categories, new releases) with cart badge and bottom nav.
 * Assumes:
 * - BaseActivity.setupBottomNav() exists and inflates/includes bottom navigation.
 * - ApiService includes getBanners(), getCategories(), getNewProducts(), getCart(user_id).
 * - CartManager exists and has init(Context), getInstance(), addListener(), removeListener().
 */
public class HomePage extends BaseActivity {

    // Banner
    private ViewPager2 viewPagerSlider;

    // Categories strip
    private RecyclerView rvCategories;
    private CategoryAdapter categoryAdapter;

    // New Releases
    private RecyclerView rvNewProducts;
    private ProductNewAdapter newAdapter;

    // Cart UI
    private ImageButton btnCart;
    private TextView tvCartBadge;
    private CartManager.OnChangeListener cartListener;

    private SessionManager session;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        // If BaseActivity provides bottom nav integration
        try { setupBottomNav(); } catch (Throwable ignore) {}

        // init session manager
        session = new SessionManager(this);

        // init CartManager (local singleton which may broadcast changes)
        try {
            CartManager.init(this);
        } catch (Throwable ignore) { Log.w("HomePage", "CartManager.init failed: " + ignore.getMessage()); }

        // bind cart views (IDs must exist in layout)
        btnCart = findViewById(R.id.btnCart);
        tvCartBadge = findViewById(R.id.tvCartBadge);

        if (btnCart != null) {
            btnCart.setOnClickListener(v -> {
                startActivity(new Intent(HomePage.this, com.example.nikestore.func.CartActivity.class));
            });
        }

        // register cart listener to update badge
        cartListener = new CartManager.OnChangeListener() {
            @Override
            public void onCartChanged(int totalCount, double totalPrice) {
                runOnUiThread(() -> {
                    if (tvCartBadge == null) return;
                    if (totalCount <= 0) {
                        tvCartBadge.setVisibility(View.GONE);
                    } else {
                        tvCartBadge.setVisibility(View.VISIBLE);
                        tvCartBadge.setText(totalCount > 99 ? "99+" : String.valueOf(totalCount));
                    }
                });
            }
        };

        try {
            CartManager.getInstance().addListener(cartListener);
        } catch (Throwable t) {
            Log.w("HomePage", "Cannot add cart listener: " + t.getMessage());
        }

        // ----- Banner -----
        viewPagerSlider = findViewById(R.id.viewPagerSlider);
        if (viewPagerSlider != null) {
            viewPagerSlider.setAdapter(new BannerAdapter(new ArrayList<>()));
            viewPagerSlider.setOffscreenPageLimit(1);
        }

        // ----- Categories -----
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

        // ----- New Releases -----
        rvNewProducts = findViewById(R.id.rvNewProducts);
        newAdapter = new ProductNewAdapter();
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

        // update badge once (initial)
        updateCartBadge();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // refresh badge on resume (in case cart changed in another screen)
        updateCartBadge();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // unregister listener
        try {
            if (cartListener != null) CartManager.getInstance().removeListener(cartListener);
        } catch (Throwable ignore) {}
    }

    // ================== BANNERS ==================
    private void loadBanners() {
        RetrofitClient.api().getBanners().enqueue(new Callback<BannerResponse>() {
            @Override
            public void onResponse(Call<BannerResponse> call, Response<BannerResponse> response) {
                try {
                    if (!isFinishing() && !isDestroyed()
                            && response.isSuccessful()
                            && response.body() != null
                            && response.body().success) {

                        List<Banner> list = response.body().banners;
                        if (list == null) list = new ArrayList<>();
                        if (viewPagerSlider != null) viewPagerSlider.setAdapter(new BannerAdapter(list));
                    } else {
                        Log.e("BANNER", "Response failed or empty");
                    }
                } catch (Throwable t) {
                    Log.e("BANNER", "onResponse crash-guard: " + t.getMessage(), t);
                }
            }

            @Override
            public void onFailure(Call<BannerResponse> call, Throwable t) {
                Log.e("BANNER", "Error: " + t.getMessage(), t);
            }
        });
    }

    // ================== CATEGORIES ==================
    private void loadCategories() {
        RetrofitClient.api().getCategories().enqueue(new Callback<CategoriesResponse>() {
            @Override
            public void onResponse(Call<CategoriesResponse> call, Response<CategoriesResponse> response) {
                try {
                    if (response.isSuccessful() && response.body() != null && response.body().success) {
                        categoryAdapter.submitList(response.body().data);
                    } else {
                        // fallback
                        List<Category> fallback = new ArrayList<>();
                        Category acc = new Category();
                        acc.setId(3);
                        acc.setName("Accessories");
                        fallback.add(acc);
                        categoryAdapter.submitList(fallback);
                    }
                } catch (Throwable t) {
                    Log.e("CATEGORIES", "onResponse crash-guard: " + t.getMessage(), t);
                }
            }

            @Override
            public void onFailure(Call<CategoriesResponse> call, Throwable t) {
                Log.e("CATEGORIES", "Network error: " + t.getMessage(), t);
                List<Category> fallback = new ArrayList<>();
                Category acc = new Category();
                acc.setId(3);
                acc.setName("Accessories");
                fallback.add(acc);
                categoryAdapter.submitList(fallback);
            }
        });
    }

    // ================== NEW RELEASES (4 sản phẩm) ==================
    private void loadNewProducts() {
        RetrofitClient.api().getNewProducts().enqueue(new Callback<NewProductsResponse>() {
            @Override
            public void onResponse(Call<NewProductsResponse> call, Response<NewProductsResponse> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        if (response.body().success) {
                            List<Product> list = response.body().products;
                            newAdapter.submit(list != null ? list : new ArrayList<>());
                        } else {
                            Log.e("NEW_PRODUCTS", "success=false");
                        }
                    } else {
                        String raw = response.errorBody() != null ? response.errorBody().string() : "null";
                        Log.e("NEW_PRODUCTS", "HTTP " + response.code() + " raw=" + raw);
                    }
                } catch (Throwable t) {
                    Log.e("NEW_PRODUCTS", "crash-guard: " + t.getMessage(), t);
                }
            }

            @Override
            public void onFailure(Call<NewProductsResponse> call, Throwable t) {
                Log.e("NEW_PRODUCTS", "Network error: " + t.getMessage(), t);
            }
        });
    }

    // ================== CART BADGE ==================
    private void updateCartBadge() {
        // If user not logged in hide badge (CartManager may still reflect guest cart)
        int uid = session != null ? session.getUserId() : 0;
        if (uid <= 0) {
            // If you support guest cart via CartManager, you could query it instead.
            if (tvCartBadge != null) tvCartBadge.setVisibility(View.GONE);
            return;
        }

        try {
            RetrofitClient.api().getCart(uid).enqueue(new Callback<CartResponse>() {
                @Override
                public void onResponse(Call<CartResponse> call, Response<CartResponse> response) {
                    if (!isFinishing() && !isDestroyed() && response.isSuccessful() && response.body() != null && response.body().success) {
                        int count = response.body().count;
                        runOnUiThread(() -> {
                            if (tvCartBadge == null) return;
                            if (count <= 0) {
                                tvCartBadge.setVisibility(View.GONE);
                            } else {
                                tvCartBadge.setVisibility(View.VISIBLE);
                                tvCartBadge.setText(count > 99 ? "99+" : String.valueOf(count));
                            }
                        });
                    } else {
                        runOnUiThread(() -> { if (tvCartBadge != null) tvCartBadge.setVisibility(View.GONE); });
                    }
                }

                @Override
                public void onFailure(Call<CartResponse> call, Throwable t) {
                    Log.w("HomePage", "updateCartBadge failed: " + t.getMessage());
                    // keep existing badge state
                }
            });
        } catch (Throwable t) {
            Log.w("HomePage", "updateCartBadge exception: " + t.getMessage());
        }
    }
}
