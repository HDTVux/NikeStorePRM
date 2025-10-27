package com.example.nikestore.func;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nikestore.R;
import com.example.nikestore.adapter.ProductNewAdapter;
import com.example.nikestore.model.NewProductsResponse;
import com.example.nikestore.model.Product;
import com.example.nikestore.model.PromotionResponse; // NEW: Import PromotionResponse
import com.example.nikestore.net.RetrofitClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch; // NEW: Import CountDownLatch
import java.util.concurrent.TimeUnit; // NEW: Import TimeUnit

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
    private List<Product> currentProducts = new ArrayList<>(); // NEW: To hold products from API
    private List<Product> currentPromotions = new ArrayList<>(); // NEW: To hold promotions from API

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_products);

        // BIND VIEWS
        rvProducts = findViewById(R.id.rvCategoryProducts);
        tvEmpty = findViewById(R.id.tvEmptyCategory);

        // INIT ADAPTER + LAYOUTMANAGER BEFORE ANY submit(...) CALL
        adapter = new ProductNewAdapter(this);
        try {
            adapter.setOnItemClickListener(item -> {
                Log.d("PRODUCT_CLICK", "CategoryProducts -> open product id=" + item.id);
                Intent i = new Intent(CategoryProductsActivity.this, com.example.nikestore.func.ProductDetailActivity.class);
                i.putExtra("product_id", item.id);
                startActivity(i);
            });
        } catch (Throwable ignore) {  }

        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        rvProducts.setAdapter(adapter);

        // Read extras
        categoryId = getIntent().getIntExtra(EXTRA_CAT_ID, 0);
        categoryName = getIntent().getStringExtra(EXTRA_CAT_NAME);
        setTitle(categoryName != null ? categoryName : "Products");

        // Load real data
        loadCategoryProducts();
    }

    @Override
    protected int getNavigationMenuItemId() {
        return 0; // CategoryProductsActivity is not a top-level navigation destination
    }

    private void loadCategoryProducts(){
        final CountDownLatch latch = new CountDownLatch(2); // Wait for 2 API calls

        // --- Call 1: Get Products by Category ---
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

        // --- Call 2: Get Promotions ---
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

        // --- Wait for both calls to complete ---
        new Thread(() -> {
            try {
                latch.await(10, TimeUnit.SECONDS); // Wait for a maximum of 10 seconds
                runOnUiThread(() -> {
                    // Apply promotions and update UI on the main thread
                    applyPromotionsToProducts(currentProducts, currentPromotions);
                    adapter.submit(currentProducts);

                    if (tvEmpty != null) {
                        tvEmpty.setVisibility(currentProducts.isEmpty() ? View.VISIBLE : View.GONE);
                        rvProducts.setVisibility(currentProducts.isEmpty() ? View.GONE : View.VISIBLE);
                        if (currentProducts.isEmpty()) {
                            tvEmpty.setText("Không có sản phẩm nào trong danh mục này.");
                        }
                    }
                });
            } catch (InterruptedException e) {
                Log.e("CAT_PRODUCTS", "Latch interrupted: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(CategoryProductsActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    if (adapter != null) adapter.submit(new ArrayList<>());
                    if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
                });
            }
        }).start();
    }

    // NEW: Method to apply promotion data to products
    private void applyPromotionsToProducts(List<Product> products, List<Product> promotions) {
        for (Product product : products) {
            for (Product promo : promotions) {
                if (product.id == promo.product_id) {
                    product.discount_percent = promo.discount_percent;
                    product.final_price = promo.final_price;
                    // Optionally, you might want to break here if each product only has one active promotion
                    break;
                }
            }
        }
    }

}
