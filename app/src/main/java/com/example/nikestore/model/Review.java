package com.example.nikestore.model;

public class Review {
    public int id;
    public int user_id;
    public int product_id;
    public String username; // Đã thêm trường username
    public int rating;
    public String comment;
    public String created_at;
}
