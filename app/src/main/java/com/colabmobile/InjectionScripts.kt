package com.colabmobile

object InjectionScripts {
    const val TOUCH_CSS = """
        (function() {
          var id = 'colab-mobile-native-style';
          var old = document.getElementById(id);
          if (old) old.remove();
          var style = document.createElement('style');
          style.id = id;
          style.textContent = `
            :root, html, body {
              color-scheme: light !important;
              background: #ffffff !important;
              color: #202124 !important;
            }
            body {
              min-width: 980px !important;
            }
            #top-toolbar, .notebook-toolbar, .colab-top-bar {
              background-color: #ffffff !important;
              color: #202124 !important;
            }
            colab-toolbar-button, .goog-toolbar-button, paper-icon-button,
            .run-button, [data-type="toolbar-button"],
            .notebook-toolbar paper-icon-button {
              min-width: 52px !important;
              min-height: 52px !important;
              padding: 11px !important;
            }
            .run-button-container paper-icon-button,
            .cell-execution-status paper-icon-button {
              width: 48px !important;
              height: 48px !important;
              padding: 8px !important;
            }
            .goog-menuitem, paper-item, .colab-dropdown-menu paper-item {
              min-height: 52px !important;
              line-height: 52px !important;
              padding: 0 18px !important;
              font-size: 16px !important;
            }
            .CodeMirror, .cm-editor, .monaco-editor .view-lines {
              font-size: 16px !important;
              line-height: 1.65 !important;
            }
            ::-webkit-scrollbar { width: 14px; height: 14px; }
            ::-webkit-scrollbar-thumb { background: #9aa0a6; border-radius: 7px; }
            #notebook-container, .notebook-container {
              max-width: none !important;
              padding-left: 12px !important;
              padding-right: 12px !important;
            }
            .cell { margin-top: 8px !important; margin-bottom: 8px !important; }
            .output_area pre, .output pre { font-size: 15px !important; }
            input[type="checkbox"] { width: 24px !important; height: 24px !important; }
            a { padding-top: 6px !important; padding-bottom: 6px !important; }
          `;
          document.head.appendChild(style);
        })();
        true;
    """

    const val RUN_CELL = """
        (function() {
          var button = document.querySelector('colab-run-button paper-icon-button') ||
            document.querySelector('[aria-label="Run cell"]') ||
            document.querySelector('.run-button');
          if (button) button.click();
          else document.activeElement?.dispatchEvent(
            new KeyboardEvent('keydown', {key:'Enter', shiftKey:true, bubbles:true})
          );
        })();
        true;
    """

    const val RUN_ALL = """
        (function() {
          var button = document.querySelector('[aria-label="Run all"]') ||
            document.querySelector('[data-tooltip="Run all"]');
          if (button) { button.click(); return; }
          var menu = document.querySelector('#runtime-menu-button');
          if (menu) menu.click();
        })();
        true;
    """

    const val INTERRUPT = """
        (function() {
          var button = document.querySelector('[aria-label="Interrupt execution"]') ||
            document.querySelector('[data-tooltip="Interrupt execution"]');
          if (button) button.click();
        })();
        true;
    """

    const val RESTART = """
        (function() {
          var button = document.querySelector('[aria-label="Restart runtime"]') ||
            document.querySelector('[data-tooltip="Restart runtime"]');
          if (button) button.click();
          else {
            var menu = document.querySelector('#runtime-menu-button');
            if (menu) menu.click();
          }
        })();
        true;
    """

    const val OPEN_FILES = """
        (function() {
          var button = document.querySelector('[aria-label="Files"]') ||
            document.querySelector('colab-filesview-button') ||
            document.querySelector('[data-tooltip="Files"]');
          if (button) button.click();
        })();
        true;
    """

    const val OPEN_TOC = """
        (function() {
          var button = document.querySelector('[aria-label="Table of contents"]') ||
            document.querySelector('[data-tooltip="Table of contents"]');
          if (button) button.click();
        })();
        true;
    """

    fun key(key: String, ctrl: Boolean = false, shift: Boolean = false, alt: Boolean = false): String {
        val safeKey = key.replace("'", "\\'")
        return """
            (function() {
              var el = document.activeElement;
              if (!el) return;
              var opts = {
                key:'$safeKey', bubbles:true, cancelable:true,
                ctrlKey:$ctrl, shiftKey:$shift, altKey:$alt
              };
              el.dispatchEvent(new KeyboardEvent('keydown', opts));
              el.dispatchEvent(new KeyboardEvent('keypress', opts));
              el.dispatchEvent(new KeyboardEvent('keyup', opts));
            })();
            true;
        """
    }

    fun insert(text: String): String {
        val escaped = text.replace("\\", "\\\\").replace("'", "\\'")
        return """
            (function() {
              var value = '$escaped';
              if (document.execCommand) document.execCommand('insertText', false, value);
            })();
            true;
        """
    }
}