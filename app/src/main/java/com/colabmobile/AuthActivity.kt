package com.colabmobile

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.webkit.*
import android.widget.LinearLayout
import android.widget.TextView

/**
 * A dedicated Activity for Google OAuth.
 *
 * All WebViews in the same app process share one CookieManager, so cookies
 * obtained here are immediately available in the main ColabWebView.
 *
 * We inject window.chrome and other Chrome APIs at page-start to satisfy
 * Google's sign-in page checks.
 */
class AuthActivity : Activity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        val startUrl = intent.getStringExtra(EXTRA_URL) ?: GOOGLE_ACCOUNTS

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
        }

        // Minimal top bar with a close button
        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(Color.parseColor("#1a73e8"))
            setPadding(dp(8), 0, dp(8), 0)
        }
        val title = TextView(this).apply {
            text = "Đăng nhập Google"
            textSize = 16f
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val closeBtn = TextView(this).apply {
            text = "✕"
            textSize = 20f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true
            setOnClickListener { finish() }
        }
        topBar.addView(title)
        topBar.addView(closeBtn, LinearLayout.LayoutParams(dp(48), dp(48)))

        webView = buildAuthWebView()

        root.addView(topBar, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(56)))
        root.addView(webView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))

        setContentView(root)
        webView.loadUrl(startUrl)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun buildAuthWebView(): WebView {
        val wv = WebView(this)
        wv.setBackgroundColor(Color.WHITE)

        val cm = CookieManager.getInstance()
        cm.setAcceptCookie(true)
        cm.setAcceptThirdPartyCookies(wv, true)

        wv.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            // Use a standard mobile Chrome UA — Google allows sign-in from mobile Chrome
            userAgentString = MOBILE_CHROME_UA
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }

        wv.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                // Inject window.chrome to satisfy Google's browser checks
                view.evaluateJavascript(CHROME_INJECT, null)
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                view.evaluateJavascript(CHROME_INJECT, null)
                CookieManager.getInstance().flush()

                // When Google redirects back to Colab after sign-in → signal success
                val host = Uri.parse(url).host ?: ""
                if (host.contains("colab.research.google.com") ||
                    host.contains("drive.google.com") ||
                    host.contains("myaccount.google.com")) {
                    setResult(RESULT_OK)
                    finish()
                }
            }

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val scheme = request.url.scheme ?: return true
                return scheme != "https" && scheme != "http"
            }
        }
        wv.webChromeClient = WebChromeClient()
        return wv
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack()
        else super.onBackPressed()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density + 0.5f).toInt()

    companion object {
        const val EXTRA_URL = "start_url"
        const val GOOGLE_ACCOUNTS = "https://accounts.google.com/"
        const val REQUEST_CODE = 1001

        /**
         * Mobile Chrome UA — Google permits sign-in from mobile Chrome browsers.
         * This differs from our main WebView (which uses a Desktop UA for page layout).
         */
        const val MOBILE_CHROME_UA =
            "Mozilla/5.0 (Linux; Android 14; Pixel 8) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/125.0.0.0 Mobile Safari/537.36"

        /**
         * Inject Chrome-like global APIs that Google's sign-in page checks for.
         * Without these, Google identifies the page as an "unsupported browser".
         */
        const val CHROME_INJECT = """
(function() {
  try {
    if (!window.chrome) {
      Object.defineProperty(window, 'chrome', {
        value: {
          runtime: {
            id: undefined,
            connect: function(){},
            sendMessage: function(){}
          },
          loadTimes: function(){ return {
            requestTime: Date.now()/1000,
            startLoadTime: Date.now()/1000,
            commitLoadTime: Date.now()/1000,
            finishDocumentLoadTime: Date.now()/1000,
            finishLoadTime: Date.now()/1000,
            firstPaintTime: Date.now()/1000,
            firstPaintAfterLoadTime: 0,
            navigationType: 'Other',
            wasFetchedViaSpdy: false,
            wasNpnNegotiated: false,
            npnNegotiatedProtocol: '',
            wasAlternateProtocolAvailable: false,
            connectionInfo: 'h2'
          }; },
          csi: function(){ return {
            startE: Date.now(),
            onloadT: Date.now(),
            pageT: Math.random()*500+100,
            tran: 15
          }; },
          app: { isInstalled: false },
          webstore: { onInstallStageChanged: {}, onDownloadProgress: {} }
        },
        writable: false,
        configurable: false
      });
    }

    // Fix navigator.userAgent if needed
    if (navigator.webdriver) {
      Object.defineProperty(navigator, 'webdriver', { get: () => false });
    }

    // Plugins array (empty in real Chrome on Android but not absent)
    if (navigator.plugins.length === 0) {
      Object.defineProperty(navigator, 'plugins', {
        get: () => [1, 2, 3]
      });
    }
  } catch(e) {}
})(); true;
        """
    }
}
