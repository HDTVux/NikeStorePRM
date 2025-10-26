package com.example.nikestore.func;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nikestore.R;
import com.example.nikestore.adapter.ProductNewAdapter;
import com.example.nikestore.model.NewProductsResponse;
import com.example.nikestore.model.Product;
import com.example.nikestore.net.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryProductsActivity extends BaseActivity {
    public static final String EXTRA_CAT_ID = "cat_id";
    public static final String EXTRA_CAT_NAME = "cat_name";

    private RecyclerView rvProducts;
    private ProductNewAdapter adapter;
    private int categoryId;
    private String categoryName;
    private TextView tvEmpty; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_products);

        // BIND VIEWS
        rvProducts = findViewById(R.id.rvCategoryProducts);
        tvEmpty = findViewById(R.id.tvEmptyCategory); 

        // INIT ADAPTER + LAYOUTMANAGER BEFORE ANY submit(...) CALL
        adapter = new ProductNewAdapter(this);
        try {
            adapter.setOnItemClickListener(item -> {
                Log.d("PRODUCT_CLICK", "CategoryProducts -> open product id=" + item.id);
                Intent i = new Intent(CategoryProductsActivity.this, com.example.nikestore.func.ProductDetailActivity.class);
                i.putExtra("product_id", item.id);
                startActivity(i);
            });
        } catch (Throwable ignore) {  }

        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        rvProducts.setAdapter(adapter);

        // Read extras
        categoryId = getIntent().getIntExtra(EXTRA_CAT_ID, 0);
        categoryName = getIntent().getStringExtra(EXTRA_CAT_NAME);
        setTitle(categoryName != null ? categoryName : "Products");

        // Load real data
        loadCategoryProducts();
    }

    @Override
    protected int getNavigationMenuItemId() {
        return 0; // CategoryProductsActivity is not a top-level navigation destination
    }

    private void loadCategoryProducts(){
        Call<NewProductsResponse> call = RetrofitClient.api().getProductsByCategory(categoryId, 1, 50, "newest");

        try {
            Log.d("CAT_PRODUCTS", "Request URL = " + call.request().url().toString());
        } catch (Throwable t) {
            Log.e("CAT_PRODUCTS", "Cannot get request URL: " + t.getMessage(), t);
        }

        call.enqueue(new Callback<NewProductsResponse>() {
            @Override
            public void onResponse(Call<NewProductsResponse> call, Response<NewProductsResponse> response) {
                try {
                    Log.d("CAT_PRODUCTS", "HTTP code=" + response.code());
                    if (response.body() != null) {
                        try {
                            Log.d("CAT_PRODUCTS", "parsed body: " + new com.google.gson.Gson().toJson(response.body()));
                        } catch(Throwable ignore){}
                    } else {
                        try {
                            String raw = response.errorBody() != null ? response.errorBody().string() : "null";
                            Log.d("CAT_PRODUCTS", "empty body, error raw: " + raw);
                        } catch (Throwable ignore) {}
                    }

                    List<Product> list = null;
                    if (response.isSuccessful() && response.body() != null && response.body().success) {
                        try {
                            list = response.body().getProductList();
                        } catch (Throwable t) {
                            Log.w("CAT_PRODUCTS", "getProductList() not present or failed: " + t.getMessage());
                        }
                    }

                    if (list == null) list = new ArrayList<>();

                    Log.d("CAT_PRODUCTS", "final product count = " + list.size());
                    if (adapter != null) {
                        adapter.submit(list);
                    } else {
                        Log.e("CAT_PRODUCTS", "adapter is null! cannot submit list");
                    }

                    if (tvEmpty != null) {
                        tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                        rvProducts.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
                    }
                } catch (Throwable t) {
                    Log.e("CAT_PRODUCTS","onResponse crash-guard: " + t.getMessage(), t);
                    if (adapter != null) adapter.submit(new ArrayList<>());
                    if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<NewProductsResponse> call, Throwable t) {
                Log.e("CAT_PRODUCTS","err", t);
                Toast.makeText(CategoryProductsActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
                if (adapter != null) adapter.submit(new ArrayList<>());
                if (tvEmpty != null) tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }
}
