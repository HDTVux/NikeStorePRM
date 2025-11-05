package com.vux.store.model;

public class OrderStatusResponse {
    public boolean success;
    public Order order;
    public Payment payment;
    public String message;

    public static class Order {
        public int id;
        public String status;
        public double total_price;
        public double shipping_fee;
        public double subtotal;
        public String payment_method;
    }

    public static class Payment {
        public int id;
        public String payment_method;
        public double amount;
        public String status;
        public String transaction_id;
        public String created_at;
    }
}
