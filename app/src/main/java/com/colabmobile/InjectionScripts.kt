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
            colab-toolbar-button, .goog-toolbar-button, paper-icon-button,
            .run-button, [data-type="toolbar-button"],
            .notebook-toolbar paper-icon-button {
              min-width: 48px !important;
              min-height: 48px !important;
              padding: 10px !important;
            }
            .run-button-container paper-icon-button,
            .cell-execution-status paper-icon-button {
              width: 42px !important;
              height: 42px !important;
              padding: 7px !important;
            }
            .goog-menuitem, paper-item, .colab-dropdown-menu paper-item {
              min-height: 48px !important;
              line-height: 48px !important;
              padding: 0 16px !important;
              font-size: 15px !important;
            }
            .CodeMirror, .cm-editor, .monaco-editor .view-lines {
              font-size: 14px !important;
              line-height: 1.6 !important;
            }
            ::-webkit-scrollbar { width: 12px; height: 12px; }
            ::-webkit-scrollbar-thumb { background: #667085; border-radius: 6px; }
            #notebook-container, .notebook-container {
              max-width: 100% !important;
              padding-left: 6px !important;
              padding-right: 6px !important;
            }
            .cell { margin-top: 5px !important; margin-bottom: 5px !important; }
            .output_area pre, .output pre { font-size: 13px !important; }
            input[type="checkbox"] { width: 22px !important; height: 22px !important; }
            a { padding-top: 4px !important; padding-bottom: 4px !important; }
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