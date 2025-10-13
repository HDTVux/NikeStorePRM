package com.example.nikestore.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREFS = "nike_store_prefs";
    private static final String KEY_USERID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ROLE = "role";

    private static SessionManager instance;
    private final SharedPreferences sp;

    public SessionManager(Context ctx) {
        sp = ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static synchronized SessionManager getInstance(Context ctx) {
        if (instance == null) instance = new SessionManager(ctx);
        return instance;
    }

    public void saveUser(int userId, String username, String role) {
        sp.edit()
                .putInt(KEY_USERID, userId)
                .putString(KEY_USERNAME, username)
                .putString(KEY_ROLE, role)
                .apply();
    }

    public int getUserId() { return sp.getInt(KEY_USERID, 0); }
    public boolean isLoggedIn() { return getUserId() > 0; }
    public String getUsername() { return sp.getString(KEY_USERNAME, ""); }
    public void clear() { sp.edit().clear().apply(); }
}

