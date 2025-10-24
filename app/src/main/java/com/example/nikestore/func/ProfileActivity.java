package com.example.nikestore.func;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nikestore.R;
import com.example.nikestore.model.User;
import com.example.nikestore.model.UserResponse;
import com.example.nikestore.net.RetrofitClient;
import com.example.nikestore.util.SessionManager;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends BaseActivity {

    private ImageView ivProfileAvatar;
    private TextView tvProfileEmail;
    private EditText etProfileUsername;
    private EditText etProfileAddress;
    private RadioGroup rgProfileGender;
    private RadioButton rbGenderMale, rbGenderFemale, rbGenderOther;
    private TextView tvProfileCreatedAt;
    private Button btnChangePassword;
    private Button btnUpdateProfile;
    private Button btnLogout;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // BaseActivity already sets the content view to R.layout.activity_base_with_nav
        // We need to set the specific layout for ProfileActivity into the container_main_content
        getLayoutInflater().inflate(R.layout.activity_profile, findViewById(R.id.container_main_content));

        sessionManager = SessionManager.getInstance(this);

        // Bind views
        ivProfileAvatar = findViewById(R.id.ivProfileAvatar);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        etProfileUsername = findViewById(R.id.etProfileUsername);
        etProfileAddress = findViewById(R.id.etProfileAddress);
        rgProfileGender = findViewById(R.id.rgProfileGender);
        rbGenderMale = findViewById(R.id.rbGenderMale);
        rbGenderFemale = findViewById(R.id.rbGenderFemale);
        rbGenderOther = findViewById(R.id.rbGenderOther);
        tvProfileCreatedAt = findViewById(R.id.tvProfileCreatedAt);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        btnLogout = findViewById(R.id.btnLogout);

        // Load user profile data
        loadUserProfile();

        // Set listeners
        btnUpdateProfile.setOnClickListener(v -> updateUserProfile());
        btnLogout.setOnClickListener(v -> logoutUser());
        btnChangePassword.setOnClickListener(v -> {
            Toast.makeText(ProfileActivity.this, "Chức năng đổi mật khẩu chưa được triển khai", Toast.LENGTH_SHORT).show();
            // TODO: Implement navigation to ChangePasswordActivity or show a dialog
        });
    }

    @Override
    protected int getNavigationMenuItemId() {
        return R.id.nav_account; // Highlight the account icon in the bottom navigation
    }

    private void loadUserProfile() {
        int userId = sessionManager.getUserId();
        Log.d("ProfileActivity", "Loading user profile for userId: " + userId);

        if (userId <= 0) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            Log.e("ProfileActivity", "User not logged in. Redirecting to Login.");
            startActivity(new Intent(ProfileActivity.this, Login.class));
            finish();
            return;
        }

        RetrofitClient.api().getUserProfile("get_user_profile", userId)
                .enqueue(new Callback<UserResponse>() {
                    @Override
                    public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().success) {
                            User user = response.body().user;
                            if (user != null) {
                                Log.d("ProfileActivity", "User profile loaded successfully: " + new Gson().toJson(user));
                                displayUserProfile(user);
                            } else {
                                Toast.makeText(ProfileActivity.this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                                Log.e("ProfileActivity", "User object is null in response body.");
                            }
                        } else {
                            String errorMsg = "Lỗi tải hồ sơ: " + (response.body() != null ? response.body().message : response.message());
                            Toast.makeText(ProfileActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                            Log.e("ProfileActivity", "Failed to load profile. Error: " + errorMsg + ", ErrorBody: " + new Gson().toJson(response.errorBody()));
                        }
                    }

                    @Override
                    public void onFailure(Call<UserResponse> call, Throwable t) {
                        Toast.makeText(ProfileActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("ProfileActivity", "Network error while loading profile: " + t.getMessage(), t);
                    }
                });
    }

    private void displayUserProfile(User user) {
        Log.d("ProfileActivity", "Displaying user profile for: " + user.username);
        tvProfileEmail.setText(user.email);
        etProfileUsername.setText(user.username);
        etProfileAddress.setText(user.address);
        tvProfileCreatedAt.setText(user.createdAt != null ? user.createdAt.substring(0, 10) : "-"); // Use user.createdAt

        // Set gender radio button
        if (user.gender == 1) {
            rbGenderMale.setChecked(true);
        } else if (user.gender == 2) {
            rbGenderFemale.setChecked(true);
        } else {
            rbGenderOther.setChecked(true); // Default or 'other' for 0 or any other value
        }
    }

    private void updateUserProfile() {
        int userId = sessionManager.getUserId();
        Log.d("ProfileActivity", "Updating user profile for userId: " + userId);

        if (userId <= 0) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            Log.e("ProfileActivity", "User not logged in for update. Redirecting to Login.");
            startActivity(new Intent(ProfileActivity.this, Login.class));
            finish();
            return;
        }

        String username = etProfileUsername.getText().toString().trim();
        String address = etProfileAddress.getText().toString().trim();

        int gender = 0; // Default to 'Other'
        if (rbGenderMale.isChecked()) {
            gender = 1;
        } else if (rbGenderFemale.isChecked()) {
            gender = 2;
        } else if (rbGenderOther.isChecked()) {
            gender = 0;
        }

        if (username.isEmpty()) {
            etProfileUsername.setError("Tên người dùng không được để trống");
            etProfileUsername.requestFocus();
            Log.w("ProfileActivity", "Username is empty. Update aborted.");
            return;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("user_id", userId);
        params.put("username", username);
        params.put("address", address);
        params.put("gender", gender);
        Log.d("ProfileActivity", "Update parameters: " + new Gson().toJson(params));

        RetrofitClient.api().updateUserProfile("update_user_profile", params)
                .enqueue(new Callback<UserResponse>() {
                    @Override
                    public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().success) {
                            Toast.makeText(ProfileActivity.this, response.body().message, Toast.LENGTH_SHORT).show();
                            Log.d("ProfileActivity", "Profile updated successfully. Response: " + new Gson().toJson(response.body()));
                            // Optionally update session or refresh UI with new data
                            if (response.body().user != null) {
                                sessionManager.saveUser(response.body().user); // Assuming SessionManager has a saveUser method
                                displayUserProfile(response.body().user);
                            }
                        } else {
                            String errorMsg = "Cập nhật hồ sơ thất bại: " + (response.body() != null ? response.body().message : response.message());
                            Toast.makeText(ProfileActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                            Log.e("ProfileActivity", "Profile update failed. Error: " + errorMsg + ", ErrorBody: " + new Gson().toJson(response.errorBody()));
                        }
                    }

                    @Override
                    public void onFailure(Call<UserResponse> call, Throwable t) {
                        Toast.makeText(ProfileActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("ProfileActivity", "Network error during profile update: " + t.getMessage(), t);
                    }
                });
    }

    private void logoutUser() {
        Log.d("ProfileActivity", "Logging out user.");
        sessionManager.logoutUser();
        Intent intent = new Intent(ProfileActivity.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
        startActivity(intent);
        finish();
    }
}
