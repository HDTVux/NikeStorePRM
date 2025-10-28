package com.example.nikestore.model;

import com.google.gson.annotations.SerializedName;

public class SmartSuggestionResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("query")
    public String query;

    @SerializedName("suggestions")
    public String suggestions;

    @SerializedName("context_count")
    public int contextCount;

    @SerializedName("message") // Có thể có trường message cho lỗi
    public String message;
}
