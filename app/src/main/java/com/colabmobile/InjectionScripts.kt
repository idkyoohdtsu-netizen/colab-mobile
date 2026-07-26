package com.colabmobile

/**
 * JavaScript/CSS injected after every page load.
 *
 * Context: the WebView renders at 100 % desktop zoom with a desktop User-Agent.
 * The page therefore has its natural desktop dimensions — e.g. Colab is ~1 000 px
 * wide.  All CSS values here are in desktop CSS pixels and apply at that scale.
 * Touch targets must be at least 48 dp at 1:1 zoom.
 */
object InjectionScripts {

    /**
     * Injected on every onPageFinished.
     * - Keeps the desktop layout intact (no min-width override needed at 100 % zoom).
     * - Enlarges interactive elements to 48 × 48 px minimum (comfortable finger target).
     * - Increases font-size in dense UI areas.
     * - Thickens scrollbars.
     */
    val DESKTOP_TOUCH_CSS = """
(function() {
  var ID = '__colab_mobile_patch__';
  var old = document.getElementById(ID);
  if (old) old.remove();

  var s = document.createElement('style');
  s.id = ID;
  s.textContent = `
    /* ── Scrollbars ─────────────────────────────────────────────── */
    ::-webkit-scrollbar              { width: 14px !important; height: 14px !important; }
    ::-webkit-scrollbar-thumb        { background: #9aa0a6 !important; border-radius: 7px !important; min-height: 48px !important; }
    ::-webkit-scrollbar-track        { background: #f1f3f4 !important; }

    /* ── Universal touch targets ────────────────────────────────── */
    button,
    [role="button"],
    input[type="button"],
    input[type="submit"],
    input[type="reset"],
    select,
    label[for],
    [class*="icon-button"],
    paper-icon-button,
    colab-toolbar-button,
    .goog-toolbar-button,
    .run-button,
    [data-type="toolbar-button"],
    .notebook-toolbar paper-icon-button {
      min-width:   48px !important;
      min-height:  48px !important;
      padding:     8px  !important;
      box-sizing:  border-box !important;
    }

    /* ── Dropdown / menu rows ────────────────────────────────────── */
    .goog-menuitem,
    paper-item,
    [role="menuitem"],
    [role="option"],
    li[class*="menu-item"] {
      min-height:  52px !important;
      line-height: 52px !important;
      padding:     0 18px !important;
      font-size:   15px !important;
    }

    /* ── Code / notebook editors ─────────────────────────────────── */
    .CodeMirror,
    .cm-editor,
    .cm-content,
    .monaco-editor .view-lines {
      font-size:   15px !important;
      line-height: 1.65 !important;
    }

    /* ── General readable text ───────────────────────────────────── */
    p, li, td, th, span, div {
      font-size: max(13px, 0.9em) !important;
    }

    /* ── Form inputs ─────────────────────────────────────────────── */
    input[type="text"],
    input[type="email"],
    input[type="search"],
    input[type="url"],
    textarea {
      font-size:  16px !important;
      min-height: 44px !important;
      padding:    8px  !important;
    }

    /* ── Checkboxes / radios ─────────────────────────────────────── */
    input[type="checkbox"],
    input[type="radio"] {
      width:  24px !important;
      height: 24px !important;
    }

    /* ── Links ───────────────────────────────────────────────────── */
    a { padding-top: 4px !important; padding-bottom: 4px !important; }

    /* ── Google Colab – specific ─────────────────────────────────── */
    .run-button-container paper-icon-button,
    .cell-execution-status paper-icon-button {
      width:  52px !important;
      height: 52px !important;
    }
    #notebook-container,
    .notebook-container {
      max-width:     none !important;
      padding-left:  10px !important;
      padding-right: 10px !important;
    }
    .cell {
      margin-top:    6px !important;
      margin-bottom: 6px !important;
    }
    .output_area pre,
    .output pre {
      font-size: 14px !important;
    }
  `;
  document.head.appendChild(s);
})();
true;
    """.trimIndent()

    // ── Quick-action helpers for Colab ────────────────────────────────────────

    const val RUN_CELL = """
(function() {
  var b = document.querySelector('colab-run-button paper-icon-button')
       || document.querySelector('[aria-label="Run cell"]')
       || document.querySelector('.run-button');
  if (b) b.click();
  else document.activeElement?.dispatchEvent(
    new KeyboardEvent('keydown',{key:'Enter',shiftKey:true,bubbles:true}));
})(); true;"""

    const val RUN_ALL = """
(function() {
  var b = document.querySelector('[aria-label="Run all"]')
       || document.querySelector('[data-tooltip="Run all"]');
  if (b) { b.click(); return; }
  document.querySelector('#runtime-menu-button')?.click();
})(); true;"""

    fun key(key: String, ctrl: Boolean = false, shift: Boolean = false): String {
        val k = key.replace("'", "\\'")
        return """
(function(){
  var el = document.activeElement; if (!el) return;
  var o = {key:'$k',bubbles:true,cancelable:true,ctrlKey:$ctrl,shiftKey:$shift};
  ['keydown','keypress','keyup'].forEach(t => el.dispatchEvent(new KeyboardEvent(t,o)));
})(); true;""".trimIndent()
    }
}
