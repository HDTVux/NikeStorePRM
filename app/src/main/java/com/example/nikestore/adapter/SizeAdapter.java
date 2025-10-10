package com.example.nikestore.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nikestore.R;
import com.example.nikestore.model.ProductVariant;

import java.util.ArrayList;
import java.util.List;

public class SizeAdapter extends RecyclerView.Adapter<SizeAdapter.VH> {

    private final List<ProductVariant> items = new ArrayList<>();
    private int selectedPos = -1;
    private OnSizeSelected listener;

    public interface OnSizeSelected {
        void onSizeSelected(int variantId, String size);
    }

    public SizeAdapter(List<ProductVariant> data) {
        if (data != null) items.addAll(data);
    }

    public void submitList(List<ProductVariant> data) {
        items.clear();
        if (data != null) items.addAll(data);
        selectedPos = -1;
        notifyDataSetChanged();
    }

    public void setOnSizeSelected(OnSizeSelected l) {
        this.listener = l;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_size, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        ProductVariant v = items.get(position);
        holder.tvSize.setText(v.size != null ? v.size : "N/A");

        // kiểm tra tồn kho — sửa theo kiểu dữ liệu của model:
        // nếu ProductVariant.stock là kiểu nguyên thủy `int` (thường là case của bạn), dùng:
        boolean inStock = v.stock > 0;

        // nếu ProductVariant.stock là `Integer` (có thể null), thay bằng:
        // boolean inStock = (v.stock != null && v.stock > 0);

        holder.tvSize.setAlpha(inStock ? 1f : 0.5f);

        // highlight nếu được chọn
        boolean selected = position == selectedPos;
        applyBackground(holder.tvSize.getContext(), holder.tvSize, selected);

        holder.itemView.setOnClickListener(view -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            selectedPos = pos;
            notifyDataSetChanged(); // lists nhỏ ok; nếu lớn thì nên dùng notifyItemChanged(prev) + notifyItemChanged(pos)
            if (listener != null) listener.onSizeSelected(v.id, v.size);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvSize;
        VH(View v) {
            super(v);
            tvSize = v.findViewById(R.id.tvSize);
        }
    }

    // Tạo background bo tròn + stroke programmatically (đỡ phải sửa resources)
    private void applyBackground(Context ctx, View view, boolean selected) {
        int cornerDp = 8;
        int strokeDp = 1;
        float cornerPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, cornerDp, ctx.getResources().getDisplayMetrics());
        int strokePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, strokeDp, ctx.getResources().getDisplayMetrics());

        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(cornerPx);
        if (selected) {
            gd.setColor(ContextCompat.getColor(ctx, android.R.color.holo_blue_light));
            gd.setStroke(strokePx, ContextCompat.getColor(ctx, android.R.color.holo_blue_dark));
        } else {
            gd.setColor(ContextCompat.getColor(ctx, android.R.color.transparent));
            gd.setStroke(strokePx, ContextCompat.getColor(ctx, android.R.color.darker_gray));
        }
        view.setBackground(gd);

        int padH = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, ctx.getResources().getDisplayMetrics());
        int padV = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, ctx.getResources().getDisplayMetrics());
        view.setPadding(padH, padV, padH, padV);
    }
}
