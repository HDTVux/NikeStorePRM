package com.vux.store.model;

import com.google.gson.annotations.SerializedName;

public class ChatResponse extends ApiResponse {
    @SerializedName("chat_id")
    public int chat_id;
}
