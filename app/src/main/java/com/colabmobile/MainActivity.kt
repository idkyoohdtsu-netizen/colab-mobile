package com.colabmobile

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import android.widget.Toast

class MainActivity : android.app.Activity() {
    private lateinit var colabWebView: ColabWebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.statusBarColor = Color.WHITE
        window.navigationBarColor = Color.WHITE
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            )

        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.WHITE)
        }
        // The WebView is the complete Colab desktop surface. No custom overlay
        // sits on top of it, so menus, notebooks, sidebars, and Gemini stay intact.
        colabWebView = ColabWebView(this)
        root.addView(
            colabWebView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            ),
        )
        setContentView(root)
    }

    override fun onBackPressed() {
        if (!colabWebView.goBackIfPossible()) {
            Toast.makeText(this, "Nhấn quay lại lần nữa để thoát", Toast.LENGTH_SHORT).show()
            super.onBackPressed()
        }
    }
}