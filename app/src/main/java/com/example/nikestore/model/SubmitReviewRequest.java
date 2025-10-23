package com.example.nikestore.model;

public class SubmitReviewRequest {
    public int user_id;
    public int product_id;
    public int rating;
    public String comment;

    public SubmitReviewRequest(int user_id, int product_id, int rating, String comment) {
        this.user_id = user_id;
        this.product_id = product_id;
        this.rating = rating;
        this.comment = comment;
    }
}
