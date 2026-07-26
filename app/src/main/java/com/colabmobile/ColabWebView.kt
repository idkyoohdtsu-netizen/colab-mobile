package com.colabmobile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
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

    init {
        setBackgroundColor(Color.WHITE)
        isVerticalScrollBarEnabled   = true
        isHorizontalScrollBarEnabled = true
        applySettings()

        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(this@ColabWebView, true)
        }

        webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                // Inject Chrome APIs early so Google's scripts see them
                view.evaluateJavascript(AuthActivity.CHROME_INJECT, null)
            }

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val uri  = request.url
                val host = uri.host ?: ""
                val scheme = uri.scheme ?: ""

                // Non-web URL → block
                if (scheme != "https" && scheme != "http") return true

                // Google accounts sign-in → open dedicated Auth Activity
                if (host == "accounts.google.com" || host.endsWith(".accounts.google.com")) {
                    openAuthActivity(uri.toString())
                    return true
                }

                return false // load normally in this WebView
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                view.evaluateJavascript(InjectionScripts.UNIVERSAL_DESKTOP_CSS, null)
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

    fun goBackIfPossible(): Boolean {
        return if (canGoBack()) { goBack(); true } else false
    }

    fun toggleDesktopMode() {
        val current = settings.userAgentString
        settings.userAgentString = if (current == DESKTOP_UA) MOBILE_UA else DESKTOP_UA
        reload()
    }

    /** Reload and pick up fresh cookies (called from MainActivity after AuthActivity returns) */
    fun reloadAfterAuth() {
        CookieManager.getInstance().flush()
        reload()
    }

    private fun openAuthActivity(url: String) {
        val intent = Intent(context, AuthActivity::class.java).apply {
            putExtra(AuthActivity.EXTRA_URL, url)
        }
        (context as? Activity)?.startActivityForResult(intent, AuthActivity.REQUEST_CODE)
            ?: context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun applySettings() {
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled   = true
            setSupportZoom(true)
            builtInZoomControls   = true
            displayZoomControls   = false
            useWideViewPort       = true
            // true = zoom to fit screen width, keeps desktop layout visible without blank sides
            loadWithOverviewMode  = true
            textZoom              = 100           // let the browser handle scaling
            mediaPlaybackRequiresUserGesture = false
            allowFileAccess   = false
            allowContentAccess = true
            mixedContentMode  = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            userAgentString   = DESKTOP_UA
            cacheMode         = WebSettings.LOAD_DEFAULT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                forceDark = WebSettings.FORCE_DARK_OFF
            }
        }
    }

    companion object {
        const val HOME_URL = "https://colab.research.google.com/"

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
