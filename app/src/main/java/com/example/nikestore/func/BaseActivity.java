package com.example.nikestore.func;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.nikestore.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public abstract class BaseActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNavView;

    protected abstract int getNavigationMenuItemId();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                // *** THE FIX: Navigate to HomePage, not MainActivity ***
                intent = new Intent(this, HomePage.class); 
            } else if (itemId == R.id.nav_cart) {
                intent = new Intent(this, CartActivity.class);
            } else if (itemId == R.id.nav_orders) {
                 Toast.makeText(this, "Orders coming soon!", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.nav_account) {
                 Toast.makeText(this, "Account coming soon!", Toast.LENGTH_SHORT).show();
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }

            return true;
        });
    }

    private void updateBottomNavState() {
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
        } else {
            bottomNavView.getOrCreateBadge(R.id.nav_cart).setNumber(count);
        }
    }
}
