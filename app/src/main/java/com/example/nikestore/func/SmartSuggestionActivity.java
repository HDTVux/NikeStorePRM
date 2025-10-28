package com.example.nikestore.func;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nikestore.R;
import com.example.nikestore.model.SmartSuggestionResponse;
import com.example.nikestore.net.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SmartSuggestionActivity extends AppCompatActivity {

    private EditText edtUserQuery;
    private Button btnGetSuggestions;
    private ProgressBar progressBar;
    private TextView tvSuggestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_suggestion);

        edtUserQuery = findViewById(R.id.edtUserQuery);
        btnGetSuggestions = findViewById(R.id.btnGetSuggestions);
        progressBar = findViewById(R.id.progressBar);
        tvSuggestions = findViewById(R.id.tvSuggestions);

        btnGetSuggestions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSmartSuggestions();
            }
        });
    }

    private void getSmartSuggestions() {
        String query = edtUserQuery.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập câu hỏi của bạn.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvSuggestions.setText("Đang tìm kiếm gợi ý...");

        Map<String, String> body = new HashMap<>();
        body.put("query", query);

        RetrofitClient.api().getSmartSuggestions(body).enqueue(new Callback<SmartSuggestionResponse>() {
            @Override
            public void onResponse(Call<SmartSuggestionResponse> call, Response<SmartSuggestionResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    SmartSuggestionResponse suggestionResponse = response.body();
                    if (suggestionResponse.success) {
                        tvSuggestions.setText(suggestionResponse.suggestions);
                    } else {
                        tvSuggestions.setText("Lỗi từ AI: " + suggestionResponse.message);
                        Toast.makeText(SmartSuggestionActivity.this, "Lỗi: " + suggestionResponse.message, Toast.LENGTH_LONG).show();
                    }
                } else {
                    tvSuggestions.setText("Lỗi phản hồi từ máy chủ.");
                    Toast.makeText(SmartSuggestionActivity.this, "Lỗi phản hồi: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<SmartSuggestionResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvSuggestions.setText("Lỗi kết nối mạng.");
                Toast.makeText(SmartSuggestionActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
