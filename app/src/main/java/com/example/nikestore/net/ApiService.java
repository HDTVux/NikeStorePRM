package com.example.nikestore.net;

import com.example.nikestore.model.ApiResponse;
import com.example.nikestore.model.BannerResponse;
import com.example.nikestore.model.CartResponse;
import com.example.nikestore.model.CategoriesResponse;
import com.example.nikestore.model.NewProductsResponse;
import com.example.nikestore.model.OrderDetailResponse;
import com.example.nikestore.model.OrdersResponse;
import com.example.nikestore.model.ProductDetailResponse;
import com.example.nikestore.model.ReviewsResponse;
import com.example.nikestore.model.GetProductReviewResponse;
import com.example.nikestore.model.SubmitReviewRequest;
import com.example.nikestore.model.UserResponse; // Import UserResponse
import com.example.nikestore.model.WishlistResponse; // Import WishlistResponse

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    // LOGIN
    @FormUrlEncoded
    @POST("api.php?action=login")
    Call<ApiLoginResponse> login(
            @Field("username") String username,
            @Field("password") String password
    );

    // REGISTER
    @FormUrlEncoded
    @POST("api.php?action=register")
    Call<ApiLoginResponse> register(
            @Field("email") String email,
            @Field("username") String username,
            @Field("password") String password,
            @Field("gender") int gender
    );

    @FormUrlEncoded
    @POST("api.php?action=request_otp")
    Call<ApiResponse> requestOtp(@Field("email") String email);

    @FormUrlEncoded
    @POST("api.php?action=verify_otp")
    Call<ApiResponse> verifyOtp(@Field("email") String email, @Field("otp") String otp);

    @FormUrlEncoded
    @POST("api.php?action=reset_password")
    Call<ApiResponse> resetPassword(@Field("email") String email, @Field("password") String newPassword);

    // BANNERS
    @GET("api.php?action=get_banners")
    Call<BannerResponse> getBanners();

    // NEW PRODUCTS
    @GET("api.php?action=get_new_products")
    Call<NewProductsResponse> getNewProducts();

    // CATEGORIES
    @GET("api.php?action=get_categories")
    Call<CategoriesResponse> getCategories();

    // PRODUCTS BY CATEGORY (pageable)
    // call: api.php?action=get_products_by_category&category_id=3&page=1&page_size=50&sort=newest
    @GET("api.php?action=get_products_by_category")
    Call<NewProductsResponse> getProductsByCategory(
            @Query("category_id") int categoryId,
            @Query("page") int page,
            @Query("page_size") int pageSize,
            @Query("sort") String sort
    );

    // PRODUCT DETAILS (CHÍNH THỨC: action=get_product_details, param product_id)
    @GET("api.php?action=get_product_details")
    Call<ProductDetailResponse> getProductDetails(@Query("product_id") int productId);

    // GET PRODUCT REVIEWS (CHÍNH THỨC: action=get_product_reviews, param product_id)
    @GET("api.php?action=get_product_reviews")
    Call<ReviewsResponse> getProductReviews(@Query("product_id") int productId);

    // NEW: Get single product review by user
    @GET("api.php?action=get_product_review")
    Call<GetProductReviewResponse> getProductReview(
            @Query("user_id") int userId,
            @Query("product_id") int productId
    );

    // NEW: Submit/Update product review
    @POST("api.php?action=submit_review")
    Call<ApiResponse> submitReview(@Body SubmitReviewRequest request);

    // add to cart (POST form)
    @FormUrlEncoded
    @POST("api.php?action=add_to_cart")
    Call<com.example.nikestore.model.ApiResponse> addToCart(
            @Field("user_id") int userId,
            @Field("product_id") int productId,
            @Field("variant_id") Integer variantId,
            @Field("quantity") int quantity
    );

    // get cart
    @GET("api.php?action=get_cart")
    Call<com.example.nikestore.model.CartResponse> getCart(@Query("user_id") int userId);

    // update item
    @FormUrlEncoded
    @POST("api.php?action=update_cart_item")
    Call<com.example.nikestore.model.ApiResponse> updateCartItem(
            @Field("item_id") int itemId,
            @Field("quantity") int quantity
    );

    // remove item
    @FormUrlEncoded
    @POST("api.php?action=remove_cart_item")
    Call<com.example.nikestore.model.ApiResponse> removeCartItem(@Field("item_id") int itemId);

    // clear cart
    @FormUrlEncoded
    @POST("api.php?action=clear_cart")
    Call<com.example.nikestore.model.ApiResponse> clearCart(@Field("user_id") int userId);

    // merge cart (POST JSON)
    @POST("api.php?action=merge_cart")
    Call<com.example.nikestore.model.ApiResponse> mergeCart(@Body Map<String, Object> body);

    // in ApiService.java
    @POST("api.php?action=create_order")
    Call<com.example.nikestore.model.ApiResponse> createOrder(@Body Map<String,Object> body);

    @POST("api.php?action=create_vnpay_payment")
    Call<com.example.nikestore.model.VnPayResponse> createVnPayPayment(@Body Map<String,Object> body);

    // get order status (server verification)
    @GET("api.php?action=get_order_status")
    Call<com.example.nikestore.model.OrderStatusResponse> getOrderStatus(@Query("order_id") int orderId);

    // trong ApiService.java
    @FormUrlEncoded
    @POST("api.php?action=verify_vnpay_return")
    Call<com.example.nikestore.model.ApiResponse> verifyVnpayReturn(@FieldMap Map<String, String> params);

    // SEARCH PRODUCTS
    @GET("api.php?action=search_products")
    Call<NewProductsResponse> searchProducts(
            @Query("q") String query,
            @Query("page") int page,
            @Query("page_size") int pageSize
    );

    @GET("api.php")
    Call<OrdersResponse> getUserOrders(
            @Query("action") String action,
            @Query("user_id") int userId
    );

    @GET("api.php")
    Call<OrderDetailResponse> getOrderDetail(
            @Query("action") String action,
            @Query("order_id") int orderId,
            @Query("user_id") int userId
    );

    // NEW: Get user profile
    @GET("api.php")
    Call<UserResponse> getUserProfile(
            @Query("action") String action,
            @Query("user_id") int userId
    );

    // NEW: Update user profile
    @POST("api.php")
    Call<UserResponse> updateUserProfile(
            @Query("action") String action,
            @Body Map<String, Object> body
    );

    // ================== Add/Remove/Get favorite ==================
    @FormUrlEncoded
    @POST("api.php?action=add_favorite")
    Call<ApiResponse> addFavorite(@Field("user_id") int userId, @Field("product_id") int productId);

    @FormUrlEncoded
    @POST("api.php?action=remove_favorite")
    Call<ApiResponse> removeFavorite(@Field("user_id") int userId, @Field("product_id") int productId);

    @GET("api.php?action=get_favorite") // Note: Backend uses GET for get_favorite
    Call<WishlistResponse> getFavorites(@Query("user_id") int userId);

    // Convenience overloads
    default Call<NewProductsResponse> getProductsByCategoryDefault(int categoryId) {
        return getProductsByCategory(categoryId, 1, 50, "newest");
    }

    default Call<NewProductsResponse> searchProducts(String q) {
        return searchProducts(q, 1, 50);
    }
}
