package com.example.nikestore.model;

public class Product {
    public int id;
    public String name;
    public double price;
    public String image_url; // trùng tên JSON

    public Product() {
    }

    public Product(int id, String name, double price, String image_url) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.image_url = image_url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", image_url='" + image_url + '\'' +
                '}';
    }
}
