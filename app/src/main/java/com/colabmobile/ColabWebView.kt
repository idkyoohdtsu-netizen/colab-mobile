package com.colabmobile

import android.annotation.SuppressLint
import android.graphics.Color
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

@SuppressLint("SetJavaScriptEnabled")
class ColabWebView(
    context: android.content.Context,
    private val onEdgeSwipe: (fromLeft: Boolean) -> Unit,
) : WebView(context) {
    private var downX = 0f
    private var downY = 0f

    init {
        setBackgroundColor(Color.rgb(16, 19, 27))
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            useWideViewPort = true
            loadWithOverviewMode = true
            mediaPlaybackRequiresUserGesture = false
            allowFileAccess = false
            allowContentAccess = true
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            userAgentString = DESKTOP_CHROME_USER_AGENT
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

    override fun onTouchEvent(event: android.view.MotionEvent): Boolean {
        when (event.actionMasked) {
            android.view.MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
            }
            android.view.MotionEvent.ACTION_UP -> {
                val distanceX = event.x - downX
                val nearLeft = downX < 72
                val nearRight = downX > width - 72
                if (kotlin.math.abs(distanceX) > 150 &&
                    kotlin.math.abs(distanceX) > kotlin.math.abs(event.y - downY) * 1.4f
                ) {
                    if (nearLeft && distanceX > 0) onEdgeSwipe(true)
                    if (nearRight && distanceX < 0) onEdgeSwipe(false)
                }
            }
        }
        return super.onTouchEvent(event)
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