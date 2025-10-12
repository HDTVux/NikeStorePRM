package com.example.nikestore.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nikestore.R;
import com.example.nikestore.model.Review;

import java.util.ArrayList;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.VH> {

    private final List<Review> data = new ArrayList<>();

    public void submitList(List<Review> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Review r = data.get(position);
        holder.tvReviewer.setText(r.username != null ? r.username : "User");
        holder.ratingBar.setRating(r.rating);
        holder.tvComment.setText(r.comment != null ? r.comment : "");
        holder.tvCreatedAt.setText(r.created_at != null ? r.created_at : "");
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvReviewer, tvComment, tvCreatedAt;
        RatingBar ratingBar;
        VH(@NonNull View v) {
            super(v);
            tvReviewer = v.findViewById(R.id.tvReviewer);
            tvComment = v.findViewById(R.id.tvComment);
            tvCreatedAt = v.findViewById(R.id.tvCreatedAt);
            ratingBar = v.findViewById(R.id.ratingBar);
        }
    }
}
