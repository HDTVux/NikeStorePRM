package com.example.nikestore.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nikestore.R;
import com.example.nikestore.model.ApiResponse;
import com.example.nikestore.model.GetProductReviewResponse;
import com.example.nikestore.model.OrderItem;
import com.example.nikestore.model.Review;
import com.example.nikestore.model.SubmitReviewRequest;
import com.example.nikestore.net.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductReviewAdapter extends RecyclerView.Adapter<ProductReviewAdapter.ProductReviewViewHolder> {

    private List<OrderItem> items;
    private int userId;

    public ProductReviewAdapter(List<OrderItem> items, int userId) {
        this.items = items;
        this.userId = userId;
    }

    public void submitList(List<OrderItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_review, parent, false);
        return new ProductReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductReviewViewHolder holder, int position) {
        OrderItem item = items.get(position);
        Context context = holder.itemView.getContext();

        holder.tvProductName.setText(item.product_name);
        holder.tvProductQuantity.setText("Số lượng: " + item.quantity);

        // Kiểm tra xem sản phẩm đã được đánh giá chưa
        RetrofitClient.api().getProductReview(userId, item.product_id)
                .enqueue(new Callback<GetProductReviewResponse>() {
                    @Override
                    public void onResponse(Call<GetProductReviewResponse> call, Response<GetProductReviewResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().success && response.body().review != null) {
                            // Đã đánh giá
                            Review review = response.body().review;
                            holder.layoutReviewInput.setVisibility(View.GONE);
                            holder.tvReviewStatus.setVisibility(View.VISIBLE);
                            holder.tvReviewStatus.setText("Đã đánh giá (" + review.rating + " sao): \'" + review.comment + "\'");
                            // Bạn có thể thêm nút chỉnh sửa ở đây nếu muốn
                        } else {
                            // Chưa đánh giá
                            holder.layoutReviewInput.setVisibility(View.VISIBLE);
                            holder.tvReviewStatus.setVisibility(View.GONE);
                            holder.ratingBarProduct.setRating(0);
                            holder.edtComment.setText("");

                            holder.btnSubmitReview.setOnClickListener(v -> {
                                int rating = (int) holder.ratingBarProduct.getRating();
                                String comment = holder.edtComment.getText().toString().trim();

                                if (rating < 1) {
                                    Toast.makeText(context, "Vui lòng chọn số sao", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                SubmitReviewRequest request = new SubmitReviewRequest(userId, item.product_id, rating, comment);
                                RetrofitClient.api().submitReview(request)
                                        .enqueue(new Callback<ApiResponse>() {
                                            @Override
                                            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                                                if (response.isSuccessful() && response.body() != null && response.body().success) {
                                                    Toast.makeText(context, "Đánh giá thành công!", Toast.LENGTH_SHORT).show();
                                                    // Cập nhật UI để hiển thị trạng thái đã đánh giá
                                                    holder.layoutReviewInput.setVisibility(View.GONE);
                                                    holder.tvReviewStatus.setVisibility(View.VISIBLE);
                                                    holder.tvReviewStatus.setText("Đã đánh giá (" + rating + " sao): \'" + comment + "\'");
                                                } else {
                                                    Toast.makeText(context, "Lỗi khi gửi đánh giá", Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<ApiResponse> call, Throwable t) {
                                                Toast.makeText(context, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<GetProductReviewResponse> call, Throwable t) {
                        Log.e("ProductReviewAdapter", "Lỗi khi lấy đánh giá: " + t.getMessage());
                        holder.layoutReviewInput.setVisibility(View.VISIBLE); // Mặc định hiển thị nếu lỗi
                        holder.tvReviewStatus.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ProductReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvProductQuantity, tvReviewStatus;
        RatingBar ratingBarProduct;
        EditText edtComment;
        Button btnSubmitReview;
        LinearLayout layoutReviewInput;

        public ProductReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductQuantity = itemView.findViewById(R.id.tvProductQuantity);
            ratingBarProduct = itemView.findViewById(R.id.ratingBarProduct);
            edtComment = itemView.findViewById(R.id.edtComment);
            btnSubmitReview = itemView.findViewById(R.id.btnSubmitReview);
            tvReviewStatus = itemView.findViewById(R.id.tvReviewStatus);
            layoutReviewInput = itemView.findViewById(R.id.layoutReviewInput);
        }
    }
}
