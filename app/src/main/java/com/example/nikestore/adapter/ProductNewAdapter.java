package com.example.nikestore.adapter;

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
import com.example.nikestore.model.Product;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ProductNewAdapter extends RecyclerView.Adapter<ProductNewAdapter.VH> {

    private final List<Product> data = new ArrayList<>();
    private final DecimalFormat money = new DecimalFormat("#,##0.##");

    public interface OnItemClickListener { void onItemClick(Product item); }
    private OnItemClickListener listener;
    public void setOnItemClickListener(OnItemClickListener l){ this.listener = l; }

    public void submit(List<Product> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_new, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int position) {
        Product p = data.get(position);
        h.tvName.setText(p.name);
        h.tvPrice.setText("$" + money.format(p.price));
        Glide.with(h.itemView.getContext())
                .load(p.image_url == null ? "" : p.image_url.trim())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_delete)
                .centerCrop()
                .into(h.img);

        h.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(p);
            }
        });
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

