package com.vux.store.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UserChatListResponse extends ApiResponse {
    @SerializedName("chats")
    public List<ChatSummary> chats;

    // You might need a ChatSummary model as well, depending on what you want to display in a chat list.
    // For now, let's assume ChatSummary is a simple class or just re-use ChatResponse fields if they fit.
    // If ChatSummary is needed, define it as a static nested class or a separate file.
    public static class ChatSummary {
        @SerializedName("id")
        public int id;
        @SerializedName("status")
        public String status;
        @SerializedName("last_message")
        public String lastMessage;
        @SerializedName("last_message_time")
        public String lastMessageTime;
        @SerializedName("created_at")
        public String createdAt;

        // Constructor for ChatSummary
        public ChatSummary(int id, String status, String lastMessage, String lastMessageTime, String createdAt) {
            this.id = id;
            this.status = status;
            this.lastMessage = lastMessage;
            this.lastMessageTime = lastMessageTime;
            this.createdAt = createdAt;
        }
    }
}
