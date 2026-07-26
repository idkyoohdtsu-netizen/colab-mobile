package com.colabmobile

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import android.widget.Toast

class MainActivity : android.app.Activity() {
    private lateinit var colabWebView: ColabWebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.statusBarColor = Color.rgb(16, 19, 27)
        window.navigationBarColor = Color.rgb(16, 19, 27)

        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.rgb(16, 19, 27))
        }
        colabWebView = ColabWebView(this) { fromLeft ->
            colabWebView.evaluateJavascript(
                if (fromLeft) InjectionScripts.OPEN_FILES else InjectionScripts.OPEN_TOC,
                null,
            )
        }
        root.addView(
            colabWebView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            ),
        )

        val shortcutBar = ShortcutBar(this) { script ->
            colabWebView.evaluateJavascript(script, null)
        }
        val shortcutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            dp(56),
            Gravity.BOTTOM,
        )
        root.addView(shortcutBar, shortcutParams)

        val dock = FloatingDock(this) { script ->
            colabWebView.evaluateJavascript(script, null)
        }
        val dockParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.END or Gravity.TOP,
        ).apply {
            topMargin = dp(92)
            marginEnd = dp(10)
        }
        root.addView(dock, dockParams)
        setContentView(root)
    }

    override fun onBackPressed() {
        if (!colabWebView.goBackIfPossible()) {
            Toast.makeText(this, "Nhấn quay lại lần nữa để thoát", Toast.LENGTH_SHORT).show()
            super.onBackPressed()
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}