package com.vux.store.func;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vux.store.R;
import com.vux.store.adapter.ChatAdapter;
import com.vux.store.model.ApiResponse;
import com.vux.store.model.ChatMessage;
import com.vux.store.model.ChatResponse;
import com.vux.store.model.MessageListResponse;
import com.vux.store.net.RetrofitClient;
import com.vux.store.net.ApiService;
import com.vux.store.util.SessionManager;

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

    private int currentChatId = -1;
    private SessionManager sessionManager;
    private ApiService apiService;
    private int userId;

    // --- MỚI: Thêm Handler để tự động làm mới ---
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable refreshMessagesRunnable;
    private static final long REFRESH_INTERVAL = 5000; // 5 giây
    private boolean isActivityVisible = false;
    // ---------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendMessageButton = findViewById(R.id.sendMessageButton);

        sessionManager = SessionManager.getInstance(this);
        apiService = RetrofitClient.api();

        userId = sessionManager.getUserId();
        if (userId <= 0) {
            Toast.makeText(this, "Vui lòng đăng nhập để chat.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList, userId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        sendMessageButton.setOnClickListener(v -> sendMessage());

        createOrLoadChat();

        // --- MỚI: Khởi tạo Runnable để làm mới tin nhắn ---
        setupAutoRefresh();
        // ----------------------------------------------------
    }

    // --- MỚI: Quản lý vòng đời để bắt đầu/dừng làm mới ---
    @Override
    protected void onStart() {
        super.onStart();
        isActivityVisible = true;
        // Bắt đầu làm mới khi Activity hiển thị
        handler.post(refreshMessagesRunnable);
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActivityVisible = false;
        // Dừng làm mới khi Activity không còn hiển thị để tiết kiệm pin
        handler.removeCallbacks(refreshMessagesRunnable);
    }
    // ---------------------------------------------------------

    private void setupAutoRefresh() {
        refreshMessagesRunnable = new Runnable() {
            @Override
            public void run() {
                // Chỉ chạy nếu Activity đang hiển thị
                if (isActivityVisible) {
                    Log.d("ChatActivity", "Tự động làm mới tin nhắn...");
                    loadMessages(); // Gọi hàm tải tin nhắn
                    // Lên lịch chạy lại sau khoảng thời gian REFRESH_INTERVAL
                    handler.postDelayed(this, REFRESH_INTERVAL);
                }
            }
        };
    }

    private void createOrLoadChat() {
        apiService.createChat(userId).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    currentChatId = response.body().chat_id;
                    Log.d("ChatActivity", "Chat ID: " + currentChatId + ", Message: " + response.body().message);
                    loadMessages(); // Tải tin nhắn lần đầu
                } else {
                    Toast.makeText(ChatActivity.this, "Không thể tạo/tải cuộc trò chuyện.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                Toast.makeText(ChatActivity.this, "Lỗi mạng khi tạo/tải chat.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadMessages() {
        if (currentChatId == -1 || !isActivityVisible) { // Chỉ tải nếu có chat_id và activity đang chạy
            return;
        }

        apiService.getMessages(currentChatId).enqueue(new Callback<MessageListResponse>() {
            @Override
            public void onResponse(Call<MessageListResponse> call, Response<MessageListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    if (response.body().messages != null && response.body().messages.size() > messageList.size()) {
                        // Chỉ cập nhật nếu có tin nhắn mới
                        messageList.clear();
                        messageList.addAll(response.body().messages);
                        chatAdapter.notifyDataSetChanged();
                        recyclerView.scrollToPosition(messageList.size() - 1);
                    }
                } else {
                    // Không hiển thị Toast lỗi ở đây để tránh làm phiền người dùng mỗi 5 giây
                    Log.e("ChatActivity", "Lỗi khi tải tin nhắn tự động.");
                }
            }

            @Override
            public void onFailure(Call<MessageListResponse> call, Throwable t) {
                // Không hiển thị Toast lỗi ở đây
                Log.e("ChatActivity", "Lỗi mạng khi tải tin nhắn tự động.", t);
            }
        });
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (messageText.isEmpty() || currentChatId == -1) {
            return;
        }

        messageEditText.setText("");

        // Dừng làm mới tạm thời để tránh xung đột
        handler.removeCallbacks(refreshMessagesRunnable);

        // Gửi tin nhắn qua API (is_admin luôn là false cho user)
        apiService.sendMessage(currentChatId, userId, false, messageText).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    Log.d("ChatActivity", "Gửi tin nhắn thành công.");
                    loadMessages(); // Tải lại tin nhắn ngay sau khi gửi
                } else {
                    Toast.makeText(ChatActivity.this, "Không thể gửi tin nhắn.", Toast.LENGTH_SHORT).show();
                }
                // Bắt đầu lại vòng lặp làm mới sau khi gửi xong
                handler.postDelayed(refreshMessagesRunnable, REFRESH_INTERVAL);
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(ChatActivity.this, "Lỗi mạng khi gửi tin nhắn.", Toast.LENGTH_SHORT).show();
                // Bắt đầu lại vòng lặp làm mới ngay cả khi gửi lỗi
                handler.postDelayed(refreshMessagesRunnable, REFRESH_INTERVAL);
            }
        });
    }
}
