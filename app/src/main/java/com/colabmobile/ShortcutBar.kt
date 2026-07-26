package com.colabmobile

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView

class ShortcutBar(
    context: android.content.Context,
    private val inject: (String) -> Unit,
) : HorizontalScrollView(context) {
    init {
        setBackgroundColor(Color.rgb(18, 22, 33))
        isHorizontalScrollBarEnabled = false
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(6), dp(8), dp(8))
        }
        shortcuts().forEach { (label, script) ->
            row.addView(keyView(label, script))
        }
        addView(row)
    }

    private fun keyView(label: String, script: String): TextView {
        return TextView(context).apply {
            text = label
            textSize = 13f
            setTextColor(Color.rgb(235, 240, 250))
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.MONOSPACE
            minWidth = dp(42)
            minHeight = dp(42)
            setPadding(dp(10), 0, dp(10), 0)
            background = rounded(Color.rgb(39, 47, 67), Color.rgb(82, 97, 128))
            setOnClickListener { inject(script) }
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dp(42),
            )
            params.marginEnd = dp(6)
            layoutParams = params
        }
    }

    private fun shortcuts(): List<Pair<String, String>> = listOf(
        "Tab" to InjectionScripts.key("Tab"),
        "Ctrl" to InjectionScripts.key("Control", ctrl = true),
        "Shift" to InjectionScripts.key("Shift", shift = true),
        "Alt" to InjectionScripts.key("Alt", alt = true),
        "( )" to InjectionScripts.insert("()"),
        "[ ]" to InjectionScripts.insert("[]"),
        "{ }" to InjectionScripts.insert("{}"),
        ":" to InjectionScripts.insert(":"),
        "#" to InjectionScripts.insert("#"),
        "/" to InjectionScripts.insert("/"),
        "=" to InjectionScripts.insert("="),
        "'" to InjectionScripts.insert("'"),
        "\"" to InjectionScripts.insert("\""),
        "_" to InjectionScripts.insert("_"),
        "←" to InjectionScripts.key("ArrowLeft"),
        "→" to InjectionScripts.key("ArrowRight"),
        "↑" to InjectionScripts.key("ArrowUp"),
        "↓" to InjectionScripts.key("ArrowDown"),
        "Del" to InjectionScripts.key("Backspace"),
        "Esc" to InjectionScripts.key("Escape"),
        "⇧↵" to InjectionScripts.key("Enter", shift = true),
    )

    private fun rounded(fill: Int, stroke: Int): GradientDrawable =
        GradientDrawable().apply {
            setColor(fill)
            setStroke(dp(1), stroke)
            cornerRadius = dp(9).toFloat()
        }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}