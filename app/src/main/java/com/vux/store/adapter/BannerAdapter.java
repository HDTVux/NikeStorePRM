package com.vux.store.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.vux.store.R;
import com.vux.store.model.Banner;

import java.util.ArrayList;
import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerVH> {

    private final List<Banner> data;

    public BannerAdapter(List<Banner> data) {
        // phòng null
        this.data = (data == null) ? new ArrayList<>() : data;
    }

    @NonNull
    @Override
    public BannerVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_banner, parent, false);
        return new BannerVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerVH h, int position) {
        Banner b = data.get(position);
        String raw = (b == null || b.image_url == null) ? "" : b.image_url;
        String img = raw.trim().replaceAll("\\s+", "");
        Log.d("BANNER_URL", "pos=" + position + " -> " + img);

        Glide.with(h.itemView.getContext())
                .load(img.isEmpty() ? null : img)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_delete)
                .centerCrop()
                .into(h.imgBanner);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class BannerVH extends RecyclerView.ViewHolder {
        ImageView imgBanner;
        BannerVH(@NonNull View itemView) {
            super(itemView);
            imgBanner = itemView.findViewById(R.id.imgBanner);
        }
    }
}
