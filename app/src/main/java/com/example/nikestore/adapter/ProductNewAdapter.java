package com.example.nikestore.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nikestore.R;
import com.example.nikestore.func.Login;
import com.example.nikestore.model.ApiResponse; // Import ApiResponse
import com.example.nikestore.model.Product;
import com.example.nikestore.model.WishlistResponse; // Import WishlistResponse
import com.example.nikestore.net.RetrofitClient;
import com.example.nikestore.util.SessionManager; // Import SessionManager

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductNewAdapter extends RecyclerView.Adapter<ProductNewAdapter.VH> {

    private final List<Product> data = new ArrayList<>();
    private final DecimalFormat money = new DecimalFormat("#,##0.##");
    private final SessionManager sessionManager;
    private Set<Integer> favoritedProductIds = new HashSet<>(); // To keep track of favorited products
    private final int layoutResId; // NEW: Field to hold the layout resource ID

    public interface OnItemClickListener { void onItemClick(Product item); }
    private OnItemClickListener listener;
    public void setOnItemClickListener(OnItemClickListener l){ this.listener = l; }

    // NEW: Constructor with layoutResId
    public ProductNewAdapter(Context context, int layoutResId) {
        this.sessionManager = SessionManager.getInstance(context);
        this.layoutResId = layoutResId;
    }

    // Existing constructor (will now default to item_product_new)
    public ProductNewAdapter(Context context) {
        this(context, R.layout.item_product_new);
    }

    public void submit(List<Product> list) {
        data.clear();
        if (list != null) {
            data.addAll(list);
        }
        loadFavoritedProducts(); // Load favorited products for the current user
        notifyDataSetChanged();
    }

    // Method to update the favorited product IDs from the outside (e.g., from WishlistActivity)
    public void setFavoritedProductIds(Set<Integer> favoritedProductIds) {
        this.favoritedProductIds = favoritedProductIds;
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // UPDATED: Use the stored layoutResId
        View v = LayoutInflater.from(parent.getContext())
                .inflate(layoutResId, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int position) {
        Product p = data.get(position);
        h.tvName.setText(p.name);
        h.tvPrice.setText("$" + money.format(p.price));

        // Only load image if it's the default layout (not wishlist layout)
        if (layoutResId == R.layout.item_product_new) {
            Log.d("ProductNewAdapter", "Loading image for product: " + p.name + ", URL: " + (p.image_url == null ? "null" : p.image_url));
            Glide.with(h.itemView.getContext())
                    .load(p.image_url == null ? "" : p.image_url.trim())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_delete)
                    .centerCrop()
                    .into(h.img);
        }

        // Set initial favorite icon state
        h.ivFavorite.setImageResource(favoritedProductIds.contains(p.id) ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);

        h.ivFavorite.setOnClickListener(v -> {
            if (sessionManager.getUserId() <= 0) {
                Toast.makeText(h.itemView.getContext(), "Vui lòng đăng nhập để thêm vào danh sách yêu thích", Toast.LENGTH_SHORT).show();
                h.itemView.getContext().startActivity(new Intent(h.itemView.getContext(), Login.class));
                return;
            }
            toggleFavorite(p, h.ivFavorite);
        });

        h.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(p);
            }
        });
    }

    private void loadFavoritedProducts() {
        int userId = sessionManager.getUserId();
        if (userId <= 0) {
            favoritedProductIds.clear();
            return;
        }

        RetrofitClient.api().getFavorites(userId).enqueue(new Callback<WishlistResponse>() {
            @Override
            public void onResponse(Call<WishlistResponse> call, Response<WishlistResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    favoritedProductIds.clear();
                    if (response.body().wishlist != null) {
                        for (Product p : response.body().wishlist) {
                            favoritedProductIds.add(p.id);
                        }
                    }
                    notifyDataSetChanged(); // Refresh UI to reflect favorite status
                } else {
                    Log.e("ProductNewAdapter", "Failed to load favorited products: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<WishlistResponse> call, Throwable t) {
                Log.e("ProductNewAdapter", "Network error loading favorited products: " + t.getMessage());
            }
        });
    }

    private void toggleFavorite(Product product, ImageView favoriteIcon) {
        int userId = sessionManager.getUserId();
        if (userId <= 0) return; // Should be handled by click listener already

        boolean isFavorited = favoritedProductIds.contains(product.id);

        if (isFavorited) {
            // Remove from favorite
            RetrofitClient.api().removeFavorite(userId, product.id).enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().success) {
                        favoritedProductIds.remove(product.id);
                        favoriteIcon.setImageResource(R.drawable.ic_favorite_border);
                        Toast.makeText(favoriteIcon.getContext(), "Đã xóa khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(favoriteIcon.getContext(), "Không thể xóa khỏi danh sách yêu thích", Toast.LENGTH_SHORT).show();
                        Log.e("ProductNewAdapter", "Remove favorite failed: " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    Toast.makeText(favoriteIcon.getContext(), "Lỗi mạng khi xóa yêu thích", Toast.LENGTH_SHORT).show();
                    Log.e("ProductNewAdapter", "Network error removing favorite: " + t.getMessage());
                }
            });
        } else {
            // Add to favorite
            RetrofitClient.api().addFavorite(userId, product.id).enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().success) {
                        favoritedProductIds.add(product.id);
                        favoriteIcon.setImageResource(R.drawable.ic_favorite);
                        Toast.makeText(favoriteIcon.getContext(), "Đã thêm vào danh sách yêu thích", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(favoriteIcon.getContext(), "Không thể thêm vào danh sách yêu thích", Toast.LENGTH_SHORT).show();
                        Log.e("ProductNewAdapter", "Add favorite failed: " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    Toast.makeText(favoriteIcon.getContext(), "Lỗi mạng khi thêm yêu thích", Toast.LENGTH_SHORT).show();
                    Log.e("ProductNewAdapter", "Network error adding favorite: " + t.getMessage());
                }
            });
        }
    }

    @Override public int getItemCount() { return data.size(); }

    // New method to get current data list
    public List<Product> getData() {
        return new ArrayList<>(data);
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img; TextView tvName, tvPrice;
        ImageView ivFavorite; // New: Favorite icon

        VH(@NonNull View v) {
            super(v);
            img = v.findViewById(R.id.imgProduct);
            tvName = v.findViewById(R.id.tvName);
            tvPrice = v.findViewById(R.id.tvPrice);
            ivFavorite = v.findViewById(R.id.ivFavorite); // Bind favorite icon
        }
    }
}
