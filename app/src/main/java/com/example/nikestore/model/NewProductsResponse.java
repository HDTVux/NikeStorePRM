package com.example.nikestore.model;

import java.util.List;

public class NewProductsResponse {
    public boolean success;
    public List<Product> products;
    public List<Product> data;   // <-- accept "data" from server

    public List<Product> getProductsOrData() {
        if (products != null) return products;
        if (data != null) return data;
        return null;
    }
}
