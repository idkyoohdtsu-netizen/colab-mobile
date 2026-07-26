# Colab Mobile 2.1

Native Android Kotlin app that keeps the full Google Colab desktop interface on
a phone. The desktop canvas can be panned horizontally when the phone is
narrower than the desktop layout, while controls and code remain touch-readable.

## Included

- Desktop Chrome user agent and persistent WebView storage for Colab login.
- Touch-sized Colab controls and readable code cells.
- The complete Colab desktop toolbar, sidebars, notebook, and Gemini surface.
- Light mode only; no dark overlay or custom floating controls cover the page.
- GitHub Actions workflow that builds a small native debug APK on every push to `main`.

## Build

```bash
gradle assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.