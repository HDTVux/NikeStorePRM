package com.example.nikestore.func;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nikestore.R;
import com.example.nikestore.model.ApiResponse;
import com.example.nikestore.net.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {
    EditText edtPass, edtConfirm;
    Button btnReset;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        edtPass = findViewById(R.id.edtPass);
        edtConfirm = findViewById(R.id.edtConfirm);
        btnReset = findViewById(R.id.btnReset);
        email = getIntent().getStringExtra("email");

        btnReset.setOnClickListener(v -> doReset());
    }

    private void doReset() {
        String pass = edtPass.getText().toString().trim();
        String confirm = edtConfirm.getText().toString().trim();

        if (pass.isEmpty() || confirm.isEmpty()) {
            toast("Nhập đầy đủ mật khẩu");
            return;
        }
        if (!pass.equals(confirm)) {
            toast("Mật khẩu không khớp");
            return;
        }

        btnReset.setEnabled(false);
        btnReset.setText("Đang xử lý...");

        RetrofitClient.api().resetPassword(email, pass).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                btnReset.setEnabled(true);
                btnReset.setText("Đặt lại mật khẩu");

                if (!response.isSuccessful() || response.body() == null) {
                    toast("Lỗi phản hồi server");
                    return;
                }

                ApiResponse res = response.body();
                if (res.success) {
                    toast("Đổi mật khẩu thành công");
                    Intent i = new Intent(ResetPasswordActivity.this, Login.class);
                    startActivity(i);
                    finish();
                } else {
                    toast(res.message);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                btnReset.setEnabled(true);
                btnReset.setText("Đặt lại mật khẩu");
                toast("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
