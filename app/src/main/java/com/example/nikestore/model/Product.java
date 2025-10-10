package com.example.nikestore.model;

import java.util.List;

public class Product {
    public int id;
    public String name;
    public String description;
    public double price;
    public int stock;
    public String image_url;   // main image (nếu có)
    public String size_type;   // optional: 'shoe','clothing','one-size'
    public double avg_rating;
    public int rating_count;

    // Các trường được di chuyển vào đây để khớp với JSON
    public List<String> images;
    public List<ProductVariant> variants;

    public Product() {}
}
