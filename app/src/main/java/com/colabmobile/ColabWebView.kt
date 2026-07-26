package com.colabmobile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.webkit.*

@SuppressLint("SetJavaScriptEnabled")
class ColabWebView(
    context: Context,
    private val onProgressChanged: (Int) -> Unit = {},
    private val onPageFinished: (String) -> Unit = {},
) : WebView(context) {

    private var desktopMode = true

    init {
        setBackgroundColor(Color.WHITE)
        isVerticalScrollBarEnabled   = true
        isHorizontalScrollBarEnabled = true
        setInitialScale(100)          // 100% zoom — full desktop scale, no shrinking
        applySettings()

        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(this@ColabWebView, true)
        }

        webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                view.evaluateJavascript(AuthActivity.CHROME_INJECT, null)
            }

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val uri    = request.url
                val host   = uri.host ?: ""
                val scheme = uri.scheme ?: ""

                if (scheme != "https" && scheme != "http") return true

                // Google sign-in → dedicated AuthActivity (same CookieManager = shared session)
                if (host == "accounts.google.com" || host.endsWith(".accounts.google.com")) {
                    (context as? Activity)?.startActivityForResult(
                        Intent(context, AuthActivity::class.java)
                            .putExtra(AuthActivity.EXTRA_URL, uri.toString()),
                        AuthActivity.REQUEST_CODE
                    )
                    return true
                }

                return false
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                view.evaluateJavascript(InjectionScripts.DESKTOP_TOUCH_CSS, null)
                CookieManager.getInstance().flush()
                this@ColabWebView.onPageFinished(url)
            }
        }

        webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                this@ColabWebView.onProgressChanged(newProgress)
            }
        }
    }

    fun goBackIfPossible() = if (canGoBack()) { goBack(); true } else false

    fun toggleDesktopMode() {
        desktopMode = !desktopMode
        settings.userAgentString = if (desktopMode) DESKTOP_UA else MOBILE_UA
        setInitialScale(if (desktopMode) 100 else 0)
        reload()
    }

    fun reloadAfterAuth() {
        CookieManager.getInstance().flush()
        reload()
    }

    private fun applySettings() {
        settings.apply {
            javaScriptEnabled          = true
            domStorageEnabled          = true
            databaseEnabled            = true
            setSupportZoom(true)
            builtInZoomControls        = true
            displayZoomControls        = false   // hide the +/- on-screen zoom buttons
            useWideViewPort            = true    // tell browser to use a desktop-width viewport
            loadWithOverviewMode       = false   // ← DO NOT shrink — keep 100 % desktop scale
            textZoom                   = 115     // slightly larger text for readability
            mediaPlaybackRequiresUserGesture = false
            allowFileAccess            = false
            allowContentAccess         = true
            mixedContentMode           = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            userAgentString            = DESKTOP_UA
            cacheMode                  = WebSettings.LOAD_DEFAULT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                forceDark = WebSettings.FORCE_DARK_OFF
            }
        }
    }

    companion object {
        const val HOME_URL = "https://colab.research.google.com/"

        /** Chrome on Windows — sites serve the full desktop layout */
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
