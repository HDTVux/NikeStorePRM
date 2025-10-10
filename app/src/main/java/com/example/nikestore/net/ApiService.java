package com.example.nikestore.net;

import com.example.nikestore.model.ApiResponse;
import com.example.nikestore.model.BannerResponse;
import com.example.nikestore.model.CategoriesResponse;
import com.example.nikestore.model.NewProductsResponse;
import com.example.nikestore.model.ProductDetailResponse;

import retrofit2.Call;
import retrofit2.http.Field;
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

    // Convenience overloads
    default Call<NewProductsResponse> getProductsByCategoryDefault(int categoryId) {
        return getProductsByCategory(categoryId, 1, 50, "newest");
    }
}
