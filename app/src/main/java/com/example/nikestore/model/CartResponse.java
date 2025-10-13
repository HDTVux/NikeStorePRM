package com.example.nikestore.model;

import java.util.List;

public class CartResponse {
    public boolean success;
    public List<CartItem> items;
    public double total;
    public int count;
}
