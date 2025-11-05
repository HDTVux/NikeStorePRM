package com.vux.store.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.vux.store.R;

import java.util.List;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.VH> {
    private final List<String> urls;

    public ImageSliderAdapter(List<String> urls) {
        this.urls = urls;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_slider, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        String url = urls.get(position);
        Glide.with(holder.itemView.getContext())
                .load(url)
                .placeholder(R.drawable.ic_image_placeholder) // add this drawable or replace with android.R.drawable.*
                .error(R.drawable.ic_broken_image)
                .centerCrop()
                .into(holder.img);
    }

    @Override public int getItemCount() { return urls.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        VH(@NonNull View v) {
            super(v);
            img = v.findViewById(R.id.imgSlider);
        }
    }
}
