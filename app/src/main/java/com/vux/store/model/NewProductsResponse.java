package com.vux.store.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NewProductsResponse {
    public boolean success;

    @SerializedName("products")
    public List<Product> products;

    @SerializedName("data")
    public List<Product> data;

    public List<Product> getProductList() {
        if (products != null) return products;
        return data;
    }
}
