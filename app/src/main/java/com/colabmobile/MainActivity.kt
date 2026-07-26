package com.colabmobile

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.widget.*
import androidx.core.content.ContextCompat

class MainActivity : android.app.Activity() {

    private lateinit var webView: ColabWebView
    private lateinit var urlInput: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var btnBack: ImageButton
    private lateinit var btnForward: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        window.statusBarColor = Color.parseColor("#1a73e8")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(0, 0)
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
        window.navigationBarColor = Color.parseColor("#f8f9fa")

        val root = buildLayout()
        setContentView(root)
        webView.loadUrl(ColabWebView.HOME_URL)
    }

    private fun buildLayout(): LinearLayout {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
        }

        // ── Top URL bar ──────────────────────────────────────────────────────────
        val topBar = buildTopBar()
        root.addView(topBar, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(56)
        ))

        // Progress bar sits directly below topBar
        progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            isIndeterminate = false
            progressDrawable = resources.getDrawable(android.R.drawable.progress_horizontal, theme)
            visibility = View.GONE
        }
        root.addView(progressBar, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(3)
        ))

        // ── WebView ───────────────────────────────────────────────────────────────
        webView = ColabWebView(this,
            onProgressChanged = { progress ->
                progressBar.progress = progress
                progressBar.visibility = if (progress < 100) View.VISIBLE else View.GONE
            },
            onPageFinished = { url ->
                urlInput.setText(url)
                btnBack.alpha = if (webView.canGoBack()) 1f else 0.38f
                btnForward.alpha = if (webView.canGoForward()) 1f else 0.38f
            }
        )
        root.addView(webView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        ))

        // ── Bottom nav bar ────────────────────────────────────────────────────────
        val bottomBar = buildBottomBar()
        root.addView(bottomBar, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(64)
        ))

        return root
    }

    // ── Top bar: [back] [forward] [URL input] [refresh] ──────────────────────────
    private fun buildTopBar(): LinearLayout {
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(Color.parseColor("#1a73e8"))
            elevation = dp(4).toFloat()
        }

        btnBack = iconBtn(android.R.drawable.ic_media_previous, "Back", Color.WHITE) {
            if (webView.canGoBack()) webView.goBack()
        }
        btnForward = iconBtn(android.R.drawable.ic_media_next, "Forward", Color.WHITE) {
            if (webView.canGoForward()) webView.goForward()
        }
        btnBack.alpha = 0.38f
        btnForward.alpha = 0.38f

        urlInput = EditText(this).apply {
            setSingleLine(true)
            textSize = 14f
            setTextColor(Color.WHITE)
            setHintTextColor(Color.parseColor("#aaddff"))
            hint = "Nhập URL hoặc tìm kiếm..."
            background = null
            imeOptions = EditorInfo.IME_ACTION_GO
            setOnEditorActionListener { _, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_GO ||
                    (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                    navigateToInput()
                    true
                } else false
            }
        }

        val refreshBtn = iconBtn(android.R.drawable.ic_menu_rotate, "Refresh", Color.WHITE) {
            if (webView.progress < 100) webView.stopLoading()
            else webView.reload()
        }

        bar.addView(btnBack, navIconParams())
        bar.addView(btnForward, navIconParams())
        bar.addView(urlInput, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
            marginStart = dp(4)
            marginEnd = dp(4)
        })
        bar.addView(refreshBtn, navIconParams())

        return bar
    }

    // ── Bottom bar: [back] [forward] [home] [tabs/desktop] [share] ───────────────
    private fun buildBottomBar(): LinearLayout {
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#f8f9fa"))
            elevation = dp(8).toFloat()
        }

        val items = listOf(
            Triple("◀", "Quay lại") { if (webView.canGoBack()) webView.goBack() },
            Triple("▶", "Tiến") { if (webView.canGoForward()) webView.goForward() },
            Triple("⌂", "Trang chủ") { webView.loadUrl(ColabWebView.HOME_URL) },
            Triple("⊞", "Desktop") { webView.toggleDesktopMode() },
            Triple("⬆", "Chia sẻ") { shareCurrentUrl() }
        )

        items.forEach { (icon, desc, action) ->
            val btn = buildBottomButton(icon, desc, action)
            bar.addView(btn, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f))
        }

        return bar
    }

    private fun buildBottomButton(icon: String, desc: String, action: () -> Unit): LinearLayout {
        val cell = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true
            setRipple()
            setOnClickListener {
                ObjectAnimator.ofFloat(this, "scaleX", 1f, 0.88f, 1f).setDuration(150).start()
                ObjectAnimator.ofFloat(this, "scaleY", 1f, 0.88f, 1f).setDuration(150).start()
                action()
            }
            contentDescription = desc
        }
        val tv = TextView(this).apply {
            text = icon
            textSize = 22f
            setTextColor(Color.parseColor("#5f6368"))
            gravity = Gravity.CENTER
        }
        cell.addView(tv)
        return cell
    }

    private fun navigateToInput() {
        val raw = urlInput.text.toString().trim()
        val url = if (raw.startsWith("http://") || raw.startsWith("https://")) {
            raw
        } else if (raw.contains(".") && !raw.contains(" ")) {
            "https://$raw"
        } else {
            "https://www.google.com/search?q=${android.net.Uri.encode(raw)}"
        }
        webView.loadUrl(url)
        hideKeyboard()
    }

    private fun shareCurrentUrl() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, webView.url ?: ColabWebView.HOME_URL)
        }
        startActivity(Intent.createChooser(intent, "Chia sẻ trang"))
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(urlInput.windowToken, 0)
        urlInput.clearFocus()
    }

    private fun iconBtn(resId: Int, desc: String, tint: Int, action: () -> Unit): ImageButton {
        return ImageButton(this).apply {
            setImageResource(resId)
            setColorFilter(tint)
            background = null
            contentDescription = desc
            setPadding(dp(10), dp(10), dp(10), dp(10))
            setOnClickListener { action() }
        }
    }

    private fun navIconParams() = LinearLayout.LayoutParams(dp(48), dp(48))

    private fun dp(v: Int) = (v * resources.displayMetrics.density + 0.5f).toInt()

    override fun onBackPressed() {
        if (!webView.goBackIfPossible()) {
            Toast.makeText(this, "Nhấn quay lại lần nữa để thoát", Toast.LENGTH_SHORT).show()
            super.onBackPressed()
        }
    }
}

private fun View.setRipple() {
    val attrs = intArrayOf(android.R.attr.selectableItemBackground)
    val ta = context.obtainStyledAttributes(attrs)
    background = ta.getDrawable(0)
    ta.recycle()
}
