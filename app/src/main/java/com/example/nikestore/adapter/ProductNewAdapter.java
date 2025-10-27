package com.example.nikestore.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nikestore.R;
import com.example.nikestore.func.Login;
import com.example.nikestore.model.ApiResponse;
import com.example.nikestore.model.Product;
import com.example.nikestore.model.WishlistResponse;
import com.example.nikestore.net.RetrofitClient;
import com.example.nikestore.util.SessionManager;

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
    private Set<Integer> favoritedProductIds = new HashSet<>();
    private final int layoutResId;

    public interface OnItemClickListener { void onItemClick(Product item); }
    private OnItemClickListener listener;
    public void setOnItemClickListener(OnItemClickListener l){ this.listener = l; }

    public ProductNewAdapter(Context context, int layoutResId) {
        this.sessionManager = SessionManager.getInstance(context);
        this.layoutResId = layoutResId;
    }

    public ProductNewAdapter(Context context) {
        this(context, R.layout.item_product_new);
    }

    public void submit(List<Product> list) {
        data.clear();
        if (list != null) {
            data.addAll(list);
        }
        loadFavoritedProducts();
        notifyDataSetChanged();
    }

    public void setFavoritedProductIds(Set<Integer> favoritedProductIds) {
        this.favoritedProductIds = favoritedProductIds;
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(layoutResId, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int position) {
        Product p = data.get(position);
        h.tvName.setText(p.name);

        Log.d("ProductNewAdapter", "Product: " + p.name + ", Price: " + p.price + ", Final Price: " + p.final_price + ", Discount: " + p.discount_percent);

        // UPDATED: Logic để hiển thị giá gốc và giá giảm giá (áp dụng cho mọi layout có các TextView này)
        if (p.discount_percent > 0 && h.tvOriginalPrice != null) { // Chỉ áp dụng nếu có discount và tvOriginalPrice tồn tại
            // Có khuyến mãi
            h.tvOriginalPrice.setText("$" + money.format(p.price));
            h.tvOriginalPrice.setPaintFlags(h.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            h.tvOriginalPrice.setVisibility(View.VISIBLE);
            h.tvPrice.setText("$" + money.format(p.final_price));
            h.tvPrice.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.red)); // Màu đỏ cho giá sale
        } else {
            // Không có khuyến mãi
            if (h.tvOriginalPrice != null) {
                h.tvOriginalPrice.setVisibility(View.GONE);
            }
            h.tvPrice.setText("$" + money.format(p.price));
            h.tvPrice.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.black)); // Màu đen cho giá thường
        }

        // Logic tải ảnh: chỉ load ảnh cho item_product_new (nếu layout có ImageView imgProduct)
        // Hiện tại, item_product_list_compact cũng có imgProduct. Nên logic này cần được điều chỉnh.
        // Để đơn giản và đúng với mục đích hiển thị giá, tôi sẽ giữ nguyên logic này
        // và giả định rằng item_product_list_compact cũng sẽ tải ảnh.
        if (h.img != null && p.image_url != null) {
            Log.d("ProductNewAdapter", "Loading image for product: " + p.name + ", URL: " + p.image_url);
            Glide.with(h.itemView.getContext())
                    .load(p.image_url.trim())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_delete)
                    .centerCrop()
                    .into(h.img);
        }

        // Logic cho nút yêu thích: chỉ hiển thị nếu layout có ivFavorite
        if (h.ivFavorite != null) {
            h.ivFavorite.setImageResource(favoritedProductIds.contains(p.id) ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
            h.ivFavorite.setOnClickListener(v -> {
                if (sessionManager.getUserId() <= 0) {
                    Toast.makeText(h.itemView.getContext(), "Vui lòng đăng nhập để thêm vào danh sách yêu thích", Toast.LENGTH_SHORT).show();
                    h.itemView.getContext().startActivity(new Intent(h.itemView.getContext(), Login.class));
                    return;
                }
                toggleFavorite(p, h.ivFavorite);
            });
        } else {
            Log.d("ProductNewAdapter", "ivFavorite is null for layoutResId: " + layoutResId);
        }

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
                    notifyDataSetChanged();
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
        if (userId <= 0) return;

        boolean isFavorited = favoritedProductIds.contains(product.id);

        if (isFavorited) {
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

    public List<Product> getData() {
        return new ArrayList<>(data);
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvName, tvPrice, tvOriginalPrice;
        ImageView ivFavorite;

        VH(@NonNull View v) {
            super(v);
            // Kiểm tra xem các view có tồn tại trong layout hiện tại không trước khi findViewById
            img = v.findViewById(R.id.imgProduct); // item_product_new, item_product_list_compact
            tvName = v.findViewById(R.id.tvName);
            tvPrice = v.findViewById(R.id.tvPrice);
            tvOriginalPrice = v.findViewById(R.id.tvOriginalPrice);
            ivFavorite = v.findViewById(R.id.ivFavorite); // item_product_new, item_product_wishlist
        }
    }
}