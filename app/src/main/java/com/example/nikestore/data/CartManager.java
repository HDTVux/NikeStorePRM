package com.example.nikestore.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.nikestore.model.CartItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CartManager {
    private static final String PREF = "app_cart_pref";
    private static final String KEY = "cart_items_v1";
    private static CartManager instance;
    private final SharedPreferences prefs;
    private final Gson gson = new Gson();
    private List<CartItem> items;
    private final List<OnChangeListener> listeners = new ArrayList<>();

    private CartManager(Context ctx) {
        prefs = ctx.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
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
            catch (Throwable ttt) { items = new ArrayList<>(); }
        }
    }

    private void persist() {
        prefs.edit().putString(KEY, gson.toJson(items)).apply();
        notifyChanged();
    }

    public interface OnChangeListener { void onCartChanged(int totalCount, double totalPrice); }

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

    /** Add item: if same productId+variantId exists, increase quantity (cap by maxStock if >0) */
    public void addItem(CartItem item, int maxStock) {
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
        for (CartItem ci : items) t += ci.unitPrice * ci.quantity;
        return t;
    }
}
