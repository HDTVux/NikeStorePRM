package com.example.nikestore.func;

import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nikestore.R;

public class BaseActivity extends AppCompatActivity {

    protected void setupBottomNav() {
        View container = findViewById(R.id.bottomNavContainer);
        if (container == null) return;

        ImageButton btnHome = container.findViewById(R.id.btnHome);
        ImageButton btnFav = container.findViewById(R.id.btnFavourite);
        ImageButton btnNotif = container.findViewById(R.id.btnNotifications);
        ImageButton btnProfile = container.findViewById(R.id.btnProfile);
        ImageButton fabCart = container.findViewById(R.id.fabCart);
        TextView tvBadge = container.findViewById(R.id.tvCartBadge);

        btnHome.setOnClickListener(v -> {
            // ví dụ: nếu đang không phải Home, mở home:
            Intent i = new Intent(this, HomePage.class);
            i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(i);
            overridePendingTransition(0,0);
        });

//        btnFav.setOnClickListener(v -> {
//            // mở wishlist activity
//            Intent i = new Intent(this, WishlistActivity.class);
//            startActivity(i);
//        });
//
//        btnNotif.setOnClickListener(v -> {
//            // open notifications
//            Intent i = new Intent(this, NotificationsActivity.class);
//            startActivity(i);
//        });
//
//        btnProfile.setOnClickListener(v -> {
//            Intent i = new Intent(this, ProfileActivity.class);
//            startActivity(i);
//        });

        fabCart.setOnClickListener(v -> {
            Intent i = new Intent(this, CartActivity.class);
            startActivity(i);
        });

        // demo: cập nhật badge từ cart count method (khởi tạo 0)
        updateCartBadge(tvBadge, 0);
    }

    protected void updateCartBadge(TextView tvBadge, int count) {
        if (tvBadge == null) return;
        if (count <= 0) {
            tvBadge.setVisibility(View.GONE);
        } else {
            tvBadge.setVisibility(View.VISIBLE);
            tvBadge.setText(String.valueOf(Math.min(count, 99)));
        }
    }
}
