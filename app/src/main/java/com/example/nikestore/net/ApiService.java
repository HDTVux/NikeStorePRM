package com.example.nikestore.net;

import com.example.nikestore.model.ApiResponse;
import com.example.nikestore.model.BannerResponse;
import com.example.nikestore.model.CategoriesResponse;
import com.example.nikestore.model.NewProductsResponse;
import com.example.nikestore.model.ProductDetailResponse;
import com.example.nikestore.model.ReviewsResponse;

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



    // Convenience overloads
    default Call<NewProductsResponse> getProductsByCategoryDefault(int categoryId) {
        return getProductsByCategory(categoryId, 1, 50, "newest");
    }

    default Call<NewProductsResponse> searchProducts(String q) {
        return searchProducts(q, 1, 50);
    }


}
