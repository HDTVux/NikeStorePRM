package com.vux.store.model;

import com.google.gson.annotations.SerializedName;

public class ChatMessage {
    @SerializedName("id")
    public int id;
    @SerializedName("sender_id")
    public int senderId;
    @SerializedName("sender_name")
    public String senderName;
    @SerializedName("is_admin")
    public boolean isAdmin;
    @SerializedName("message")
    public String message;
    @SerializedName("created_at")
    public String createdAt;

    public ChatMessage(int id, int senderId, String senderName, boolean isAdmin, String message, String createdAt) {
        this.id = id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.isAdmin = isAdmin;
        this.message = message;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public int getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public boolean isAdmin() { return isAdmin; }
    public String getMessage() { return message; }
    public String getCreatedAt() { return createdAt; }
}
