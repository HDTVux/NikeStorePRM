package com.example.nikestore.model;

import java.util.List;

public class OrdersResponse {
    public boolean success;
    public List<OrderSummary> orders; // Mỗi item gồm order info + payment (nếu bạn trả về cặp order/payment như backend)

    // Inner class hoặc tạo file riêng:
    public static class OrderSummary {
        public Order order;        // Đơn hàng
        public Payment payment;    // Thanh toán (có thể null)
    }
}
