package com.vux.store.func;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.vux.store.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton; // Import FloatingActionButton

public abstract class BaseActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNavView;
    private FrameLayout containerMainContent;

    // Declare Floating Action Buttons for AI and Chat
    private FloatingActionButton fabAi;
    private FloatingActionButton fabChat;

    protected abstract int getNavigationMenuItemId();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the base layout containing the FrameLayout and BottomNavigationView
        setContentView(R.layout.activity_base_with_nav);
        containerMainContent = findViewById(R.id.container_main_content);
        setupBottomNav();

        // Initialize Floating Action Button for AI
        fabAi = findViewById(R.id.fab_ai);
        if (fabAi != null) {
            fabAi.setOnClickListener(v -> {
                // Start SmartSuggestionActivity when AI FAB is clicked
                Intent intent = new Intent(BaseActivity.this, SmartSuggestionActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        // Initialize Floating Action Button for Chat
        fabChat = findViewById(R.id.fab_chat);
        if (fabChat != null) {
            fabChat.setOnClickListener(v -> {
                // Start ChatActivity when Chat FAB is clicked
                Intent intent = new Intent(BaseActivity.this, ChatActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }
    }

    // Override setContentView methods to inflate child activity's layout into the FrameLayout
    @Override
    public void setContentView(int layoutResID) {
        if (containerMainContent != null) {
            LayoutInflater.from(this).inflate(layoutResID, containerMainContent, true);
        } else {
            // If containerMainContent is null (e.g., first call in onCreate), call super
            super.setContentView(layoutResID);
        }
    }

    @Override
    public void setContentView(View view) {
        if (containerMainContent != null) {
            containerMainContent.addView(view);
        }
        else {
            super.setContentView(view);
        }
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        if (containerMainContent != null) {
            containerMainContent.addView(view, params);
        }
        else {
            super.setContentView(view, params);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateBottomNavState();
    }

    protected void setupBottomNav() {
        bottomNavView = findViewById(R.id.bottom_nav_view);
        if (bottomNavView == null) {
            return;
        }

        bottomNavView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == getNavigationMenuItemId()) {
                return true;
            }

            Intent intent = null;
            if (itemId == R.id.nav_home) {
                intent = new Intent(this, HomePage.class); 
            } else if (itemId == R.id.nav_cart) {
                intent = new Intent(this, CartActivity.class);
            } else if (itemId == R.id.nav_wishlist) { // Handle Wishlist navigation
                intent = new Intent(this, WishlistActivity.class);
            } else if (itemId == R.id.nav_orders) {
                 intent = new Intent(this, OrderHistoryActivity.class); 
            } else if (itemId == R.id.nav_account) {
                 intent = new Intent(this, ProfileActivity.class); // Launch ProfileActivity
            }

            if (intent != null) {
                // FLAG_ACTIVITY_REORDER_TO_FRONT: If the activity is already running, bring it to the foreground.
                // FLAG_ACTIVITY_PREVIOUS_IS_TOP: Hint to the system that the previous activity is no longer needed.
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }

            return true;
        });
    }

    private void updateBottomNavState() {
        // Ensure bottomNavView is initialized before use
        if (bottomNavView == null) {
            bottomNavView = findViewById(R.id.bottom_nav_view);
        }
        if (bottomNavView != null) {
            bottomNavView.setSelectedItemId(getNavigationMenuItemId());
        }
    }

    protected void updateCartBadge(int count) {
        if (bottomNavView == null) return;
        if (count <= 0) {
            bottomNavView.removeBadge(R.id.nav_cart);
        }
        else {
            bottomNavView.getOrCreateBadge(R.id.nav_cart).setNumber(count);
        }
    }
}
