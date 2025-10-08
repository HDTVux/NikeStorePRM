package com.example.nikestore.model;

import java.util.List;

public class CategoriesResponse {
    public boolean success;
    public List<Category> data;
    public Meta meta;
    public static class Meta { public int count; }
}
