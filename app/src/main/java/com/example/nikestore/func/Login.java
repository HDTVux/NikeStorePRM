package com.example.nikestore.func;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.example.nikestore.data.CartManager;
import com.example.nikestore.model.ApiResponse;
import com.example.nikestore.net.ApiLoginResponse;
import com.example.nikestore.net.RetrofitClient;
import com.example.nikestore.util.SessionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Login bằng USERNAME và PASSWORD.
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

        // Ensure CartManager initialized (prevents crash if user opens Login directly)
        try {
            CartManager.init(getApplicationContext());
        } catch (Throwable ignore) {}

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

        // Gọi API login
        RetrofitClient.api().login(username, password).enqueue(new Callback<ApiLoginResponse>() {
            @Override
            public void onResponse(Call<ApiLoginResponse> call, Response<ApiLoginResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    ApiLoginResponse res = response.body();
                    if (res.success && res.user != null) {
                        // Lấy userId thực từ response
                        final int userId = res.user.id;
                        final String usernameResp = res.user.username;
                        final String roleResp = res.user.role;

                        // Lưu user local ngay (trước khi merge) — để các hàm khác có thể đọc user
                        SessionManager.getInstance(getApplicationContext()).saveUser(userId, res.user.username, res.user.role);


                        // Lấy guest items (dưới dạng List<Map<String,Object>>) từ CartManager
                        List<Map<String, Object>> guestItems = null;
                        try {
                            guestItems = CartManager.getInstance().getGuestItemsForApi();
                        } catch (Throwable t) {
                            Log.w("LOGIN", "Cannot get guest items: " + t.getMessage());
                        }

                        // Nếu có guest items thì gọi mergeCart, nếu không thì chuyển thẳng Home
                        if (guestItems != null && !guestItems.isEmpty()) {
                            Map<String,Object> body = new HashMap<>();
                            body.put("user_id", userId);
                            body.put("items", guestItems);

                            Log.d("LOGIN", "Merging guest cart: " + new com.google.gson.Gson().toJson(body));

                            RetrofitClient.api().mergeCart(body).enqueue(new Callback<ApiResponse>() {
                                @Override
                                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                                    if (response.isSuccessful() && response.body() != null && response.body().success) {
                                        // Merge success -> clear guest local items and optionally reload server cart
                                        try {
                                            CartManager.getInstance().clear();
                                            // if you implemented loadCartFromServer: uncomment
                                            // CartManager.getInstance().loadCartFromServer(userId);
                                        } catch (Throwable t) {
                                            Log.w("MERGE_CART", "clearGuestItems failed: " + t.getMessage());
                                        }
                                        // proceed to Home
                                        gotoHome(usernameResp);
                                    } else {
                                        // Merge failed on server side: log and still go home
                                        String msg = (response.body()!=null) ? response.body().message : ("HTTP " + response.code());
                                        Log.w("MERGE_CART", "merge failed: " + msg);
                                        gotoHome(usernameResp);
                                    }
                                }

                                @Override
                                public void onFailure(Call<ApiResponse> call, Throwable t) {
                                    Log.e("MERGE_CART", "network error", t);
                                    // Even if merge fails due network, proceed to Home (user already logged in)
                                    gotoHome(usernameResp);
                                }
                            });
                        } else {
                            // No guest items -> just go home
                            gotoHome(usernameResp);
                        }

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

    private void gotoHome(String username) {
        Toast.makeText(Login.this, "Đăng nhập thành công: " + username, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Login.this, HomePage.class);
        intent.putExtra("username", username);
        startActivity(intent);
        finish();
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
