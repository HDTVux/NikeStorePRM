package com.vux.store.func;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.vux.store.R;
import com.vux.store.model.ApiResponse;
import com.vux.store.net.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyOtpActivity extends AppCompatActivity {
    EditText edtOtp;
    Button btnVerify;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        edtOtp = findViewById(R.id.edtOtp);
        btnVerify = findViewById(R.id.btnVerify);
        email = getIntent().getStringExtra("email");

        btnVerify.setOnClickListener(v -> verifyOtp());
    }

    private void verifyOtp() {
        String otp = edtOtp.getText().toString().trim();
        if (otp.isEmpty()) {
            edtOtp.setError("Nhập mã OTP");
            return;
        }

        btnVerify.setEnabled(false);
        btnVerify.setText("Đang kiểm tra...");

        RetrofitClient.api().verifyOtp(email, otp).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                btnVerify.setEnabled(true);
                btnVerify.setText("Xác nhận");

                if (!response.isSuccessful() || response.body() == null) {
                    toast("Lỗi phản hồi server");
                    return;
                }

                ApiResponse res = response.body();
                if (res.success) {
                    toast("OTP hợp lệ");
                    Intent i = new Intent(VerifyOtpActivity.this, ResetPasswordActivity.class);
                    i.putExtra("email", email);
                    startActivity(i);
                    finish();
                } else {
                    toast(res.message);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                btnVerify.setEnabled(true);
                btnVerify.setText("Xác nhận");
                toast("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
