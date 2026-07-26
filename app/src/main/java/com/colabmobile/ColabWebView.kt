package com.colabmobile

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.webkit.*

@SuppressLint("SetJavaScriptEnabled")
class ColabWebView(
    context: Context,
    private val onProgressChanged: (Int) -> Unit = {},
    private val onPageFinished: (String) -> Unit = {},
) : WebView(context) {

    private var isDesktopMode = true

    init {
        setBackgroundColor(Color.WHITE)
        isVerticalScrollBarEnabled = true
        isHorizontalScrollBarEnabled = true
        applySettings()

        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(this@ColabWebView, true)
        }

        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val scheme = request.url.scheme ?: return true
                return scheme != "https" && scheme != "http"
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                view.evaluateJavascript(InjectionScripts.UNIVERSAL_DESKTOP_CSS, null)
                this@ColabWebView.onPageFinished(url)
            }
        }

        webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                this@ColabWebView.onProgressChanged(newProgress)
            }

            override fun onReceivedTitle(view: WebView, title: String) {}
        }
    }

    fun goBackIfPossible(): Boolean {
        return if (canGoBack()) { goBack(); true } else false
    }

    fun toggleDesktopMode() {
        isDesktopMode = !isDesktopMode
        settings.userAgentString = if (isDesktopMode) DESKTOP_UA else MOBILE_UA
        reload()
    }

    private fun applySettings() {
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            useWideViewPort = true
            loadWithOverviewMode = false   // don't shrink to fit – keep desktop scale
            textZoom = 120                 // slightly larger text for readability
            mediaPlaybackRequiresUserGesture = false
            allowFileAccess = false
            allowContentAccess = true
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            userAgentString = DESKTOP_UA
            cacheMode = WebSettings.LOAD_DEFAULT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                forceDark = WebSettings.FORCE_DARK_OFF
            }
        }
    }

    companion object {
        const val HOME_URL = "https://colab.research.google.com/"

        /** Pretend to be Chrome on Windows – sites serve the full desktop layout */
        const val DESKTOP_UA =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/125.0.0.0 Safari/537.36"

        const val MOBILE_UA =
            "Mozilla/5.0 (Linux; Android 14; Pixel 8) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/125.0.0.0 Mobile Safari/537.36"
    }
}
