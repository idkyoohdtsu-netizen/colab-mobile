package com.colabmobile

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.TextView

class FloatingDock(
    context: android.content.Context,
    private val inject: (String) -> Unit,
) : LinearLayout(context) {
    private var expanded = true
    private val actionColumn = LinearLayout(context)

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        setPadding(dp(6), dp(5), dp(6), dp(6))
        background = rounded(Color.rgb(27, 33, 48), Color.rgb(75, 89, 118))
        elevation = dp(8).toFloat()

        val toggle = action("⌄", Color.rgb(180, 194, 218)) {
            expanded = !expanded
            actionColumn.visibility = if (expanded) VISIBLE else GONE
            it.text = if (expanded) "⌄" else "⌃"
        }
        addView(toggle)

        actionColumn.orientation = VERTICAL
        actionColumn.gravity = Gravity.CENTER_HORIZONTAL
        addView(actionColumn)

        addAction("Run", "▶", Color.rgb(92, 204, 124), InjectionScripts.RUN_CELL)
        addAction("All", "▶▶", Color.rgb(100, 170, 255), InjectionScripts.RUN_ALL)
        addAction("Stop", "■", Color.rgb(255, 112, 91), InjectionScripts.INTERRUPT)
        addAction("Restart", "↻", Color.rgb(255, 184, 77), InjectionScripts.RESTART)
        addDivider()
        addAction("Files", "▣", Color.rgb(153, 204, 255), InjectionScripts.OPEN_FILES)
        addAction("TOC", "≡", Color.rgb(153, 204, 255), InjectionScripts.OPEN_TOC)

        val handle = TextView(context).apply {
            text = "⋮⋮"
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(113, 128, 158))
            setPadding(0, dp(2), 0, 0)
        }
        addView(handle, LayoutParams(dp(60), dp(24)))
        attachDrag(handle)
    }

    private fun addAction(label: String, glyph: String, color: Int, script: String) {
        val view = action(glyph, color) { inject(script) }
        view.contentDescription = label
        view.text = "$glyph\n$label"
        view.textSize = if (label == "Restart") 9f else 10f
        actionColumn.addView(view)
    }

    private fun addDivider() {
        val divider = android.view.View(context).apply {
            setBackgroundColor(Color.rgb(66, 78, 103))
        }
        val params = LayoutParams(dp(40), dp(1))
        params.setMargins(0, dp(4), 0, dp(4))
        actionColumn.addView(divider, params)
    }

    private fun action(
        glyph: String,
        color: Int,
        callback: (TextView) -> Unit,
    ): TextView = TextView(context).apply {
        text = glyph
        textSize = 15f
        gravity = Gravity.CENTER
        setTextColor(color)
        setPadding(dp(2), dp(3), dp(2), dp(3))
        background = rounded(Color.rgb(38, 46, 66), Color.rgb(70, 84, 112))
        isClickable = true
        setOnClickListener { callback(this) }
        val params = LayoutParams(dp(60), dp(52))
        params.setMargins(0, dp(3), 0, 0)
        layoutParams = params
    }

    private fun attachDrag(handle: TextView) {
        var startX = 0f
        var startY = 0f
        var baseX = 0f
        var baseY = 0f
        handle.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.rawX
                    startY = event.rawY
                    baseX = translationX
                    baseY = translationY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    translationX = baseX + event.rawX - startX
                    translationY = baseY + event.rawY - startY
                    true
                }
                else -> true
            }
        }
    }

    private fun rounded(fill: Int, stroke: Int): GradientDrawable =
        GradientDrawable().apply {
            setColor(fill)
            setStroke(dp(1), stroke)
            cornerRadius = dp(18).toFloat()
        }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}