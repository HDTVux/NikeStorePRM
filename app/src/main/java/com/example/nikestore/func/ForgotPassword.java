package com.example.nikestore.func;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nikestore.R;
import com.example.nikestore.net.RetrofitClient;
import com.example.nikestore.model.ApiResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPassword extends AppCompatActivity {
    EditText edtEmail;
    Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        edtEmail = findViewById(R.id.sendTo);
        btnSend = findViewById(R.id.btnSend);

        btnSend.setOnClickListener(v -> sendOtp());
    }

    private void sendOtp() {
        String email = edtEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Vui lòng nhập email");
            return;
        }

        btnSend.setEnabled(false);
        btnSend.setText("Đang gửi...");

        RetrofitClient.api().requestOtp(email).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                btnSend.setEnabled(true);
                btnSend.setText("Send");

                if (!response.isSuccessful() || response.body() == null) {
                    toast("Lỗi phản hồi server");
                    return;
                }

                ApiResponse res = response.body();
                if (res.success) {
                    toast("Đã gửi OTP đến email");
                    Intent i = new Intent(ForgotPassword.this, VerifyOtpActivity.class);
                    i.putExtra("email", email);
                    startActivity(i);
                    finish();
                } else {
                    toast(res.message);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                btnSend.setEnabled(true);
                btnSend.setText("Send");
                toast("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
