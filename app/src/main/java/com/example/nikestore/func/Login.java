package com.example.nikestore.func;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.nikestore.R;
import com.example.nikestore.net.ApiLoginResponse;
import com.example.nikestore.net.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Login bằng USERNAME và PASSWORD.
 * - Chỉ parse JSON OBJECT (GsonConverterFactory) => tránh lỗi "Expected BEGIN_OBJECT but was STRING".
 * - Không dùng ScalarsConverterFactory.
 */
public class Login extends AppCompatActivity {

    private EditText edtUsername, edtPassword;
    private Button btnLogin, btnRegister;
    private TextView tvForgotPassword;

    // Tên file SharedPreferences
    private static final String PREFS = "nike_store_prefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_USERID = "user_id";
    private static final String KEY_ROLE = "role";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Xử lý padding cho thanh system bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bindViews();
        bindActions();

        // Tự động điền username đã lưu (nếu có)
        String savedUsername = getSharedPreferences(PREFS, MODE_PRIVATE)
                .getString(KEY_USERNAME, "");
        if (!TextUtils.isEmpty(savedUsername)) {
            edtUsername.setText(savedUsername);
        }
    }

    private void bindViews() {
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
    }

    private void bindActions() {
        // Đăng nhập
        btnLogin.setOnClickListener(v -> doLogin());

        // Đăng ký
        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(this, Register.class))
        );

        // Quên mật khẩu
        tvForgotPassword.setOnClickListener(v -> {
            Intent i = new Intent(Login.this, ForgotPassword.class);
            startActivity(i);
            // Không finish() ở đây, để người dùng quay lại login nếu muốn
        });
    }

    private void doLogin() {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ Username và Password", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        RetrofitClient.api().login(username, password).enqueue(new Callback<ApiLoginResponse>() {
            @Override
            public void onResponse(Call<ApiLoginResponse> call, Response<ApiLoginResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    ApiLoginResponse res = response.body();
                    if (res.success && res.user != null) {
                        saveUser(res.user.id, res.user.username, res.user.role);
                        Toast.makeText(Login.this,
                                "Đăng nhập thành công: " + res.user.username,
                                Toast.LENGTH_SHORT).show();

                        // Chuyển sang trang chính
                        Intent intent = new Intent(Login.this, HomePage.class);
                        intent.putExtra("username", res.user.username);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(Login.this, res.message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Login.this, "Phản hồi không hợp lệ từ server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiLoginResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(Login.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        btnRegister.setEnabled(!loading);
        tvForgotPassword.setEnabled(!loading);
        btnLogin.setText(loading ? "Đang đăng nhập..." : "Đăng nhập");
    }

    private void saveUser(int userId, String username, String role) {
        SharedPreferences sp = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit()
                .putInt(KEY_USERID, userId)
                .putString(KEY_USERNAME, username)
                .putString(KEY_ROLE, role)
                .apply();
    }
}
