package com.colabmobile

import android.annotation.SuppressLint
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

@SuppressLint("SetJavaScriptEnabled")
class ColabWebView(
    context: android.content.Context,
) : WebView(context) {
    init {
        setBackgroundColor(Color.WHITE)
        setInitialScale(100)
        isVerticalScrollBarEnabled = true
        isHorizontalScrollBarEnabled = true
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            useWideViewPort = true
            // Keep the desktop canvas instead of shrinking it to the phone width.
            loadWithOverviewMode = false
            textZoom = 125
            mediaPlaybackRequiresUserGesture = false
            allowFileAccess = false
            allowContentAccess = true
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            userAgentString = DESKTOP_CHROME_USER_AGENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                forceDark = WebSettings.FORCE_DARK_OFF
            }
        }
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(this@ColabWebView, true)
        }
        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest,
            ): Boolean = !isAllowedWebUrl(request.url)

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                view.evaluateJavascript(InjectionScripts.TOUCH_CSS, null)
            }
        }
        webChromeClient = WebChromeClient()
        loadUrl(COLAB_URL)
    }

    fun goBackIfPossible(): Boolean {
        return if (canGoBack()) {
            goBack()
            true
        } else {
            false
        }
    }

    private fun isAllowedWebUrl(url: Uri): Boolean {
        return url.scheme == "https" || url.scheme == "http"
    }

    companion object {
        private const val COLAB_URL = "https://colab.research.google.com/"
        private const val DESKTOP_CHROME_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
    }
}