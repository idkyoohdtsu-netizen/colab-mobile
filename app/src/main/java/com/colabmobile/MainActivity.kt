package com.colabmobile

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

class MainActivity : android.app.Activity() {

    private lateinit var webView: ColabWebView
    private lateinit var urlInput: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var btnBack: TextView
    private lateinit var btnFwd: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.statusBarColor = Color.parseColor("#1a73e8")
        window.navigationBarColor = Color.parseColor("#f8f9fa")
        setContentView(buildLayout())
        webView.loadUrl(ColabWebView.HOME_URL)
    }

    // ── After returning from AuthActivity (Google sign-in) ────────────────────────
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AuthActivity.REQUEST_CODE) {
            // Reload to pick up freshly stored auth cookies
            webView.reloadAfterAuth()
        }
    }

    // ── Root layout ──────────────────────────────────────────────────────────────
    private fun buildLayout(): LinearLayout {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
        }
        root.addView(buildTopBar(),    lp(MATCH, dp(56)))
        root.addView(buildProgress(),  lp(MATCH, dp(3)))
        root.addView(buildWebView(),   lp(MATCH, 0, 1f))
        root.addView(buildBottomBar(), lp(MATCH, dp(64)))
        return root
    }

    // ── Top bar ───────────────────────────────────────────────────────────────────
    private fun buildTopBar(): LinearLayout {
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(Color.parseColor("#1a73e8"))
            elevation = dp(4).toFloat()
        }

        btnBack = iconTv("◀", "Quay lại") { if (webView.canGoBack()) webView.goBack() }
        btnFwd  = iconTv("▶", "Tiến")     { if (webView.canGoForward()) webView.goForward() }
        btnBack.alpha = 0.35f
        btnFwd.alpha  = 0.35f

        urlInput = EditText(this).apply {
            setSingleLine(true)
            textSize = 14f
            setTextColor(Color.WHITE)
            setHintTextColor(Color.parseColor("#aaddff"))
            hint = "Nhập URL hoặc tìm kiếm…"
            background = null
            imeOptions = EditorInfo.IME_ACTION_GO
            setOnEditorActionListener { _, actionId, event ->
                val go = actionId == EditorInfo.IME_ACTION_GO ||
                    (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
                if (go) { navigate(); true } else false
            }
        }

        val refresh = iconTv("↻", "Làm mới") {
            if (webView.progress < 100) webView.stopLoading() else webView.reload()
        }

        bar.addView(btnBack,  lp(dp(48), dp(48)))
        bar.addView(btnFwd,   lp(dp(48), dp(48)))
        bar.addView(urlInput, lp(0, WRAP, 1f).also {
            it.marginStart = dp(4); it.marginEnd = dp(4)
        })
        bar.addView(refresh,  lp(dp(48), dp(48)))
        return bar
    }

    // ── Progress bar ──────────────────────────────────────────────────────────────
    private fun buildProgress(): ProgressBar {
        progressBar = ProgressBar(
            this, null, android.R.attr.progressBarStyleHorizontal
        ).apply { max = 100; isIndeterminate = false; visibility = View.GONE }
        return progressBar
    }

    // ── WebView ───────────────────────────────────────────────────────────────────
    private fun buildWebView(): ColabWebView {
        webView = ColabWebView(
            context = this,
            onProgressChanged = { p ->
                progressBar.progress = p
                progressBar.visibility = if (p < 100) View.VISIBLE else View.GONE
            },
            onPageFinished = { url ->
                urlInput.setText(url)
                btnBack.alpha = if (webView.canGoBack())    1f else 0.35f
                btnFwd.alpha  = if (webView.canGoForward()) 1f else 0.35f
            }
        )
        return webView
    }

    // ── Bottom bar ────────────────────────────────────────────────────────────────
    private fun buildBottomBar(): LinearLayout {
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#f8f9fa"))
            elevation = dp(8).toFloat()
        }

        val buttons = listOf(
            Triple("◀", "Quay lại")  { if (webView.canGoBack())    webView.goBack() },
            Triple("▶", "Tiến")      { if (webView.canGoForward()) webView.goForward() },
            Triple("⌂", "Trang chủ") { webView.loadUrl(ColabWebView.HOME_URL) },
            Triple("⊞", "Desktop")   { webView.toggleDesktopMode() },
            Triple("⬆", "Chia sẻ")  { share() }
        )

        for ((icon, desc, action) in buttons) {
            val ta = obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground))
            val ripple = ta.getDrawable(0)
            ta.recycle()

            val cell = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                isClickable = true
                isFocusable = true
                background = ripple
                contentDescription = desc
                setOnClickListener {
                    ObjectAnimator.ofFloat(this, "scaleX", 1f, 0.86f, 1f).setDuration(140).start()
                    ObjectAnimator.ofFloat(this, "scaleY", 1f, 0.86f, 1f).setDuration(140).start()
                    action()
                }
                addView(TextView(this@MainActivity).apply {
                    text = icon
                    textSize = 22f
                    setTextColor(Color.parseColor("#5f6368"))
                    gravity = Gravity.CENTER
                })
            }
            bar.addView(cell, lp(0, MATCH, 1f))
        }
        return bar
    }

    // ── Helpers ───────────────────────────────────────────────────────────────────

    private fun iconTv(icon: String, desc: String, action: () -> Unit) = TextView(this).apply {
        text = icon
        textSize = 18f
        setTextColor(Color.WHITE)
        gravity = Gravity.CENTER
        isClickable = true
        isFocusable = true
        background = null
        contentDescription = desc
        setOnClickListener { action() }
    }

    private fun navigate() {
        val raw = urlInput.text.toString().trim()
        val url = when {
            raw.startsWith("http://") || raw.startsWith("https://") -> raw
            raw.contains(".") && !raw.contains(" ") -> "https://$raw"
            else -> "https://www.google.com/search?q=${android.net.Uri.encode(raw)}"
        }
        webView.loadUrl(url)
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(urlInput.windowToken, 0)
        urlInput.clearFocus()
    }

    private fun share() {
        startActivity(Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, webView.url ?: ColabWebView.HOME_URL)
            }, "Chia sẻ trang"
        ))
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density + 0.5f).toInt()
    private fun lp(w: Int, h: Int, weight: Float = 0f) = LinearLayout.LayoutParams(w, h, weight)
    private val MATCH = LinearLayout.LayoutParams.MATCH_PARENT
    private val WRAP  = LinearLayout.LayoutParams.WRAP_CONTENT

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (!webView.goBackIfPossible()) {
            Toast.makeText(this, "Nhấn quay lại lần nữa để thoát", Toast.LENGTH_SHORT).show()
            super.onBackPressed()
        }
    }
}
