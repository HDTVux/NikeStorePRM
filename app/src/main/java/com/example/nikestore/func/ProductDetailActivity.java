package com.example.nikestore.func;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.nikestore.R;
import com.example.nikestore.adapter.ImageSliderAdapter;
import com.example.nikestore.adapter.ReviewAdapter;
import com.example.nikestore.adapter.SizeAdapter;
import com.example.nikestore.model.Product;
import com.example.nikestore.model.ProductDetailResponse;
import com.example.nikestore.model.ProductVariant;
import com.example.nikestore.model.Review;
import com.example.nikestore.model.ReviewsResponse;
import com.example.nikestore.net.RetrofitClient;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {
    private ViewPager2 vpImages;
    private TabLayout indicator;
    private TextView tvTitle, tvPrice, tvDescription, tvQuantity, tvTotalPrice;
    private RecyclerView rvSizes;
    private ImageButton btnBack, btnMinus, btnPlus;
    private Button btnAddToCart;
    private DecimalFormat money = new DecimalFormat("#,##0.##");

    private int quantity = 1;
    private Product current;
    private List<String> images = new ArrayList<>();
    private List<ProductVariant> variants = new ArrayList<>();
    private int selectedVariantId = -1;
    private double activePrice = 0.0;
    private RecyclerView rvReviews;
    private ReviewAdapter reviewAdapter;
    private TextView tvNoReviews;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // bind views
        vpImages = findViewById(R.id.vpImages);
        indicator = findViewById(R.id.indicator);
        tvTitle = findViewById(R.id.tvTitle);
        tvPrice = findViewById(R.id.tvPrice);
        tvDescription = findViewById(R.id.tvDescription);
        tvQuantity = findViewById(R.id.tvQuantity);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        rvSizes = findViewById(R.id.rvSizes);
        btnBack = findViewById(R.id.btnBack);
        btnMinus = findViewById(R.id.btnMinus);
        btnPlus = findViewById(R.id.btnPlus);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        rvReviews = findViewById(R.id.rvReviews);
        tvNoReviews = findViewById(R.id.tvNoReviews);
        // listeners
        btnBack.setOnClickListener(v -> finish());
        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
                updateTotal();
            }
        });
        btnPlus.setOnClickListener(v -> {
            quantity++;
            tvQuantity.setText(String.valueOf(quantity));
            updateTotal();
        });
        btnAddToCart.setOnClickListener(v -> {
            // tạm: show toast — bạn có thể triển khai logic thêm vào giỏ hàng
            Toast.makeText(this, "Add to cart (variantId=" + selectedVariantId + ", qty=" + quantity + ")", Toast.LENGTH_SHORT).show();
        });

        // get product id
        int productId = getIntent().getIntExtra("product_id", 0);
        if (productId <= 0) {
            Toast.makeText(this, "Missing product", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // init review adapter
        reviewAdapter = new ReviewAdapter();
        rvReviews.setAdapter(reviewAdapter);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));

        // load
        loadProductDetail(productId);
        loadReviews(productId);
    }

    private void loadProductDetail(int productId) {
        RetrofitClient.api().getProductDetails(productId)
                .enqueue(new Callback<ProductDetailResponse>() {
                    @Override
                    public void onResponse(Call<ProductDetailResponse> call, Response<ProductDetailResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().success) {
                            ProductDetailResponse body = response.body();

                            // product
                            current = body.product;
                            if (current == null) {
                                Toast.makeText(ProductDetailActivity.this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            
                            setupInfoFromProduct(current);
                            
                            // images
                            images = (current.images != null && !current.images.isEmpty()) ? current.images : new ArrayList<>();
                            if (images.isEmpty() && current.image_url != null && !current.image_url.isEmpty()) {
                                images.add(current.image_url);
                            }
                            setupImageSlider(images);

                            // variants / sizes
                            variants = current.variants != null ? current.variants : new ArrayList<>();
                            setupSizes();

                        } else {
                            String raw = "";
                            try { raw = response.errorBody() != null ? response.errorBody().string() : ""; } catch (Exception ignored) {}
                            Log.e("PRODUCT_DETAIL", "unexpected response, code=" + response.code() + " raw=" + raw);
                            Toast.makeText(ProductDetailActivity.this, "Không lấy được chi tiết sản phẩm", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ProductDetailResponse> call, Throwable t) {
                        Log.e("PRODUCT_DETAIL", "network error", t);
                        Toast.makeText(ProductDetailActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void loadReviews(int productId) {
        RetrofitClient.api().getProductReviews(productId).enqueue(new Callback<ReviewsResponse>() {
            @Override
            public void onResponse(Call<ReviewsResponse> call, Response<ReviewsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    List<Review> list = response.body().reviews;
                    if (list == null || list.isEmpty()) {
                        tvNoReviews.setVisibility(View.VISIBLE);
                        rvReviews.setVisibility(View.GONE);
                    } else {
                        tvNoReviews.setVisibility(View.GONE);
                        rvReviews.setVisibility(View.VISIBLE);
                        reviewAdapter.submitList(list);
                    }
                } else {
                    tvNoReviews.setVisibility(View.VISIBLE);
                    rvReviews.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ReviewsResponse> call, Throwable t) {
                tvNoReviews.setVisibility(View.VISIBLE);
                rvReviews.setVisibility(View.GONE);
            }
        });
    }


    private void setupImageSlider(List<String> urls) {
        ImageSliderAdapter adapter = new ImageSliderAdapter(urls);
        vpImages.setAdapter(adapter);
        // attach indicator (dots)
        new TabLayoutMediator(indicator, vpImages, (tab, position) -> { /* nothing */ }).attach();
    }

    private void setupInfoFromProduct(Product p) {
        tvTitle.setText(p.name != null ? p.name : "");
        tvPrice.setText("$" + money.format(p.price));
        tvDescription.setText(p.description != null ? p.description : "");
        activePrice = p.price;
        tvQuantity.setText(String.valueOf(quantity));
        updateTotal();
    }

    private void setupSizes() {
        // SizeAdapter: giả sử có constructor nhận list<ProductVariant> và callback setOnSizeSelected
        SizeAdapter sizeAdapter = new SizeAdapter(variants);
        sizeAdapter.setOnSizeSelected((variantId, sizeLabel) -> {
            selectedVariantId = variantId;
            // update price nếu variant có price
            double newPrice = activePrice;
            for (ProductVariant v : variants) {
                if (v != null && v.id == variantId) {
                    if (v.price != null) newPrice = v.price;
                    break;
                }
            }
            activePrice = newPrice;
            tvPrice.setText("$" + money.format(activePrice));
            updateTotal();
        });

        rvSizes.setAdapter(sizeAdapter);
        rvSizes.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvSizes.setHasFixedSize(true);
    }

    private void updateTotal() {
        double total = activePrice * quantity;
        tvTotalPrice.setText("Total: $" + money.format(total));
    }
}
