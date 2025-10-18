package com.example.nikestore;

import android.os.Bundle;
import com.example.nikestore.func.BaseActivity;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        setupBottomNav();
    }

    @Override
    protected int getNavigationMenuItemId() {
        // Trả về ID của mục "Home" trong menu
        return R.id.nav_home;
    }
}
