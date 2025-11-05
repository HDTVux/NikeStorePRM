package com.vux.store.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.vux.store.R;
import com.vux.store.model.Product;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ListProductAdapter extends RecyclerView.Adapter<ListProductAdapter.VH> {

    public interface OnItemClickListener {
        void onItemClick(Product item);
    }

    private final List<Product> items = new ArrayList<>();
    private final Context ctx;
    private OnItemClickListener listener;

    public ListProductAdapter(Context ctx) { this.ctx = ctx; }

    public void submit(List<Product> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    public void addAll(List<Product> list) {
        if (list == null || list.isEmpty()) return;
        int start = items.size();
        items.addAll(list);
        notifyItemRangeInserted(start, list.size());
    }

    public void setOnItemClickListener(OnItemClickListener l) { this.listener = l; }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_list_compact, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Product p = items.get(position);
        holder.tvName.setText(p.name != null ? p.name : "");
        holder.tvPrice.setText(String.format(java.util.Locale.US, "$%.2f", p.price));
        String img = p.image_url;
        if (img == null || img.isEmpty()) {
            holder.img.setImageResource(R.drawable.ic_image_placeholder);
        } else {
            Glide.with(ctx).load(img).centerCrop().into(holder.img);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(p);
        });
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView tvName, tvPrice;
        VH(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgProduct);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }
}
