package com.example.nikestore.model;

import java.util.List;

public class OrderDetailResponse {
    public boolean success;
    public Order order;
    public List<OrderItem> items;
    public Payment payment;
}

