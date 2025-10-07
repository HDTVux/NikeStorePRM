package com.example.nikestore.net;

import com.example.nikestore.model.ApiResponse;
import com.example.nikestore.model.BannerResponse;
import com.example.nikestore.model.NewProductsResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
//login
    @FormUrlEncoded
    @POST("api.php?action=login")
    Call<ApiLoginResponse> login(
            @Field("username") String username,
            @Field("password") String password
    );

//register
@FormUrlEncoded
@POST("api.php?action=register")
Call<ApiLoginResponse> register(
        @Field("email") String email,
        @Field("username") String username,
        @Field("password") String password,
        @Field("gender") int gender);
    @FormUrlEncoded
    @POST("api.php?action=request_otp")
    Call<ApiResponse> requestOtp(@Field("email") String email);

    @FormUrlEncoded
    @POST("api.php?action=verify_otp")
    Call<ApiResponse> verifyOtp(@Field("email") String email, @Field("otp") String otp);

    @FormUrlEncoded
    @POST("api.php?action=reset_password")
    Call<ApiResponse> resetPassword(@Field("email") String email, @Field("password") String newPassword);

    @GET("api.php")
    Call<BannerResponse> getBanners(@Query("action") String action);

    // helper
    default Call<BannerResponse> getBanners() {
        return getBanners("get_banners");
    }

    @GET("api.php?action=get_new_products")
    Call<NewProductsResponse> getNewProducts();

}
