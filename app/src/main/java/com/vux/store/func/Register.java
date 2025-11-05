package com.vux.store.func;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.vux.store.R;
import com.vux.store.net.ApiLoginResponse;
import com.vux.store.net.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Register extends AppCompatActivity {

    private EditText edtEmail, edtUsername, edtPassword, edtConfirmPassword;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale;
    private Button btnRegisterNew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        bindViews();
        bindActions();
    }

    private void bindViews() {
        edtEmail = findViewById(R.id.edtEmail);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        rgGender = findViewById(R.id.rgGender);
        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);
        btnRegisterNew = findViewById(R.id.btnRegisterNew);
    }

    private void bindActions() {
        btnRegisterNew.setOnClickListener(v -> doRegister());
    }

    private void doRegister() {
        String email = edtEmail.getText().toString().trim();
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirm  = edtConfirmPassword.getText().toString().trim();

        int gender = 0;
        if (rbMale.isChecked()) gender = 1;
        else if (rbFemale.isChecked()) gender = 2;

        // Validate
        if (TextUtils.isEmpty(email)) { edtEmail.setError("Nhập email"); edtEmail.requestFocus(); return; }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email không hợp lệ"); edtEmail.requestFocus(); return;
        }
        if (TextUtils.isEmpty(username)) { edtUsername.setError("Nhập username"); edtUsername.requestFocus(); return; }
        if (TextUtils.isEmpty(password)) { edtPassword.setError("Nhập password"); edtPassword.requestFocus(); return; }
        if (password.length() < 6) { edtPassword.setError("Mật khẩu tối thiểu 6 ký tự"); edtPassword.requestFocus(); return; }
        if (!password.equals(confirm)) { edtConfirmPassword.setError("Mật khẩu nhập lại không khớp"); edtConfirmPassword.requestFocus(); return; }

        btnRegisterNew.setEnabled(false);
        btnRegisterNew.setText("Đang đăng ký...");

        RetrofitClient.api().register(email, username, password, gender)
                .enqueue(new Callback<ApiLoginResponse>() {
                    @Override
                    public void onResponse(Call<ApiLoginResponse> call, Response<ApiLoginResponse> response) {
                        btnRegisterNew.setEnabled(true);
                        btnRegisterNew.setText("Register");

                        if (!response.isSuccessful() || response.body() == null) {
                            toast("Phản hồi không hợp lệ từ server");
                            return;
                        }
                        ApiLoginResponse res = response.body();
                        if (res.success && res.user != null) {
                            toast("Đăng ký thành công! Đăng nhập ngay nhé");
                            Intent i = new Intent(Register.this, Login.class);
                            i.putExtra("prefill_username", res.user.username);
                            startActivity(i);
                            finish();
                        } else {
                            toast(res.message != null ? res.message : "Đăng ký thất bại");
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiLoginResponse> call, Throwable t) {
                        btnRegisterNew.setEnabled(true);
                        btnRegisterNew.setText("Register");
                        toast("Lỗi mạng: " + t.getMessage());
                    }
                });
    }


    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
