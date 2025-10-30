package com.example.nikestore.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MessageListResponse extends ApiResponse {
    @SerializedName("messages")
    public List<ChatMessage> messages;
}
