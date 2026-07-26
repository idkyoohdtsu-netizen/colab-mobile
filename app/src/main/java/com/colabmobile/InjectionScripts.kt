package com.colabmobile

/**
 * JavaScript injected into every page after it loads.
 *
 * Goal: the page renders in full desktop layout (wide viewport + desktop UA)
 * but touch targets are enlarged so fingers can hit them reliably.
 * Font sizes are boosted so the shrunken desktop text stays readable.
 */
object InjectionScripts {

    /**
     * Universal CSS injection – works on any website.
     * Injected via evaluateJavascript() on every onPageFinished callback.
     */
    val UNIVERSAL_DESKTOP_CSS = """
(function() {
  var STYLE_ID = '__replit_mobile_patch__';
  var old = document.getElementById(STYLE_ID);
  if (old) old.remove();

  var s = document.createElement('style');
  s.id = STYLE_ID;
  s.textContent = `
    /* ── Minimum body width keeps desktop layout intact ── */
    body { min-width: 960px !important; }

    /* ── Scrollbars – thicker so fingers can grab them ── */
    ::-webkit-scrollbar          { width: 16px !important; height: 16px !important; }
    ::-webkit-scrollbar-thumb    { background: #9aa0a6 !important; border-radius: 8px !important; min-height: 40px !important; }
    ::-webkit-scrollbar-track    { background: #f1f3f4 !important; }

    /* ── Interactive elements – minimum 48 × 48 dp touch target ── */
    button, [role="button"], input[type="button"], input[type="submit"],
    input[type="reset"], a[href], select, label,
    [class*="toolbar"] [class*="button"],
    [class*="icon-button"], paper-icon-button,
    colab-toolbar-button, .goog-toolbar-button,
    .run-button, [data-type="toolbar-button"] {
      min-width:   48px !important;
      min-height:  48px !important;
      padding:     8px  !important;
      box-sizing:  border-box !important;
    }

    /* ── Dropdown / menu items ── */
    .goog-menuitem, paper-item, [role="menuitem"],
    [role="option"], li[class*="menu"] {
      min-height:  52px !important;
      line-height: 52px !important;
      padding:     0 20px !important;
      font-size:   15px !important;
    }

    /* ── Code editors ── */
    .CodeMirror, .cm-editor, .cm-content,
    .monaco-editor .view-lines,
    .notebook-cell .CodeMirror {
      font-size: 15px !important;
      line-height: 1.6  !important;
    }

    /* ── General text ── */
    p, span, div, td, th, li {
      font-size: max(13px, 0.9em) !important;
    }

    /* ── Form inputs ── */
    input[type="text"], input[type="email"], input[type="search"],
    input[type="url"], textarea {
      font-size:   16px !important;   /* prevents iOS/Android auto-zoom */
      min-height:  44px !important;
      padding:     8px  !important;
    }

    /* ── Checkboxes / radios ── */
    input[type="checkbox"], input[type="radio"] {
      width:  26px !important;
      height: 26px !important;
    }

    /* ── Links ── */
    a { padding-top: 4px !important; padding-bottom: 4px !important; }

    /* ── Google Colab – specific overrides ── */
    #top-toolbar, .notebook-toolbar, .colab-top-bar {
      background-color: #fff !important;
    }
    .run-button-container paper-icon-button,
    .cell-execution-status paper-icon-button {
      width: 52px !important; height: 52px !important;
    }
    #notebook-container, .notebook-container {
      max-width: none  !important;
      padding-left:  10px !important;
      padding-right: 10px !important;
    }
    .cell { margin-top: 6px !important; margin-bottom: 6px !important; }
    .output_area pre, .output pre { font-size: 14px !important; }

    /* ── Google Search ── */
    .gNO89b, .FPdoLc input, input[name="q"] {
      font-size: 18px !important;
      height: 48px !important;
    }
  `;
  document.head.appendChild(s);
})();
true;
    """.trimIndent()

    // ── Colab quick-action helpers ────────────────────────────────────────────

    const val RUN_CELL = """
(function() {
  var b = document.querySelector('colab-run-button paper-icon-button')
       || document.querySelector('[aria-label="Run cell"]')
       || document.querySelector('.run-button');
  if (b) b.click();
  else document.activeElement?.dispatchEvent(
    new KeyboardEvent('keydown', {key:'Enter',shiftKey:true,bubbles:true}));
})(); true;"""

    const val RUN_ALL = """
(function() {
  var b = document.querySelector('[aria-label="Run all"]')
       || document.querySelector('[data-tooltip="Run all"]');
  if (b) { b.click(); return; }
  document.querySelector('#runtime-menu-button')?.click();
})(); true;"""

    const val INTERRUPT = """
(function() {
  (document.querySelector('[aria-label="Interrupt execution"]')
|| document.querySelector('[data-tooltip="Interrupt execution"]'))?.click();
})(); true;"""

    fun key(key: String, ctrl: Boolean = false, shift: Boolean = false): String {
        val k = key.replace("'", "\\'")
        return """
(function(){
  var el=document.activeElement; if(!el) return;
  var o={key:'$k',bubbles:true,cancelable:true,ctrlKey:$ctrl,shiftKey:$shift};
  ['keydown','keypress','keyup'].forEach(t=>el.dispatchEvent(new KeyboardEvent(t,o)));
})(); true;""".trimIndent()
    }
}
