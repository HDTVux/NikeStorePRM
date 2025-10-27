package com.example.nikestore.model;

import java.util.List;

public class Product {
    public int id;
    public String name;
    public String description;
    public double price; // Giá gốc
    public int stock;
    public String image_url;   // main image (nếu có)
    public String size_type;   // optional: 'shoe','clothing','one-size'
    public double avg_rating;
    public int rating_count;

    // NEW: Các trường cho chương trình khuyến mãi
    public int product_id; // NEW: Thêm trường product_id để khớp với API promotions
    public double discount_percent;
    public double final_price; // Giá sau khuyến mãi

    // Các trường được di chuyển vào đây để khớp với JSON
    public List<String> images;
    public List<ProductVariant> variants;

    public Product() {}
}
