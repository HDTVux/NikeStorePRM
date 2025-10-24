package com.example.nikestore.model;

import com.google.gson.annotations.SerializedName;

public class User {
    public int id;
    public String email;
    public String username;
    public int gender;
    public String address;
    public String role;
    public int is_active; // Đã thay đổi từ boolean sang int để khớp với tinyint(1) từ backend

    @SerializedName("created_at")
    public String createdAt;

    public User() {
    }

    public User(int id, String email, String username, int gender, String address, String role, int is_active, String createdAt) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.gender = gender;
        this.address = address;
        this.role = role;
        this.is_active = is_active; 
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getIs_active() { // Getter cho is_active
        return is_active;
    }

    public void setIs_active(int is_active) { // Setter cho is_active
        this.is_active = is_active;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", gender=" + gender +
                ", address='" + address + '\'' +
                ", role='" + role + '\'' +
                ", is_active=" + is_active +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}
