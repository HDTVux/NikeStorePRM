package com.example.nikestore.func;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.nikestore.R;
import com.example.nikestore.adapter.BannerAdapter;
import com.example.nikestore.adapter.CategoryAdapter;
import com.example.nikestore.adapter.ProductNewAdapter;
import com.example.nikestore.model.Banner;
import com.example.nikestore.model.BannerResponse;
import com.example.nikestore.model.Category;
import com.example.nikestore.model.CategoriesResponse;
import com.example.nikestore.model.NewProductsResponse;
import com.example.nikestore.model.Product;
import com.example.nikestore.net.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomePage extends AppCompatActivity {

    // Banner
    private ViewPager2 viewPagerSlider;

    // Categories strip
    private RecyclerView rvCategories;
    private CategoryAdapter categoryAdapter;

    // New Releases
    private RecyclerView rvNewProducts;
    private ProductNewAdapter newAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        // ----- Banner -----
        viewPagerSlider = findViewById(R.id.viewPagerSlider);
        // Gắn adapter rỗng trước để tránh crash ViewPager2
        viewPagerSlider.setAdapter(new BannerAdapter(new ArrayList<>()));
        viewPagerSlider.setOffscreenPageLimit(1);

        // ----- Categories -----
        rvCategories = findViewById(R.id.rvCategories);
        categoryAdapter = new CategoryAdapter();
        rvCategories.setAdapter(categoryAdapter);
        rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setHasFixedSize(true);
        categoryAdapter.setOnItemClickListener(item -> {
            // Mở trang list sản phẩm theo category khi click
            Intent i = new Intent(HomePage.this, CategoryProductsActivity.class);
            i.putExtra(CategoryProductsActivity.EXTRA_CAT_ID, item.getId());
            i.putExtra(CategoryProductsActivity.EXTRA_CAT_NAME, item.getName());
            startActivity(i);
        });

        // ----- New Releases -----
        rvNewProducts = findViewById(R.id.rvNewProducts);
        newAdapter = new ProductNewAdapter();
        rvNewProducts.setAdapter(newAdapter);
        rvNewProducts.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        rvNewProducts.setHasFixedSize(true);

        // Gọi API (note: loadCategories trước loadNewProducts nếu bạn muốn category hiện nhanh hơn)
        loadBanners();
        loadCategories();
        loadNewProducts();
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
                        Log.d("BANNER", "count=" + (list == null ? 0 : list.size()));
                        if (list == null) list = new ArrayList<>();

                        viewPagerSlider.setAdapter(new BannerAdapter(list));
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
        // Note: ApiService must have getCategories() implemented (api.php?action=get_categories)
        RetrofitClient.api().getCategories().enqueue(new Callback<CategoriesResponse>() {
            @Override
            public void onResponse(Call<CategoriesResponse> call, Response<CategoriesResponse> response) {
                try {
                    if (response.isSuccessful() && response.body() != null && response.body().success) {
                        categoryAdapter.submitList(response.body().data);
                    } else {
                        // fallback: single Accessory item if API missing or empty
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
}
