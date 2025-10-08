package com.example.nikestore.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nikestore.R;
import com.example.nikestore.model.Product;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ProductNewAdapter extends RecyclerView.Adapter<ProductNewAdapter.VH> {

    private final List<Product> data = new ArrayList<>();
    private final DecimalFormat money = new DecimalFormat("#,##0.##");

    public void submit(List<Product> list) {
        android.util.Log.d("PRODUCT_ADAPTER", "submit list size=" + (list == null ? 0 : list.size()));
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }


    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_new, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Product p = data.get(position);
        h.tvName.setText(p.name);
        h.tvPrice.setText("$" + money.format(p.price));

        String raw = p.image_url == null ? "" : p.image_url.trim();
        android.util.Log.d("PRODUCT_ADAPTER", "raw image_url for product " + p.id + " = '" + raw + "'");

        String finalUrl = raw;
        if (!finalUrl.isEmpty() && !finalUrl.startsWith("http://") && !finalUrl.startsWith("https://")) {
            // chuẩn hóa: loại bỏ dấu / đầu nếu có rồi prefix base image host
            String img = finalUrl.startsWith("/") ? finalUrl.substring(1) : finalUrl;
            finalUrl = com.example.nikestore.net.RetrofitClient.getImageBaseUrl() + img;
        }

        android.util.Log.d("PRODUCT_ADAPTER", "using image url = " + finalUrl);

        Glide.with(h.itemView.getContext())
                .load(finalUrl.isEmpty() ? null : finalUrl) // null -> will show placeholder
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .centerCrop()
                .into(h.img);
    }


    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img; TextView tvName, tvPrice;
        VH(@NonNull View v) {
            super(v);
            img = v.findViewById(R.id.imgProduct);
            tvName = v.findViewById(R.id.tvName);
            tvPrice = v.findViewById(R.id.tvPrice);
        }
    }
}
