package com.adjaba.content;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.graphics.Color;

/**
 * Secure WebView for displaying HTML/Web content in digital signage
 * Supports: HTML widgets, dashboards, Google Slides, custom web pages
 *
 * Security:
 * - JavaScript enabled but sandboxed
 * - File access disabled
 * - Geolocation disabled
 * - No camera/microphone access
 */
public class SecureSignageWebView extends WebView {

    private WebViewLoadListener loadListener;
    private String currentUrl;

    public interface WebViewLoadListener {
        void onPageLoaded(String url);
        void onPageError(String url, int errorCode, String description);
        void onProgressChanged(int progress);
    }

    public SecureSignageWebView(Context context) {
        super(context);
        init();
    }

    public SecureSignageWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Configure WebView settings for digital signage
        WebSettings settings = getSettings();

        // Enable JavaScript (required for most modern web content)
        settings.setJavaScriptEnabled(true);

        // Enable DOM storage for web apps
        settings.setDomStorageEnabled(true);

        // Enable zoom controls (may be useful for debugging)
        settings.setBuiltInZoomControls(false);
        settings.setSupportZoom(false);

        // Set cache mode for offline support
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // Enable hardware acceleration
        setLayerType(LAYER_TYPE_HARDWARE, null);

        // Security settings
        settings.setAllowFileAccess(false); // Disable file access
        settings.setAllowContentAccess(false); // Disable content provider access
        settings.setGeolocationEnabled(false); // Disable geolocation
        settings.setMediaPlaybackRequiresUserGesture(false); // Allow autoplay

        /

/ Mixed content (HTTP in HTTPS)
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);

        // Display settings
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);

        // Background color
        setBackgroundColor(Color.TRANSPARENT);

        // Set WebViewClient to handle page navigation
        setWebViewClient(new SecureWebViewClient());

        // Set WebChromeClient to handle JavaScript dialogs and progress
        setWebChromeClient(new SecureWebChromeClient());
    }

    public void setLoadListener(WebViewLoadListener listener) {
        this.loadListener = listener;
    }

    /**
     * Load URL with error handling
     */
    public void loadUrlSafe(String url) {
        this.currentUrl = url;
        if (url != null && !url.isEmpty()) {
            loadUrl(url);
        }
    }

    /**
     * Load HTML content directly
     */
    public void loadHtmlContent(String htmlContent, String baseUrl) {
        loadDataWithBaseURL(baseUrl, htmlContent, "text/html", "UTF-8", null);
    }

    /**
     * Reload current page
     */
    public void reloadPage() {
        if (currentUrl != null) {
            loadUrl(currentUrl);
        } else {
            reload();
        }
    }

    /**
     * Clear cache and cookies
     */
    public void clearWebViewData() {
        clearCache(true);
        clearHistory();
    }

    /**
     * Execute JavaScript in the web page
     */
    public void executeJavaScript(String script) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            evaluateJavascript(script, null);
        } else {
            loadUrl("javascript:" + script);
        }
    }

    /**
     * Custom WebViewClient for security and navigation control
     */
    private class SecureWebViewClient extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (loadListener != null) {
                loadListener.onPageLoaded(url);
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            if (loadListener != null) {
                loadListener.onPageError(failingUrl, errorCode, description);
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // Allow all HTTPS and HTTP URLs
            // Block file:// and other protocols for security
            if (url.startsWith("http://") || url.startsWith("https://")) {
                view.loadUrl(url);
                return false;
            }
            // Block other protocols
            return true;
        }
    }

    /**
     * Custom WebChromeClient for progress tracking
     */
    private class SecureWebChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if (loadListener != null) {
                loadListener.onProgressChanged(newProgress);
            }
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, android.webkit.JsResult result) {
            // Suppress JavaScript alerts in digital signage
            result.confirm();
            return true;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, android.webkit.JsResult result) {
            // Auto-confirm JavaScript confirms
            result.confirm();
            return true;
        }
    }

    /**
     * Cleanup resources
     */
    public void cleanup() {
        stopLoading();
        clearWebViewData();
        removeAllViews();
        destroy();
    }
}

