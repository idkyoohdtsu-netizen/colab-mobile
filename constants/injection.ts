// CSS injected into Google Colab to make it touch-friendly
export const INJECT_CSS = `
(function() {
  const style = document.createElement('style');
  style.id = 'colab-mobile-style';
  style.textContent = \`
    /* Enlarge all toolbar buttons for touch */
    colab-toolbar-button,
    .goog-toolbar-button,
    paper-icon-button,
    .run-button,
    [data-type="toolbar-button"],
    .notebook-toolbar paper-icon-button {
      min-width: 48px !important;
      min-height: 48px !important;
      padding: 10px !important;
    }

    /* Larger run cell button */
    .run-button-container paper-icon-button,
    .cell-execution-status paper-icon-button {
      width: 40px !important;
      height: 40px !important;
      padding: 6px !important;
    }

    /* Make menu items tappable */
    .goog-menuitem,
    paper-item,
    .colab-dropdown-menu paper-item {
      min-height: 48px !important;
      line-height: 48px !important;
      padding: 0 16px !important;
      font-size: 15px !important;
    }

    /* Code cell font size */
    .CodeMirror,
    .cm-editor,
    .monaco-editor .view-lines {
      font-size: 13px !important;
      line-height: 1.6 !important;
    }

    /* Wider scrollbar for touch */
    ::-webkit-scrollbar { width: 12px; height: 12px; }
    ::-webkit-scrollbar-thumb { background: #555; border-radius: 6px; }

    /* Notebook content full width */
    #notebook-container,
    .notebook-container {
      max-width: 100% !important;
      padding: 0 4px !important;
    }

    /* Hide file browser toggle (we handle with gesture) */
    #toggle-header-button { display: none !important; }

    /* Cell margin adjustments */
    .cell { margin: 4px 0 !important; }

    /* Output area font */
    .output_area pre, .output pre {
      font-size: 12px !important;
    }

    /* Top menu bar font */
    .goog-menu,
    .colab-top-bar {
      font-size: 15px !important;
    }

    /* Larger checkboxes/toggles */
    input[type="checkbox"] {
      width: 22px !important;
      height: 22px !important;
    }

    /* Make links easier to tap */
    a { padding: 4px 0 !important; }
  \`;
  
  if (!document.getElementById('colab-mobile-style')) {
    document.head.appendChild(style);
  }
})();
true;
`;

// JavaScript helpers — called from FloatingDock via injectJavaScript
export const JS_RUN_CELL = `
(function() {
  // Try multiple selectors for run button
  const btn = document.querySelector('colab-run-button paper-icon-button') ||
              document.querySelector('[aria-label="Run cell"]') ||
              document.querySelector('.run-button');
  if (btn) btn.click();
  else {
    // Fallback: keyboard shortcut Shift+Enter
    const event = new KeyboardEvent('keydown', { key: 'Enter', shiftKey: true, bubbles: true });
    document.activeElement?.dispatchEvent(event);
  }
})();
true;
`;

export const JS_RUN_ALL = `
(function() {
  const btn = document.querySelector('[aria-label="Run all"]') ||
              document.querySelector('[data-tooltip="Run all"]');
  if (btn) { btn.click(); return; }
  // Fallback: Runtime > Run all via menu
  const menu = document.querySelector('#runtime-menu-button') ||
               document.querySelector('colab-menu-item[menu-id="runtime"]');
  if (menu) menu.click();
})();
true;
`;

export const JS_INTERRUPT = `
(function() {
  const btn = document.querySelector('[aria-label="Interrupt execution"]') ||
              document.querySelector('[data-tooltip="Interrupt execution"]');
  if (btn) btn.click();
})();
true;
`;

export const JS_RESTART = `
(function() {
  const btn = document.querySelector('[aria-label="Restart runtime"]') ||
              document.querySelector('[data-tooltip="Restart runtime"]');
  if (btn) { btn.click(); return; }
  const menu = document.querySelector('#runtime-menu-button');
  if (menu) menu.click();
})();
true;
`;

export const JS_OPEN_FILES = `
(function() {
  const filesBtn = document.querySelector('[aria-label="Files"]') ||
                   document.querySelector('colab-filesview-button') ||
                   document.querySelector('[data-tooltip="Files"]');
  if (filesBtn) filesBtn.click();
})();
true;
`;

export const JS_OPEN_TOC = `
(function() {
  const tocBtn = document.querySelector('[aria-label="Table of contents"]') ||
                 document.querySelector('[data-tooltip="Table of contents"]');
  if (tocBtn) tocBtn.click();
})();
true;
`;

// Inject a key into the focused code editor
export const buildJSKey = (key: string, modifiers?: { ctrl?: boolean; shift?: boolean; alt?: boolean }) => `
(function() {
  const el = document.activeElement;
  if (!el) return;
  const opts = {
    key: '${key}',
    bubbles: true,
    cancelable: true,
    ctrlKey: ${modifiers?.ctrl ?? false},
    shiftKey: ${modifiers?.shift ?? false},
    altKey: ${modifiers?.alt ?? false},
  };
  el.dispatchEvent(new KeyboardEvent('keydown', opts));
  el.dispatchEvent(new KeyboardEvent('keypress', opts));
  el.dispatchEvent(new KeyboardEvent('keyup', opts));
})();
true;
`;
