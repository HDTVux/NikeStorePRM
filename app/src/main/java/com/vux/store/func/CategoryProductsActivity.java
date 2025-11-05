package com.vux.store.func;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout; // Import SwipeRefreshLayout

import com.vux.store.R;
import com.vux.store.adapter.ProductNewAdapter;
import com.vux.store.model.NewProductsResponse;
import com.vux.store.model.Product;
import com.vux.store.model.PromotionResponse;
import com.vux.store.net.RetrofitClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryProductsActivity extends BaseActivity {
    public static final String EXTRA_CAT_ID = "cat_id";
    public static final String EXTRA_CAT_NAME = "cat_name";

    private RecyclerView rvProducts;
    private ProductNewAdapter adapter;
    private int categoryId;
    private String categoryName;
    private TextView tvEmpty;
    private SwipeRefreshLayout swipeRefreshLayout; // NEW: Declare SwipeRefreshLayout
    private List<Product> currentProducts = new ArrayList<>();
    private List<Product> currentPromotions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_products);

        rvProducts = findViewById(R.id.rvCategoryProducts);
        tvEmpty = findViewById(R.id.tvEmptyCategory);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout); // NEW: Initialize SwipeRefreshLayout

        adapter = new ProductNewAdapter(this);
        adapter.setOnItemClickListener(item -> {
            Intent i = new Intent(CategoryProductsActivity.this, com.vux.store.func.ProductDetailActivity.class);
            i.putExtra("product_id", item.id);
            startActivity(i);
        });

        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        rvProducts.setAdapter(adapter);

        categoryId = getIntent().getIntExtra(EXTRA_CAT_ID, 0);
        categoryName = getIntent().getStringExtra(EXTRA_CAT_NAME);
        setTitle(categoryName != null ? categoryName : "Products");

        // NEW: Set up SwipeRefreshLayout listener
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::loadCategoryProducts);
        }

        loadCategoryProducts();
    }

    @Override
    protected int getNavigationMenuItemId() {
        return 0;
    }

    private void loadCategoryProducts() {
        // Ensure refresh indicator is visible when initiating refresh
        if (swipeRefreshLayout != null && !swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
        }

        final CountDownLatch latch = new CountDownLatch(2);

        Call<NewProductsResponse> productsCall = RetrofitClient.api().getProductsByCategory(categoryId, 1, 50, "newest");
        productsCall.enqueue(new Callback<NewProductsResponse>() {
            @Override
            public void onResponse(Call<NewProductsResponse> call, Response<NewProductsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    currentProducts = response.body().getProductList() != null ? response.body().getProductList() : new ArrayList<>();
                } else {
                    Log.e("CAT_PRODUCTS", "Failed to load products: " + response.message());
                }
                latch.countDown();
            }

            @Override
            public void onFailure(Call<NewProductsResponse> call, Throwable t) {
                Log.e("CAT_PRODUCTS", "Error loading products: " + t.getMessage(), t);
                latch.countDown();
            }
        });

        Call<PromotionResponse> promotionsCall = RetrofitClient.api().getPromotions();
        promotionsCall.enqueue(new Callback<PromotionResponse>() {
            @Override
            public void onResponse(Call<PromotionResponse> call, Response<PromotionResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    currentPromotions = response.body().promotions != null ? response.body().promotions : new ArrayList<>();
                } else {
                    Log.e("CAT_PRODUCTS", "Failed to load promotions: " + response.message());
                }
                latch.countDown();
            }

            @Override
            public void onFailure(Call<PromotionResponse> call, Throwable t) {
                Log.e("CAT_PRODUCTS", "Error loading promotions: " + t.getMessage(), t);
                latch.countDown();
            }
        });

        new Thread(() -> {
            try {
                latch.await(10, TimeUnit.SECONDS);
                runOnUiThread(() -> {
                    applyPromotionsToProducts(currentProducts, currentPromotions);
                    adapter.submit(currentProducts);

                    if (tvEmpty != null) {
                        tvEmpty.setVisibility(currentProducts.isEmpty() ? View.VISIBLE : View.GONE);
                        rvProducts.setVisibility(currentProducts.isEmpty() ? View.GONE : View.VISIBLE);
                    }
                    // NEW: Stop refreshing after UI update
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            } catch (InterruptedException e) {
                Log.e("CAT_PRODUCTS", "Latch interrupted: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(CategoryProductsActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    if (adapter != null) adapter.submit(new ArrayList<>());
                    if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
                    // NEW: Stop refreshing on error
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

    private void applyPromotionsToProducts(List<Product> products, List<Product> promotions) {
        for (Product product : products) {
            for (Product promo : promotions) {
                if (product.id == promo.product_id) {
                    product.discount_percent = promo.discount_percent;
                    product.final_price = promo.final_price;
                    break;
                }
            }
        }
    }
}
