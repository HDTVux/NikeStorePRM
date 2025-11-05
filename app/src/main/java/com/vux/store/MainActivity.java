package com.vux.store;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.vux.store.func.BaseActivity;
import com.vux.store.func.ChatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends BaseActivity {

    private FloatingActionButton fabMjt;
    private FloatingActionButton fabTest;
    private FloatingActionButton fabStore;
    private FloatingActionButton fabSupport;
    private LinearLayout buttonSupport; // LinearLayout container for support button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        setupBottomNav();

        // Initialize buttons from the floating menu
        fabMjt = findViewById(R.id.fab_mjt);
        fabTest = findViewById(R.id.fab_test);
        fabStore = findViewById(R.id.fab_store);
        fabSupport = findViewById(R.id.fab_support);
        buttonSupport = findViewById(R.id.button_support);

        // Set OnClickListener for the entire support button container
        buttonSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start ChatActivity when the support button is clicked
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                startActivity(intent);
            }
        });

        // Set OnClickListener for other buttons (with Toast messages)
        fabMjt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Chức năng MJT Thi Thử!", Toast.LENGTH_SHORT).show();
            }
        });

        fabTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Chức năng Test!", Toast.LENGTH_SHORT).show();
            }
        });

        fabStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Chức năng Cửa Hàng!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected int getNavigationMenuItemId() {
        // Trả về ID của mục "Home" trong menu
        return R.id.nav_home;
    }
}
