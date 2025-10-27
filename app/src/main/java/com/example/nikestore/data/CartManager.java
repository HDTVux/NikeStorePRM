package com.example.nikestore.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.nikestore.model.ApiResponse;
import com.example.nikestore.model.CartItem;
import com.example.nikestore.net.RetrofitClient;
import com.example.nikestore.util.SessionManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartManager {
    private static final String PREF = "app_cart_pref";
    private static final String KEY = "cart_items_v1";
    private static CartManager instance;
    private final SharedPreferences prefs;
    private final Gson gson = new Gson();
    private List<CartItem> items;
    private final List<OnChangeListener> listeners = new ArrayList<>();
    private final Context appContext;
    private final SessionManager sessionManager;

    private CartManager(Context ctx) {
        appContext = ctx.getApplicationContext();
        prefs = appContext.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sessionManager = SessionManager.getInstance(appContext);
        load();
    }

    public static synchronized CartManager init(Context ctx) {
        if (instance == null) instance = new CartManager(ctx);
        return instance;
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) throw new IllegalStateException("CartManager not initialized. Call init(context) first.");
        return instance;
    }

    private void load() {
        String raw = prefs.getString(KEY, null);
        if (raw == null) items = new ArrayList<>();
        else {
            Type t = new TypeToken<List<CartItem>>(){}.getType();
            try { items = gson.fromJson(raw, t); if (items == null) items = new ArrayList<>(); }
            catch (Throwable ttt) { items = new ArrayList<>(); Log.e("CartManager", "Error loading cart: ", ttt);}
        }
    }

    private void persist() {
        prefs.edit().putString(KEY, gson.toJson(items)).apply();
        notifyChanged();
    }

    public interface OnChangeListener { void onCartChanged(int totalCount, double totalPrice); }

    // NEW: Interface cho các hành động giỏ hàng (thêm/xóa)
    public interface CartActionListener {
        void onCartActionSuccess(String message);
        void onCartActionFailure(String error);
    }

    public void addListener(OnChangeListener l) {
        if (l==null) return;
        if (!listeners.contains(l)) listeners.add(l);
        // initial callback
        l.onCartChanged(getTotalCount(), getTotalPrice());
    }

    public void removeListener(OnChangeListener l) {
        listeners.remove(l);
    }

    private void notifyChanged() {
        int count = getTotalCount();
        double price = getTotalPrice();
        for (OnChangeListener l : listeners) {
            try { l.onCartChanged(count, price); } catch(Throwable ignore){}
        }
    }

    /** Add item to local cart (without API call) */
    private void addLocalItem(CartItem item, int maxStock) {
        if (item == null) return;
        boolean merged = false;
        for (CartItem ci : items) {
            if (ci.product_id == item.product_id && ci.variant_id == item.variant_id) {
                int newQty = ci.quantity + item.quantity;
                if (maxStock > 0 && newQty > maxStock) newQty = maxStock;
                ci.quantity = newQty;
                merged = true;
                break;
            }
        }
        if (!merged) {
            if (maxStock > 0 && item.quantity > maxStock) item.quantity = maxStock;
            items.add(item);
        }
        persist();
    }

    /**
     * Add item to cart, syncs with server if user is logged in.
     * @param ctx Context for toasts
     * @param item The CartItem to add
     * @param listener Callback for action result
     */
    public void addItemToCart(Context ctx, CartItem item, CartActionListener listener) {
        int userId = sessionManager.getUserId();
        if (userId <= 0) {
            // Guest user, add locally
            addLocalItem(item, -1); // -1 means no max stock check for guest cart currently
            if (listener != null) listener.onCartActionSuccess("Đã thêm vào giỏ hàng (khách)");
            return;
        }

        // Logged in user, sync with server
        RetrofitClient.api().addToCart(userId, item.product_id, item.variant_id, item.quantity)
                .enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().success) {
                            // On success, add to local cart as well
                            addLocalItem(item, -1); // Update local cart with the item
                            if (listener != null) listener.onCartActionSuccess("Đã thêm vào giỏ hàng");
                        } else {
                            String errorMsg = "Không thể thêm vào giỏ hàng: " + (response.body() != null ? response.body().message : response.message());
                            Log.e("CartManager", "API add to cart failed: " + errorMsg);
                            if (listener != null) listener.onCartActionFailure(errorMsg);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                        Log.e("CartManager", "Network error adding to cart: ", t);
                        if (listener != null) listener.onCartActionFailure("Lỗi mạng khi thêm vào giỏ hàng");
                    }
                });
    }

    public void updateQuantity(int productId, int variantId, int qty) {
        Iterator<CartItem> it = items.iterator();
        while (it.hasNext()) {
            CartItem ci = it.next();
            if (ci.product_id == productId && ci.variant_id == variantId) {
                if (qty <= 0) it.remove();
                else ci.quantity = qty;
                break;
            }
        }
        persist();
    }

    public void removeItem(int productId, int variantId) {
        Iterator<CartItem> it = items.iterator();
        while (it.hasNext()) {
            CartItem ci = it.next();
            if (ci.product_id == productId && ci.variant_id == variantId) {
                it.remove();
                break;
            }
        }
        persist();
    }

    public List<Map<String, Object>> getGuestItemsForApi() {
        List<Map<String, Object>> out = new ArrayList<>();
        synchronized (items) { // items là List<CartItem> trong CartManager
            for (CartItem ci : items) {
                Map<String, Object> m = new HashMap<>();
                m.put("product_id", ci.getProduct_id());
                // nếu variantId null thì vẫn truyền null (server xử lý)
                m.put("variant_id", ci.getVariant_id());
                m.put("quantity", ci.getQuantity());
                out.add(m);
            }
        }
        return out;
    }

    public void clear() {
        items.clear();
        persist();
    }

    public List<CartItem> getItems() {
        return new ArrayList<>(items);
    }

    public int getTotalCount() {
        int s = 0;
        for (CartItem ci : items) s += ci.quantity;
        return s;
    }

    public double getTotalPrice() {
        double t = 0;
        for (CartItem ci : items) t += ci.getFinal_price() * ci.quantity; // Use final_price for total calculation
        return t;
    }
}
