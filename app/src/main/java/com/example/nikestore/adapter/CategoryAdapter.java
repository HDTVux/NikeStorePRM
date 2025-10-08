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
import com.example.nikestore.model.Category; // We'll create simple Category model

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.VH> {
    private List<Category> data = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener { void onClick(Category item); }
    public void setOnItemClickListener(OnItemClickListener l){ this.listener = l; }

    public void submitList(List<Category> list){
        data = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        final Category c = data.get(pos);
        h.tvName.setText(c.getName());
        // optional icon URL
        if (c.getIconUrl() != null && !c.getIconUrl().isEmpty()) {
            Glide.with(h.img.getContext()).load(c.getIconUrl()).into(h.img);
        } else {
            h.img.setImageResource(R.drawable.ic_category_placeholder);
        }
        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(c);
        });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img; TextView tvName;
        VH(@NonNull View v) {
            super(v);
            img = v.findViewById(R.id.imgCategory);
            tvName = v.findViewById(R.id.tvCategoryName);
        }
    }
}
