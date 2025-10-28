package com.example.nikestore.func;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
// import androidx.browser.customtabs.CustomTabsIntent; // Không còn cần thiết
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
import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutActivity extends AppCompatActivity {
    private static final String TAG = "CheckoutActivity"; // NEW: Tag cho Logcat
    private static final int CHECKOUT_REQUEST_CODE = 101;
    private RecyclerView rvCart;
    private TextView tvSubtotal, tvShipping, tvTotal;
    private RadioButton rbCod, rbVnpay;
    private EditText edtAddress;
    private Button btnPay;
    private com.example.nikestore.adapter.CartAdapter cartAdapter;
    private List<CartItem> cartItems;

    // *** THÊM BIẾN MỚI ***
    private EditText edtPhoneNumber;

    private double subtotal = 0.0;
    private double shippingFee = 0.0;
    private double total = 0.0;

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

        // *** THÊM THAM CHIẾU MỚI ***
        edtPhoneNumber = findViewById(R.id.edtPhoneNumber);

        cartAdapter = new com.example.nikestore.adapter.CartAdapter();
        cartAdapter.setListener(new CartAdapter.Listener() {
            @Override
            public void onQtyChanged(CartItem item, int newQty) {
                // NOTE: When quantity changes, we need to ensure the totals are re-calculated
                // However, this adapter is only for displaying. The actual cart quantity update
                // happens in CartActivity, which then reloads the cart. This activity gets
                // a list of cart items passed via intent, so a full reload or explicit update
                // is not straightforward here without modifying the passed list.
                // For now, assume the list received here is static and read-only for totals.
                // If quantity changes should reflect live, then this activity should also
                // listen to CartManager changes or fetch its own cart data.
            }

            @Override
            public void onRemove(CartItem item) {
                // Similar to onQtyChanged, removal logic isn't directly handled here.
            }
        });
        rvCart.setAdapter(cartAdapter);
        rvCart.setLayoutManager(new LinearLayoutManager(this));

        cartItems = getIntent().getParcelableArrayListExtra("cart_items");
        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }
        cartAdapter.submit(cartItems);
        recalcTotals(cartItems);

        edtAddress.setVisibility(View.VISIBLE);

        btnPay.setOnClickListener(v -> onPayClicked());
    }

    // --- XÓA PHƯƠNG THỨC onNewIntent (đã chuyển logic sang onActivityResult) ---
    // @Override
    // protected void onNewIntent(Intent intent) {
    //     super.onNewIntent(intent);
    //     Uri data = intent.getData();
    //     if (data != null && "app".equals(data.getScheme()) && "vnpay-return".equals(data.getHost())) {
    //         Log.d(TAG, "DeepLink URI: " + data.toString()); // Changed Log.d tag
    //         String txnRef = data.getQueryParameter("vnp_TxnRef");
    //         int orderId = -1;
    //         if (txnRef != null) {
    //             try {
    //                 orderId = Integer.parseInt(txnRef.split("_")[0]);
    //             } catch (Exception ex) {
    //                 orderId = -1;
    //             }
    //         }
    //         if (orderId <= 0 && currentVnPayOrderId != null) {
    //             orderId = currentVnPayOrderId;
    //         }
    //         if (orderId <= 0) {
    //             Toast.makeText(this, "Không có thông tin thanh toán", Toast.LENGTH_SHORT).show();
    //             return;
    //         }
    //         checkOrderWithPolling(orderId);
    //     }
    // }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHECKOUT_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                Uri uri = data.getData();
                if (uri != null && "app".equals(uri.getScheme()) && "vnpay-return".equals(uri.getHost())) {
                    Log.d(TAG, "DeepLink URI from WebView: " + uri.toString());
                    String txnRef = uri.getQueryParameter("vnp_TxnRef");
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
                    checkOrderWithPolling(orderId);
                }
            } else {
                // Xử lý khi WebViewActivity đóng mà không có kết quả OK (ví dụ: người dùng nhấn Back)
                Toast.makeText(this, "Thanh toán VNPay bị hủy hoặc không thành công.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkOrderWithPolling(int orderId) {
        final int MAX_ATTEMPTS = 8;
        final int INTERVAL_MS = 3000;
        final Handler handler = new Handler(Looper.getMainLooper());
        final int[] attempts = {0};
        final Runnable[] runnableHolder = new Runnable[1];
        runnableHolder[0] = new Runnable() {
            @Override
            public void run() {
                attempts[0]++;
                Log.d(TAG, "Polling order status attempt " + attempts[0] + " for order " + orderId); // Changed Log.d tag
                RetrofitClient.api().getOrderStatus(orderId).enqueue(new Callback<OrderStatusResponse>() {
                    @Override
                    public void onResponse(Call<OrderStatusResponse> call, Response<OrderStatusResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().success) {
                            String status = response.body().order != null ? response.body().order.status : null;
                            String payStatus = response.body().payment != null ? response.body().payment.status : null;
                            Log.d(TAG, "CheckOrder: order=" + status + " pay=" + payStatus); // Changed Log.d tag
                            if ("paid".equalsIgnoreCase(status) || "success".equalsIgnoreCase(payStatus)) {
                                Toast.makeText(CheckoutActivity.this, "Thanh toán VNPay thành công", Toast.LENGTH_SHORT).show();
                                CartManager.getInstance().clear();
                                setResult(RESULT_OK);
                                finish();
                                return;
                            }
                        } else {
                            Log.w(TAG, "getOrderStatus not successful or invalid body"); // Changed Log.w tag
                        }
                        if (attempts[0] < MAX_ATTEMPTS) {
                            handler.postDelayed(runnableHolder[0], INTERVAL_MS);
                        } else {
                            Toast.makeText(CheckoutActivity.this, "Thanh toán chưa được xác nhận. Vui lòng kiểm tra lịch sử đơn.", Toast.LENGTH_LONG).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<OrderStatusResponse> call, Throwable t) {
                        Log.e(TAG, "checkOrder error", t); // Changed Log.e tag
                        if (attempts[0] < MAX_ATTEMPTS) {
                            handler.postDelayed(runnableHolder[0], INTERVAL_MS);
                        }
                    }
                });
            }
        };
        handler.post(runnableHolder[0]);
    }

    private void recalcTotals(List<CartItem> items) {
        subtotal = 0;
        Log.d(TAG, "recalcTotals: Starting calculation");
        for (CartItem ci : items) {
            double itemFinalPrice = ci.getFinal_price();
            int itemQuantity = ci.getQuantity();
            double itemSubtotal = itemFinalPrice * itemQuantity;
            subtotal += itemSubtotal;
            Log.d(TAG, "  Item: " + ci.getProduct_name() + ", Final Price: $" + itemFinalPrice + ", Qty: " + itemQuantity + ", Item Subtotal: $" + itemSubtotal + ", Current Total Subtotal: $" + subtotal);
        }
        shippingFee = subtotal > 100 ? 0 : 5;
        total = subtotal + shippingFee;
        Log.d(TAG, "recalcTotals: Calculated Subtotal: $" + subtotal + ", Shipping: $" + shippingFee + ", Total: $" + total);
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
        
        // *** LẤY DỮ LIỆU SỐ ĐIỆN THOẠI ***
        String phone = edtPhoneNumber.getText().toString().trim();

        if (TextUtils.isEmpty(address)) {
            Toast.makeText(this, "Vui lòng nhập địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // *** THÊM KIỂM TRA CHO SỐ ĐIỆN THOẠI ***
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isCod = rbCod.isChecked();
        boolean isVnpay = rbVnpay.isChecked();

        if (!isCod && !isVnpay) {
            Toast.makeText(this, "Chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }

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
        body.put("phone", phone);

        if (isCod) {
            body.put("payment_method", "cash");
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
                        // Thay vì CustomTabsIntent, khởi chạy VnPayWebViewActivity
                        if (!TextUtils.isEmpty(url)) {
                            Intent webViewIntent = new Intent(CheckoutActivity.this, VnPayWebViewActivity.class);
                            webViewIntent.putExtra(VnPayWebViewActivity.EXTRA_VNPAY_URL, url);
                            startActivityForResult(webViewIntent, CHECKOUT_REQUEST_CODE);
                        } else {
                            Toast.makeText(CheckoutActivity.this, "URL VNPay rỗng", Toast.LENGTH_SHORT).show();
                        }
                    }
                     else {
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