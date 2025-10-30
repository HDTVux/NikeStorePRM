package com.example.nikestore.func;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nikestore.R;
import com.example.nikestore.adapter.ChatAdapter;
import com.example.nikestore.model.ApiResponse;
import com.example.nikestore.model.ChatMessage;
import com.example.nikestore.model.ChatResponse;
import com.example.nikestore.model.MessageListResponse;
import com.example.nikestore.net.RetrofitClient;
import com.example.nikestore.net.ApiService;
import com.example.nikestore.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText messageEditText;
    private ImageButton sendMessageButton;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;

    private int currentChatId = -1; // -1 indicates no active chat
    private SessionManager sessionManager;
    private ApiService apiService;
    private int userId; // Current logged-in user ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize views
        recyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendMessageButton = findViewById(R.id.sendMessageButton);

        // Initialize SessionManager and ApiService
        sessionManager = SessionManager.getInstance(this);
        apiService = RetrofitClient.api();

        // Get current user ID
        userId = sessionManager.getUserId();
        if (userId <= 0) {
            Toast.makeText(this, "Vui lòng đăng nhập để chat.", Toast.LENGTH_LONG).show();
            finish(); // Close chat if not logged in
            return;
        }

        // Setup RecyclerView
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList, userId); // Pass current user ID for message alignment
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        // Setup send message button
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        // Start or load chat
        createOrLoadChat();
    }

    private void createOrLoadChat() {
        apiService.createChat(userId).enqueue(new Callback<ChatResponse>() { // Assuming a ChatResponse model for create_chat
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    currentChatId = response.body().chat_id;
                    Log.d("ChatActivity", "Chat ID: " + currentChatId + ", Message: " + response.body().message);
                    loadMessages(); // Load existing messages for this chat
                } else {
                    Toast.makeText(ChatActivity.this, "Không thể tạo/tải cuộc trò chuyện: " + (response.body() != null ? response.body().message : response.message()), Toast.LENGTH_LONG).show();
                    Log.e("ChatActivity", "Error creating/loading chat: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                Toast.makeText(ChatActivity.this, "Lỗi mạng khi tạo/tải chat: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("ChatActivity", "Network error on create/load chat", t);
            }
        });
    }

    private void loadMessages() {
        if (currentChatId == -1) {
            Log.w("ChatActivity", "No active chat ID to load messages.");
            return;
        }
        apiService.getMessages(currentChatId).enqueue(new Callback<MessageListResponse>() { // Assuming MessageListResponse model
            @Override
            public void onResponse(Call<MessageListResponse> call, Response<MessageListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    messageList.clear();
                    if (response.body().messages != null) {
                        messageList.addAll(response.body().messages);
                    }
                    chatAdapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(messageList.size() - 1); // Scroll to latest message
                } else {
                    Toast.makeText(ChatActivity.this, "Không thể tải tin nhắn: " + (response.body() != null ? response.body().message : response.message()), Toast.LENGTH_SHORT).show();
                    Log.e("ChatActivity", "Error loading messages: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<MessageListResponse> call, Throwable t) {
                Toast.makeText(ChatActivity.this, "Lỗi mạng khi tải tin nhắn: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("ChatActivity", "Network error on load messages", t);
            }
        });
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (messageText.isEmpty()) {
            Toast.makeText(this, "Tin nhắn không được trống.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentChatId == -1) {
            Toast.makeText(this, "Không có cuộc trò chuyện đang hoạt động.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Clear input field immediately
        messageEditText.setText("");

        // Create a temporary ChatMessage to display immediately (optimistic update)
        ChatMessage tempMessage = new ChatMessage(
                0, // ID will be updated by server
                userId,
                sessionManager.getUsername(), // Assuming SessionManager provides username
                false, // User is not admin
                messageText,
                "Đang gửi..." // Placeholder timestamp
        );
        messageList.add(tempMessage);
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);

        // Send message via API
        apiService.sendMessage(currentChatId, userId, false, messageText).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    Log.d("ChatActivity", "Message sent successfully.");
                    loadMessages(); // Reload messages to get actual ID and timestamp
                } else {
                    Toast.makeText(ChatActivity.this, "Không thể gửi tin nhắn: " + (response.body() != null ? response.body().message : response.message()), Toast.LENGTH_SHORT).show();
                    Log.e("ChatActivity", "Error sending message: " + response.errorBody());
                    // Revert optimistic update or show error for the specific message
                    // For simplicity, we just reload here. A more robust solution would update tempMessage status.
                    loadMessages();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(ChatActivity.this, "Lỗi mạng khi gửi tin nhắn: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("ChatActivity", "Network error on send message", t);
                // Reload to potentially remove pending message or show error status
                loadMessages();
            }
        });
    }
}
