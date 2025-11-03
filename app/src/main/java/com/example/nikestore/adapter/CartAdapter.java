package com.example.nikestore.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nikestore.R;
import com.example.nikestore.model.CartItem;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.VH> {
    private final List<CartItem> data = new ArrayList<>();
    private final DecimalFormat money = new DecimalFormat("#,##0.##");
    
    // NEW: Interface with onItemClicked
    public interface Listener {
        void onQtyChanged(CartItem item, int newQty);
        void onRemove(CartItem item);
        void onItemClicked(CartItem item); // New method for item clicks
    }
    
    private Listener listener;
    public void setListener(Listener l){ listener = l; }
    public void submit(List<CartItem> list) { data.clear(); if (list!=null) data.addAll(list); notifyDataSetChanged(); }
    
    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false));
    }
    
    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        CartItem it = data.get(pos);
        h.tvName.setText(it.product_name);
        h.tvVariant.setText(it.variant_size != null ? "Size: "+it.variant_size : "");

        if (it.getDiscount_percent() > 0) {
            h.tvOriginalPrice.setText("$" + money.format(it.getPrice()));
            h.tvOriginalPrice.setPaintFlags(h.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            h.tvOriginalPrice.setVisibility(View.VISIBLE);
            h.tvPrice.setText("$" + money.format(it.getFinal_price()));
            h.tvPrice.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.red));
        } else {
            h.tvOriginalPrice.setVisibility(View.GONE);
            h.tvPrice.setText("$" + money.format(it.getPrice()));
            h.tvPrice.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.black));
        }

        h.tvQty.setText(String.valueOf(it.quantity));
        Glide.with(h.itemView.getContext()).load(it.image_url==null?"":it.image_url).placeholder(android.R.drawable.ic_menu_gallery).into(h.img);

        h.btnMinus.setOnClickListener(v->{
            if (it.quantity > 1) {
                int newQ = it.quantity - 1;
                it.quantity = newQ;
                h.tvQty.setText(String.valueOf(newQ));
                if (listener!=null) listener.onQtyChanged(it, newQ);
            } else {
                if (listener!=null) listener.onRemove(it);
            }
        });
        h.btnPlus.setOnClickListener(v->{
            int newQ = it.quantity + 1;
            it.quantity = newQ;
            h.tvQty.setText(String.valueOf(newQ));
            if (listener!=null) listener.onQtyChanged(it, newQ);
        });
        h.btnRemove.setOnClickListener(v-> { if (listener!=null) listener.onRemove(it); });

        // NEW: Set click listener on the entire item view
        h.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClicked(it);
            }
        });
    }
    
    @Override public int getItemCount(){ return data.size(); }
    
    static class VH extends RecyclerView.ViewHolder {
        ImageView img; TextView tvName, tvVariant, tvPrice, tvOriginalPrice, tvQty; ImageButton btnMinus, btnPlus, btnRemove;
        VH(@NonNull View v){
            super(v);
            img = v.findViewById(R.id.img);
            tvName = v.findViewById(R.id.tvName);
            tvVariant = v.findViewById(R.id.tvVariant);
            tvOriginalPrice = v.findViewById(R.id.tvOriginalPrice);
            tvPrice = v.findViewById(R.id.tvPrice);
            tvQty = v.findViewById(R.id.tvQty);
            btnMinus = v.findViewById(R.id.btnMinus);
            btnPlus = v.findViewById(R.id.btnPlus);
            btnRemove = v.findViewById(R.id.btnRemove);
        }
    }
}