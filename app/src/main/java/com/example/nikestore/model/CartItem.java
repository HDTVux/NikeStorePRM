package com.example.nikestore.model;

public class CartItem {
    public int item_id;
    public int product_id;
    public String product_name;
    public double price;
    public int quantity;
    public Double subtotal; // or double
    public Integer variant_id; // nullable
    public String variant_size;
    public String image_url;
    public double unitPrice;

    public CartItem() {
    }

    public CartItem(int item_id, int product_id, String product_name, double price, int quantity, Double subtotal, Integer variant_id, String variant_size, String image_url, double unitPrice) {
        this.item_id = item_id;
        this.product_id = product_id;
        this.product_name = product_name;
        this.price = price;
        this.quantity = quantity;
        this.subtotal = subtotal;
        this.variant_id = variant_id;
        this.variant_size = variant_size;
        this.image_url = image_url;
        this.unitPrice = unitPrice;
    }

    public int getItem_id() {
        return item_id;
    }

    public void setItem_id(int item_id) {
        this.item_id = item_id;
    }

    public int getProduct_id() {
        return product_id;
    }

    public void setProduct_id(int product_id) {
        this.product_id = product_id;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }

    public Integer getVariant_id() {
        return variant_id;
    }

    public void setVariant_id(Integer variant_id) {
        this.variant_id = variant_id;
    }

    public String getVariant_size() {
        return variant_size;
    }

    public void setVariant_size(String variant_size) {
        this.variant_size = variant_size;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }
}
