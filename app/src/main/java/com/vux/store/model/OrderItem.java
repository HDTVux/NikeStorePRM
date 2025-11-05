package com.vux.store.model;

public class OrderItem {
    public int product_id;
    public String product_name;
    public String image_url;
    public int quantity;
    public double price;
    // Removed public double total; as it's now calculated in the adapter

    // NEW: Các trường cho chương trình khuyến mãi
    public double discount_percent;
    public double final_price;
}
