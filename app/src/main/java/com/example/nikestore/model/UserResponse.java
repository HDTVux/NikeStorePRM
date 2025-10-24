package com.example.nikestore.model;

public class UserResponse {
    public boolean success;
    public String message;
    public User user;

    public UserResponse(boolean success, String message, User user) {
        this.success = success;
        this.message = message;
        this.user = user;
    }
}
