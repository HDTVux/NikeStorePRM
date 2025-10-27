package com.example.nikestore.func;

import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.nikestore.R;
import com.example.nikestore.adapter.ImageSliderAdapter;
import com.example.nikestore.adapter.ReviewAdapter;
import com.example.nikestore.adapter.SizeAdapter;
import com.example.nikestore.data.CartManager;
import com.example.nikestore.model.ApiResponse;
import com.example.nikestore.model.CartItem;
import com.example.nikestore.model.Product;
import com.example.nikestore.model.ProductDetailResponse;
import com.example.nikestore.model.ProductVariant;
import com.example.nikestore.model.Review;
import com.example.nikestore.model.ReviewsResponse;
import com.example.nikestore.model.WishlistResponse; // Import WishlistResponse
import com.example.nikestore.net.RetrofitClient;
import com.example.nikestore.util.SessionManager; // Import SessionManager
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends BaseActivity {
    private ViewPager2 vpImages;
    private TabLayout indicator;
    private TextView tvTitle, tvPrice, tvOriginalPrice, tvDescription, tvQuantity, tvTotalPrice; // NEW: tvOriginalPrice
    private RecyclerView rvSizes;
    private ImageButton btnBack, btnMinus, btnPlus;
    private Button btnAddToCart;
    private ImageView ivFavoriteDetail; // New: Favorite icon
    private SessionManager sessionManager; // New: SessionManager instance
    private boolean isProductFavorited = false; // New: Track favorite status

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

        sessionManager = SessionManager.getInstance(this); // Init SessionManager

        // bind views
        vpImages = findViewById(R.id.vpImages);
        indicator = findViewById(R.id.indicator);
        tvTitle = findViewById(R.id.tvTitle);
        tvPrice = findViewById(R.id.tvPrice);
        tvOriginalPrice = findViewById(R.id.tvOriginalPrice); // NEW: Bind tvOriginalPrice
        tvDescription = findViewById(R.id.tvDescription);
        tvQuantity = findViewById(R.id.tvQuantity);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        rvSizes = findViewById(R.id.rvSizes);
        btnBack = findViewById(R.id.btnBack);
        btnMinus = findViewById(R.id.btnMinus);
        btnPlus = findViewById(R.id.btnPlus);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        ivFavoriteDetail = findViewById(R.id.ivFavoriteDetail); // Bind favorite icon
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
        btnAddToCart.setOnClickListener(v->{
            int uid = sessionManager.getUserId();
            if (uid<=0) { Toast.makeText(this,"Vui lòng đăng nhập để thêm vào giỏ hàng",Toast.LENGTH_SHORT).show(); return; }
            Integer vid = selectedVariantId > 0 ? selectedVariantId : null;

            // NEW: Sử dụng giá cuối cùng (final_price) khi thêm vào giỏ hàng nếu có khuyến mãi
            double priceToAdd = (current != null && current.discount_percent > 0) ? current.final_price : current.price; // Use final price if discounted

            CartItem newItem = new CartItem();
            if (current != null) {
                newItem.setProduct_id(current.id);
                newItem.setProduct_name(current.name);
                newItem.setImage_url(current.image_url);
                newItem.setPrice(priceToAdd); // Price reflects discount now
                newItem.setQuantity(quantity);
                newItem.setVariant_id(vid);
                newItem.setDiscount_percent(current.discount_percent);
                newItem.setFinal_price(current.final_price);
                // Variant size will be set by the SizeAdapter listener if a variant is selected
            }

            // Call CartManager to add item to local cart and then sync with server
            CartManager.getInstance().addItemToCart(ProductDetailActivity.this, newItem, new CartManager.CartActionListener() {
                @Override
                public void onCartActionSuccess(String message) {
                    Toast.makeText(ProductDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCartActionFailure(String error) {
                    Toast.makeText(ProductDetailActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            });
        });

        // New: Favorite icon click listener
        ivFavoriteDetail.setOnClickListener(v -> toggleFavorite());

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

    @Override
    protected int getNavigationMenuItemId() {
        return 0; // ProductDetailActivity is not a top-level navigation destination
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
                            checkFavoriteStatus(); // New: Check favorite status after loading product
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

    private void checkFavoriteStatus() {
        int userId = sessionManager.getUserId();
        if (userId <= 0) {
            // User not logged in, cannot be favorited
            ivFavoriteDetail.setImageResource(R.drawable.ic_favorite_border);
            isProductFavorited = false;
            return;
        }
        if (current == null) return;

        RetrofitClient.api().getFavorites(userId).enqueue(new Callback<WishlistResponse>() {
            @Override
            public void onResponse(Call<WishlistResponse> call, Response<WishlistResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    List<Product> wishlist = response.body().wishlist;
                    isProductFavorited = false;
                    if (wishlist != null) {
                        for (Product p : wishlist) {
                            if (p.id == current.id) {
                                isProductFavorited = true;
                                break;
                            }
                        }
                    }
                    updateFavoriteIcon();
                } else {
                    Log.e("PRODUCT_DETAIL", "Failed to get wishlist: " + response.message());
                    updateFavoriteIcon(); // Still update icon to default if API fails
                }
            }

            @Override
            public void onFailure(Call<WishlistResponse> call, Throwable t) {
                Log.e("PRODUCT_DETAIL", "Network error checking favorite: " + t.getMessage());
                updateFavoriteIcon(); // Still update icon to default on network error
            }
        });
    }

    private void toggleFavorite() {
        int userId = sessionManager.getUserId();
        if (userId <= 0) {
            Toast.makeText(this, "Vui lòng đăng nhập để thêm vào danh sách yêu thích", Toast.LENGTH_SHORT).show();
            return;
        }
        if (current == null) return;

        if (isProductFavorited) {
            // Remove from favorite
            RetrofitClient.api().removeFavorite(userId, current.id).enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().success) {
                        isProductFavorited = false;
                        updateFavoriteIcon();
                        Toast.makeText(ProductDetailActivity.this, "Đã xóa khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProductDetailActivity.this, "Không thể xóa khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
                        Log.e("PRODUCT_DETAIL", "Remove favorite failed: " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    Toast.makeText(ProductDetailActivity.this, "Lỗi mạng khi xóa yêu thích", Toast.LENGTH_SHORT).show();
                    Log.e("PRODUCT_DETAIL", "Network error removing favorite: " + t.getMessage());
                }
            });
        } else {
            // Add to favorite
            RetrofitClient.api().addFavorite(userId, current.id).enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().success) {
                        isProductFavorited = true;
                        updateFavoriteIcon();
                        Toast.makeText(ProductDetailActivity.this, "Đã thêm vào danh sách yêu thích", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProductDetailActivity.this, "Không thể thêm vào danh sách yêu thích", Toast.LENGTH_SHORT).show();
                        Log.e("PRODUCT_DETAIL", "Add favorite failed: " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    Toast.makeText(ProductDetailActivity.this, "Lỗi mạng khi thêm yêu thích", Toast.LENGTH_SHORT).show();
                    Log.e("PRODUCT_DETAIL", "Network error adding favorite: " + t.getMessage());
                }
            });
        }
    }

    private void updateFavoriteIcon() {
        if (isProductFavorited) {
            ivFavoriteDetail.setImageResource(R.drawable.ic_favorite);
        } else {
            ivFavoriteDetail.setImageResource(R.drawable.ic_favorite_border);
        }
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
            }
            else {
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

        // NEW: Logic hiển thị giá gốc và giá khuyến mãi
        if (p.discount_percent > 0) {
            tvOriginalPrice.setText("$" + money.format(p.price));
            tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            tvOriginalPrice.setVisibility(View.VISIBLE);
            tvPrice.setText("$" + money.format(p.final_price));
            tvPrice.setTextColor(ContextCompat.getColor(this, R.color.red));
            activePrice = p.final_price;
        } else {
            tvOriginalPrice.setVisibility(View.GONE);
            tvPrice.setText("$" + money.format(p.price));
            tvPrice.setTextColor(ContextCompat.getColor(this, R.color.white)); // Màu mặc định
            activePrice = p.price;
        }

        tvDescription.setText(p.description != null ? p.description : "");
        tvQuantity.setText(String.valueOf(quantity));
        updateTotal();
    }

    private void setupSizes() {
        SizeAdapter sizeAdapter = new SizeAdapter(variants);
        sizeAdapter.setOnSizeSelected((variantId, sizeLabel) -> {
            selectedVariantId = variantId;
            double newPrice = current != null ? current.price : 0.0; // Mặc định là giá gốc của sản phẩm chính

            for (ProductVariant v : variants) {
                if (v != null && v.id == variantId) {
                    if (v.price != null) newPrice = v.price;
                    break;
                }
            }
            
            // Khi chọn size, giá hiển thị sẽ là giá của variant đó.
            // Logic discount đã được áp dụng ở setupInfoFromProduct cho giá cơ bản của sản phẩm.
            // Nếu có discount trên sản phẩm chính, thì final_price sẽ được sử dụng.
            // Giá của variant sẽ không ảnh hưởng đến final_price đã tính toán ở cấp độ sản phẩm chính.
            // Chúng ta cần đảm bảo activePrice luôn là giá sẽ được dùng để tính tổng cuối cùng.
            // Ở đây, tôi sẽ đơn giản hóa bằng cách chỉ cập nhật activePrice dựa trên giá variant.
            // Nếu bạn muốn khuyến mãi cũng áp dụng trên từng variant, logic sẽ phức tạp hơn.
            // Để giữ logic hiện tại: giá variant thay đổi base price, sau đó discount nếu có sẽ áp dụng.

            activePrice = newPrice; // Update activePrice based on selected variant price

            // NEW: Cập nhật lại hiển thị giá sau khi chọn variant
            if (current != null && current.discount_percent > 0) {
                // Nếu sản phẩm có khuyến mãi, giá của variant không thay đổi giá hiển thị đã giảm
                tvOriginalPrice.setText("$" + money.format(activePrice));
                tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvOriginalPrice.setVisibility(View.VISIBLE);
                tvPrice.setText("$" + money.format(current.final_price)); // Vẫn hiển thị final_price của sản phẩm chính
                tvPrice.setTextColor(ContextCompat.getColor(this, R.color.red));
                activePrice = current.final_price; // activePrice vẫn là giá cuối cùng đã giảm
            } else {
                tvOriginalPrice.setVisibility(View.GONE);
                tvPrice.setText("$" + money.format(activePrice));
                tvPrice.setTextColor(ContextCompat.getColor(this, R.color.white));
                activePrice = newPrice;
            }

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