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

    // --- Review Section ---
    private RecyclerView rvReviews;
    private TextView tvReviewsHeader, tvNoReviews;
    private ReviewAdapter reviewAdapter;
    private List<Review> allReviews = new ArrayList<>();
    private boolean isShowingAllReviews = false;
    private static final int INITIAL_REVIEWS_TO_SHOW = 2;


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
        tvReviewsHeader = findViewById(R.id.tvReviewsHeader);
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
            Toast.makeText(this, "Add to cart (variantId=" + selectedVariantId + ", qty=" + quantity + ")", Toast.LENGTH_SHORT).show();
        });
        tvReviewsHeader.setOnClickListener(v -> {
            if (!allReviews.isEmpty()) {
                isShowingAllReviews = !isShowingAllReviews;
                updateReviewsView();
            }
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
        rvReviews.setNestedScrollingEnabled(false);

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
                            current = body.product;
                            if (current == null) {
                                Toast.makeText(ProductDetailActivity.this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            setupInfoFromProduct(current);

                            images = (current.images != null && !current.images.isEmpty()) ? current.images : new ArrayList<>();
                            if (images.isEmpty() && current.image_url != null && !current.image_url.isEmpty()) {
                                images.add(current.image_url);
                            }
                            setupImageSlider(images);
                            variants = current.variants != null ? current.variants : new ArrayList<>();
                            setupSizes();
                        } else {
                            Log.e("PRODUCT_DETAIL", "unexpected response, code=" + response.code());
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
                allReviews.clear();
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    if (response.body().reviews != null) {
                        allReviews.addAll(response.body().reviews);
                    }
                }
                // always update view, even on failure or empty list
                updateReviewsView();
            }

            @Override
            public void onFailure(Call<ReviewsResponse> call, Throwable t) {
                allReviews.clear();
                updateReviewsView();
            }
        });
    }

    private void updateReviewsView() {
        if (allReviews.isEmpty()) {
            tvNoReviews.setVisibility(View.VISIBLE);
            rvReviews.setVisibility(View.GONE);
            tvReviewsHeader.setText("Reviews");
            tvReviewsHeader.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        } else {
            tvNoReviews.setVisibility(View.GONE);
            rvReviews.setVisibility(View.VISIBLE);

            if (isShowingAllReviews) {
                // SỬA LỖI: Gửi một bản sao mới của danh sách đầy đủ
                reviewAdapter.submitList(new ArrayList<>(allReviews));
                tvReviewsHeader.setText("Reviews (Hide)");
                tvReviewsHeader.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_up, 0);
            } else {
                int count = Math.min(INITIAL_REVIEWS_TO_SHOW, allReviews.size());
                List<Review> sublist = new ArrayList<>(allReviews.subList(0, count));
                reviewAdapter.submitList(sublist);

                if (allReviews.size() > INITIAL_REVIEWS_TO_SHOW) {
                    tvReviewsHeader.setText("Reviews (Show all " + allReviews.size() + ")");
                    tvReviewsHeader.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0);
                } else {
                    tvReviewsHeader.setText("Reviews");
                    tvReviewsHeader.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }
            }
        }
    }

    private void setupImageSlider(List<String> urls) {
        ImageSliderAdapter adapter = new ImageSliderAdapter(urls);
        vpImages.setAdapter(adapter);
        new TabLayoutMediator(indicator, vpImages, (tab, position) -> {}).attach();
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
        SizeAdapter sizeAdapter = new SizeAdapter(variants);
        sizeAdapter.setOnSizeSelected((variantId, sizeLabel) -> {
            selectedVariantId = variantId;
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
