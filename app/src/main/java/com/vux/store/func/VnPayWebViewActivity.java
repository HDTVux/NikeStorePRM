package com.vux.store.func;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.vux.store.R;

public class VnPayWebViewActivity extends AppCompatActivity {

    private static final String TAG = "VnPayWebViewActivity";
    public static final String EXTRA_VNPAY_URL = "vnpay_url";

    private WebView webView;
    private ProgressBar progressBar;
    private String initialUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vnpay_webview);

        webView = findViewById(R.id.vnpay_webview);
        progressBar = findViewById(R.id.webview_progress);

        initialUrl = getIntent().getStringExtra(EXTRA_VNPAY_URL);

        if (initialUrl == null || initialUrl.isEmpty()) {
            Toast.makeText(this, "URL thanh toán không hợp lệ.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupWebView();
        webView.loadUrl(initialUrl);
    }

    private void setupWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                Log.d(TAG, "Loading URL: " + url);

                // Kiểm tra nếu URL là deep link quay lại ứng dụng
                if (url.startsWith("app://vnpay-return")) {
                    Log.d(TAG, "Deep link caught: " + url);
                    Intent resultIntent = new Intent();
                    resultIntent.setData(Uri.parse(url));
                    setResult(RESULT_OK, resultIntent);
                    finish();
                    return true; // Đã xử lý deep link
                }

                // Mở các URL khác trong WebView
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Toast.makeText(VnPayWebViewActivity.this, "Lỗi tải trang: " + error.getDescription(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "WebView Error: " + error.getDescription() + " for URL: " + request.getUrl());
                // finish(); // Có thể chọn đóng Activity nếu lỗi nghiêm trọng
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressBar.setProgress(newProgress);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(title); // Đặt tiêu đề Activity theo tiêu đề trang web
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Nếu có các Activity khác được mở từ WebView và trả về kết quả,
        // bạn có thể xử lý ở đây.
    }
}
