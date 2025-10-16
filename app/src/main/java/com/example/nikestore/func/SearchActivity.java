package com.example.nikestore.func;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nikestore.R;
import com.example.nikestore.adapter.ListProductAdapter;
import com.example.nikestore.model.NewProductsResponse;
import com.example.nikestore.model.Product;
import com.example.nikestore.net.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * SearchActivity
 * - 1 product per line (compact horizontal: small image left, title+price right)
 * - debounce input
 * - IME SEARCH handling
 * - clicking item -> ProductDetailActivity (product_id extra)
 */
public class SearchActivity extends AppCompatActivity {
    private EditText edtSearch;
    private RecyclerView rvResults;
    private TextView tvNoResult;
    private ListProductAdapter adapter;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private static final long DEBOUNCE_MS = 350L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        edtSearch = findViewById(R.id.edtSearchTerm);
        rvResults = findViewById(R.id.rvSearchResults);
        tvNoResult = findViewById(R.id.tvNoResult);

        // Adapter: compact list style (single row per product)
        adapter = new ListProductAdapter(this);
        rvResults.setAdapter(adapter);
        LinearLayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvResults.setLayoutManager(lm);
        rvResults.setHasFixedSize(true);
        rvResults.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        adapter.setOnItemClickListener(item -> {
            Intent i = new Intent(SearchActivity.this, ProductDetailActivity.class);
            i.putExtra("product_id", item.id);
            startActivity(i);
        });

        // Debounce text changes (simpler: use setOnKeyListener + text watcher)
        edtSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(android.text.Editable s) {}

            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) handler.removeCallbacks(searchRunnable);
                searchRunnable = () -> doSearch(s.toString().trim());
                handler.postDelayed(searchRunnable, DEBOUNCE_MS);
            }
        });

        // Handle IME search button
        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                if (searchRunnable != null) handler.removeCallbacks(searchRunnable);
                doSearch(edtSearch.getText().toString().trim());
                hideKeyboard();
                handled = true;
            }
            return handled;
        });

        // If started with initial query (from HomePage)
        String q = getIntent().getStringExtra("q");
        if (q != null && !q.isEmpty()) {
            edtSearch.setText(q);
            // move cursor to end
            edtSearch.setSelection(q.length());
            doSearch(q);
        }

        // initial UI state
        adapter.submit(new ArrayList<>());
        tvNoResult.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (searchRunnable != null) handler.removeCallbacks(searchRunnable);
    }

    private void doSearch(String q) {
        if (q == null || q.length() == 0) {
            adapter.submit(new ArrayList<>());
            tvNoResult.setVisibility(View.GONE);
            return;
        }

        // show a light loading state (optional)
        tvNoResult.setText("Searching..."); tvNoResult.setVisibility(View.VISIBLE);

        RetrofitClient.api().searchProducts(q).enqueue(new Callback<NewProductsResponse>() {
            @Override
            public void onResponse(Call<NewProductsResponse> call, Response<NewProductsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    List<Product> list = response.body().products;
                    if (list == null) list = new ArrayList<>();
                    adapter.submit(list);
                    tvNoResult.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                    if (list.isEmpty()) tvNoResult.setText("No products found");
                } else {
                    // show server message or generic error
                    String err = "Server error";
                    try {
                        if (response.errorBody() != null) err = response.errorBody().string();
                    } catch (Exception ignore) {}
                    adapter.submit(new ArrayList<>());
                    tvNoResult.setText("Search failed");
                    tvNoResult.setVisibility(View.VISIBLE);
                    Toast.makeText(SearchActivity.this, "Search failed: " + err, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<NewProductsResponse> call, Throwable t) {
                adapter.submit(new ArrayList<>());
                tvNoResult.setText("Network error");
                tvNoResult.setVisibility(View.VISIBLE);
                Toast.makeText(SearchActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(edtSearch.getWindowToken(), 0);
        } catch (Exception ignored) {}
    }
}
