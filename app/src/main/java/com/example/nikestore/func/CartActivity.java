package com.example.nikestore.func;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.nikestore.R;
import com.example.nikestore.adapter.CartAdapter;
import com.example.nikestore.model.ApiResponse;
import com.example.nikestore.model.CartItem;
import com.example.nikestore.model.CartResponse;
import com.example.nikestore.net.RetrofitClient;
import com.example.nikestore.util.SessionManager;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity {
    private static final int CHECKOUT_REQUEST_CODE = 101;
    private androidx.recyclerview.widget.RecyclerView rv;
    private CartAdapter adapter;
    private TextView tvTotal;
    private Button btnCheckout;
    private SessionManager session;
    private List<CartItem> cartItems;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_cart);
        rv = findViewById(R.id.rvCartItems);
        tvTotal = findViewById(R.id.tvCartTotal);
        btnCheckout = findViewById(R.id.btnCheckout);
        session = new SessionManager(this);
        cartItems = new ArrayList<>();

        adapter = new com.example.nikestore.adapter.CartAdapter();
        adapter.setListener(new CartAdapter.Listener() {
            @Override
            public void onQtyChanged(CartItem item, int newQty) {
                RetrofitClient.api().updateCartItem(item.item_id, newQty).enqueue(new Callback<ApiResponse>() {
                    @Override public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                        loadCart(); // refresh totals
                    }
                    @Override public void onFailure(Call<ApiResponse> call, Throwable t) { Toast.makeText(CartActivity.this,"Network error",Toast.LENGTH_SHORT).show(); }
                });
            }
            @Override public void onRemove(CartItem item) {
                RetrofitClient.api().removeCartItem(item.item_id).enqueue(new Callback<ApiResponse>() {
                    @Override public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) { loadCart(); }
                    @Override public void onFailure(Call<ApiResponse> call, Throwable t) { Toast.makeText(CartActivity.this,"Network error",Toast.LENGTH_SHORT).show(); }
                });
            }
        });

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        btnCheckout.setOnClickListener(v-> {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
            intent.putParcelableArrayListExtra("cart_items", new ArrayList<>(cartItems));
            startActivityForResult(intent, CHECKOUT_REQUEST_CODE);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCart(); // Luôn tải lại giỏ hàng khi quay lại màn hình này
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHECKOUT_REQUEST_CODE && resultCode == RESULT_OK) {
            // Thanh toán thành công, loadCart() sẽ được gọi trong onResume()
        }
    }

    private void loadCart() {
        int uid = session.getUserId();
        if (uid <= 0) { Toast.makeText(this,"Please login",Toast.LENGTH_SHORT).show(); return; }
        RetrofitClient.api().getCart(uid).enqueue(new Callback<CartResponse>() {
            @Override public void onResponse(Call<CartResponse> call, Response<CartResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    cartItems = response.body().items;
                    if (cartItems == null) cartItems = new ArrayList<>();
                    adapter.submit(cartItems);
                    tvTotal.setText("$" + String.format(java.util.Locale.US, "%.2f", response.body().total));
                } else {
                    cartItems = new ArrayList<>();
                    adapter.submit(cartItems);
                    tvTotal.setText("$0.00");
                }
            }
            @Override public void onFailure(Call<CartResponse> call, Throwable t) {
                Log.e("CART","err",t);
                Toast.makeText(CartActivity.this,"Network error",Toast.LENGTH_SHORT).show();
            }
        });
    }
}