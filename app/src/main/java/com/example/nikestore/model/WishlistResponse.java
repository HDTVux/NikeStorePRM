package com.example.nikestore.model;

import java.util.List;

public class WishlistResponse {
    public boolean success;
    public String message; // Optional, for error messages
    public List<Product> wishlist;
}
