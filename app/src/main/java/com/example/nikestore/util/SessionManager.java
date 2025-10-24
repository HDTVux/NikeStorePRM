package com.example.nikestore.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.nikestore.model.User;
import com.google.gson.Gson;
public class SessionManager {
    private static final String PREFS = "nike_store_prefs";
    private static final String KEY_USERID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_ROLE = "role";
    private static final String KEY_IS_ACTIVE = "is_active";
    private static final String KEY_CREATED_AT = "created_at";

    private static SessionManager instance;
    private final SharedPreferences sp;
    private final Gson gson;

    public SessionManager(Context ctx) {
        sp = ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        gson = new Gson(); // Initialize Gson
    }

    public static synchronized SessionManager getInstance(Context ctx) {
        if (instance == null) instance = new SessionManager(ctx);
        return instance;
    }

    // Method to save partial user info (e.g., after login)
    public void saveUser(int userId, String username, String role) {
        sp.edit()
                .putInt(KEY_USERID, userId)
                .putString(KEY_USERNAME, username)
                .putString(KEY_ROLE, role)
                .apply();
    }

    // Method to save full User object (e.g., after profile update)
    public void saveUser(User user) {
        sp.edit()
                .putInt(KEY_USERID, user.id)
                .putString(KEY_EMAIL, user.email)
                .putString(KEY_USERNAME, user.username)
                .putInt(KEY_GENDER, user.gender)
                .putString(KEY_ADDRESS, user.address)
                .putString(KEY_ROLE, user.role)
                .putInt(KEY_IS_ACTIVE, user.is_active) // Changed to putInt
                .putString(KEY_CREATED_AT, user.createdAt)
                .apply();
    }

    public User getCurrentUser() {
        // Reconstruct User object from SharedPreferences
        if (!isLoggedIn()) return null;

        User user = new User();
        user.id = getUserId();
        user.email = getEmail();
        user.username = getUsername();
        user.gender = getGender();
        user.address = getAddress();
        user.role = getRole();
        user.is_active = getIsActive();
        user.createdAt = getCreatedAt();
        return user;
    }

    public int getUserId() { return sp.getInt(KEY_USERID, 0); }
    public String getEmail() { return sp.getString(KEY_EMAIL, ""); }
    public String getUsername() { return sp.getString(KEY_USERNAME, ""); }
    public int getGender() { return sp.getInt(KEY_GENDER, 0); }
    public String getAddress() { return sp.getString(KEY_ADDRESS, ""); }
    public String getRole() { return sp.getString(KEY_ROLE, ""); }
    public int getIsActive() { return sp.getInt(KEY_IS_ACTIVE, 0); } // Changed to getInt
    public String getCreatedAt() { return sp.getString(KEY_CREATED_AT, ""); }

    public boolean isLoggedIn() { return getUserId() > 0; }

    public void logoutUser() { sp.edit().clear().apply(); }
}
