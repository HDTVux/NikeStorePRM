package com.example.nikestore.model;

public class OrderItem {
    public int product_id;
    public String product_name;
    public String image_url;
    public int quantity;
    public double price;
    // Removed public double total; as it's now calculated in the adapter
}
