package com.example.nikestore.func;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nikestore.R;
import com.example.nikestore.adapter.CartAdapter;
import com.example.nikestore.model.CartItem;
import com.example.nikestore.data.CartManager;
import com.example.nikestore.model.ApiResponse;
import com.example.nikestore.model.VnPayResponse;
import com.example.nikestore.model.OrderStatusResponse;
import com.example.nikestore.net.RetrofitClient;
import com.example.nikestore.util.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.*;

public class CheckoutActivity extends AppCompatActivity {
    private static final String PREFS = "nikestore_prefs";
    private static final String KEY_LAST_ORDER = "last_order_id";

    private RecyclerView rvCart;
    private TextView tvSubtotal, tvShipping, tvTotal;
    private RadioButton rbCod, rbVnpay;
    private EditText edtAddress;
    private Button btnPay;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems;

    private double subtotal = 0.0;
    private double shippingFee = 0.0;
    private double total = 0.0;

    private int lastOrderId = 0; // lưu order đang chờ xác nhận

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        rvCart = findViewById(R.id.rvCartItems);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvShipping = findViewById(R.id.tvShipping);
        tvTotal = findViewById(R.id.tvTotal);
        rbCod = findViewById(R.id.rbCod);
        rbVnpay = findViewById(R.id.rbVnpay);
        edtAddress = findViewById(R.id.edtAddress);
        btnPay = findViewById(R.id.btnPay);

        cartAdapter = new CartAdapter();
        rvCart.setAdapter(cartAdapter);
        rvCart.setLayoutManager(new LinearLayoutManager(this));

        cartItems = getIntent().getParcelableArrayListExtra("cart_items");
        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }
        cartAdapter.submit(cartItems);
        recalcTotals(cartItems);

        // ensure address visible
        edtAddress.setVisibility(View.VISIBLE);

        btnPay.setOnClickListener(v -> onPayClicked());

        // load last order id from prefs (in case app restarted or returned)
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        lastOrderId = sp.getInt(KEY_LAST_ORDER, 0);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // important: update intent for onResume if needed
        handleDeepLinkIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // If we have a pending order id (from create payment), poll its status
        if (lastOrderId > 0) {
            pollOrderStatusWithTimeout(lastOrderId);
        }
    }

    private void handleDeepLinkIntent(Intent intent) {
        Uri data = intent.getData();
        if (data != null && "app".equals(data.getScheme()) && "vnpay-return".equals(data.getHost())) {
            // debug
            android.util.Log.d("VNPAY", "DeepLink URI: " + data.toString());
            String status = data.getQueryParameter("vnp_ResponseCode");
            String txnRef = data.getQueryParameter("vnp_TxnRef");
            // if vnp_TxnRef exists, use it to parse order id
            if (!TextUtils.isEmpty(txnRef)) {
                try {
                    int orderId = Integer.parseInt(txnRef.split("_")[0]);
                    lastOrderId = orderId;
                    // save to prefs so onResume can poll if needed
                    getSharedPreferences(PREFS, MODE_PRIVATE).edit().putInt(KEY_LAST_ORDER, orderId).apply();
                } catch (Exception e) { /* ignore */ }
            }

            if ("00".equals(status)) {
                // we still should verify with server but let's optimistic show success and clear cart
                Toast.makeText(this, "Thanh toán VNPay thành công", Toast.LENGTH_SHORT).show();
                CartManager.getInstance().clear();
                // clear last order from prefs
                getSharedPreferences(PREFS, MODE_PRIVATE).edit().remove(KEY_LAST_ORDER).apply();
                setResult(RESULT_OK);
                finish();
            } else {
                // start polling immediately to confirm
                if (lastOrderId > 0) {
                    pollOrderStatusWithTimeout(lastOrderId);
                } else {
                    Toast.makeText(this, "Thanh toán VNPay: không có thông tin", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void recalcTotals(List<CartItem> items) {
        subtotal = 0;
        for (CartItem ci : items) {
            subtotal += ci.getPrice() * ci.getQuantity();
        }
        shippingFee = subtotal > 100 ? 0 : 5;
        total = subtotal + shippingFee;

        tvSubtotal.setText(String.format(Locale.US, "Subtotal: $%.2f", subtotal));
        tvShipping.setText(String.format(Locale.US, "Shipping: $%.2f", shippingFee));
        tvTotal.setText(String.format(Locale.US, "Total: $%.2f", total));
    }

    private void onPayClicked() {
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }

        SessionManager sm = SessionManager.getInstance(getApplicationContext());
        if (!sm.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Login.class));
            return;
        }

        int userId = sm.getUserId();
        String address = edtAddress.getText().toString().trim();

        if (TextUtils.isEmpty(address)) {
            Toast.makeText(this, "Vui lòng nhập địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isCod = rbCod.isChecked();
        boolean isVnpay = rbVnpay.isChecked();

        if (!isCod && !isVnpay) {
            Toast.makeText(this, "Chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }

        // prepare items
        List<Map<String,Object>> payloadItems = new ArrayList<>();
        for (CartItem ci: cartItems) {
            Map<String,Object> m = new HashMap<>();
            m.put("product_id", ci.getProduct_id());
            Integer vid = ci.getVariant_id();
            m.put("variant_id", (vid == null || vid == 0) ? null : vid);
            m.put("quantity", ci.getQuantity());
            payloadItems.add(m);
        }

        Map<String,Object> body = new HashMap<>();
        body.put("user_id", userId);
        body.put("items", payloadItems);
        body.put("shipping_fee", shippingFee);
        body.put("subtotal", subtotal);
        body.put("total", total);
        body.put("address", address);

        if (isCod) {
            body.put("payment_method", "cash"); // align with payments enum
            RetrofitClient.api().createOrder(body).enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().success) {
                        Toast.makeText(CheckoutActivity.this, "Đặt hàng thành công", Toast.LENGTH_SHORT).show();
                        CartManager.getInstance().clear();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(CheckoutActivity.this,
                                "Lỗi: " + (response.body() != null ? response.body().message : "server"),
                                Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    Toast.makeText(CheckoutActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
                }
            });

        } else if (isVnpay) {
            body.put("payment_method", "vnpay");
            RetrofitClient.api().createVnPayPayment(body).enqueue(new Callback<VnPayResponse>() {
                @Override
                public void onResponse(Call<VnPayResponse> call, Response<VnPayResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().success) {
                        String url = response.body().payment_url;
                        int orderId = response.body().order_id;
                        // save last order id to prefs so we can poll onResume if app is not opened by deep link
                        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putInt(KEY_LAST_ORDER, orderId).apply();
                        lastOrderId = orderId;

                        if (!TextUtils.isEmpty(url)) {
                            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                            CustomTabsIntent customTabsIntent = builder.build();
                            customTabsIntent.launchUrl(CheckoutActivity.this, Uri.parse(url));
                        } else {
                            Toast.makeText(CheckoutActivity.this, "URL VNPay rỗng", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(CheckoutActivity.this,
                                "Lỗi tạo VNPay: " + (response.body() != null ? response.body().message : "server"),
                                Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<VnPayResponse> call, Throwable t) {
                    Toast.makeText(CheckoutActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Poll with reasonable attempts (like your previous implementation)
    private void pollOrderStatusWithTimeout(int orderId) {
        final int MAX_ATTEMPTS = 6;
        final int INTERVAL_MS = 3000;
        final android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
        final int[] attempts = {0};
        final Runnable[] runnableHolder = new Runnable[1];

        runnableHolder[0] = new Runnable() {
            @Override
            public void run() {
                attempts[0]++;
                RetrofitClient.api().getOrderStatus(orderId).enqueue(new Callback<OrderStatusResponse>() {
                    @Override
                    public void onResponse(Call<OrderStatusResponse> call, Response<OrderStatusResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().success) {
                            String status = response.body().order != null ? response.body().order.status : null;
                            String payStatus = response.body().payment != null ? response.body().payment.status : null;
                            android.util.Log.d("VNPAY", "CheckOrder: order=" + status + " pay=" + payStatus);
                            if ("paid".equalsIgnoreCase(status) || "success".equalsIgnoreCase(payStatus)) {
                                Toast.makeText(CheckoutActivity.this, "Thanh toán VNPay thành công", Toast.LENGTH_SHORT).show();
                                CartManager.getInstance().clear();
                                // clear lastOrder
                                getSharedPreferences(PREFS, MODE_PRIVATE).edit().remove(KEY_LAST_ORDER).apply();
                                lastOrderId = 0;
                                setResult(RESULT_OK);
                                finish();
                                return;
                            }
                        }
                        if (attempts[0] < MAX_ATTEMPTS) {
                            handler.postDelayed(runnableHolder[0], INTERVAL_MS);
                        } else {
                            Toast.makeText(CheckoutActivity.this, "Thanh toán chưa được xác nhận. Vui lòng kiểm tra lịch sử đơn hoặc thử lại sau.", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<OrderStatusResponse> call, Throwable t) {
                        android.util.Log.e("VNPAY", "checkOrder error", t);
                        if (attempts[0] < MAX_ATTEMPTS) {
                            handler.postDelayed(runnableHolder[0], INTERVAL_MS);
                        } else {
                            Toast.makeText(CheckoutActivity.this, "Không thể kiểm tra trạng thái đơn. Lỗi mạng", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        };

        handler.post(runnableHolder[0]);
    }
}
