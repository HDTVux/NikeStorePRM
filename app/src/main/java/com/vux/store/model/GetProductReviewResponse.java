package com.vux.store.model;

public class GetProductReviewResponse {
    public boolean success;
    public Review review; // Có thể là null nếu không tìm thấy đánh giá
}
