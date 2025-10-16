package com.example.nikestore.func;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.example.nikestore.model.ApiResponse;
import com.example.nikestore.model.OrderStatusResponse;
import com.example.nikestore.model.VnPayResponse;
import com.example.nikestore.model.CartItem;
import com.example.nikestore.data.CartManager;
import com.example.nikestore.net.RetrofitClient;
import com.example.nikestore.util.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.*;

public class CheckoutActivity extends AppCompatActivity {
    private RecyclerView rvCart;
    private TextView tvSubtotal, tvShipping, tvTotal;
    private RadioButton rbCod, rbVnpay;
    private EditText edtAddress;
    private Button btnPay;
    private com.example.nikestore.adapter.CartAdapter cartAdapter;
    private List<CartItem> cartItems;

    private double subtotal = 0.0;
    private double shippingFee = 0.0;
    private double total = 0.0;

    // hiện tại đang xử lý VNPay cho orderId này
    private Integer currentVnPayOrderId = null;

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

        cartAdapter = new com.example.nikestore.adapter.CartAdapter();
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
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri data = intent.getData();
        if (data != null && "app".equals(data.getScheme()) && "vnpay-return".equals(data.getHost())) {
            // debug logs (optional)
            android.util.Log.d("VNPAY", "DeepLink URI: " + data.toString());

            // Try to get order id from vnp_TxnRef; fallback to currentVnPayOrderId
            String txnRef = data.getQueryParameter("vnp_TxnRef");
            int orderId = -1;
            if (txnRef != null) {
                try {
                    orderId = Integer.parseInt(txnRef.split("_")[0]);
                } catch (Exception ex) {
                    orderId = -1;
                }
            }
            if (orderId <= 0 && currentVnPayOrderId != null) {
                orderId = currentVnPayOrderId;
            }
            if (orderId <= 0) {
                Toast.makeText(this, "Không có thông tin thanh toán", Toast.LENGTH_SHORT).show();
                return;
            }

            // Start/continue polling for this concrete order id
            checkOrderWithPolling(orderId);
        }
    }

    /**
     * Poll server for order/payment status until confirmed or max attempts reached.
     * This method is idempotent and safe to call multiple times.
     */
    private void checkOrderWithPolling(int orderId) {
        final int MAX_ATTEMPTS = 8;        // số lần thử
        final int INTERVAL_MS = 3000;      // mỗi lần 3s
        final Handler handler = new Handler(Looper.getMainLooper());
        final int[] attempts = {0};

        final Runnable[] runnableHolder = new Runnable[1];

        runnableHolder[0] = new Runnable() {
            @Override
            public void run() {
                attempts[0]++;
                android.util.Log.d("VNPAY", "Polling order status attempt " + attempts[0] + " for order " + orderId);
                RetrofitClient.api().getOrderStatus(orderId).enqueue(new Callback<OrderStatusResponse>() {
                    @Override
                    public void onResponse(Call<OrderStatusResponse> call, Response<OrderStatusResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().success) {
                            String status = response.body().order != null ? response.body().order.status : null;
                            String payStatus = response.body().payment != null ? response.body().payment.status : null;
                            android.util.Log.d("VNPAY", "CheckOrder: order=" + status + " pay=" + payStatus);
                            if ("paid".equalsIgnoreCase(status) || "success".equalsIgnoreCase(payStatus)) {
                                Toast.makeText(CheckoutActivity.this, "Thanh toán VNPay thành công", Toast.LENGTH_SHORT).show();
                                // Clear server-side cart has been done by server in return/ipn
                                CartManager.getInstance().clear();
                                setResult(RESULT_OK);
                                finish();
                                return;
                            }
                        } else {
                            android.util.Log.w("VNPAY", "getOrderStatus not successful or invalid body");
                        }

                        if (attempts[0] < MAX_ATTEMPTS) {
                            handler.postDelayed(runnableHolder[0], INTERVAL_MS);
                        } else {
                            Toast.makeText(CheckoutActivity.this, "Thanh toán chưa được xác nhận. Vui lòng kiểm tra lịch sử đơn.", Toast.LENGTH_LONG).show();
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

        // start immediately
        handler.post(runnableHolder[0]);
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
            startActivity(new Intent(this, com.example.nikestore.func.Login.class));
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
            body.put("payment_method", "cash"); // align with server enum
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
                        int returnedOrderId = response.body().order_id;
                        currentVnPayOrderId = returnedOrderId;

                        // Start polling immediately for returnedOrderId (improves UX and handles race)
                        checkOrderWithPolling(returnedOrderId);

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
}
