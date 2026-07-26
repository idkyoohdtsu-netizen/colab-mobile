# Colab Mobile

Native Android Kotlin app that opens Google Colab in desktop mode while adding
touch-friendly controls for a phone.

## Included

- Desktop Chrome user agent and persistent WebView storage for Colab login.
- Touch-sized Colab controls and readable code cells.
- Floating Run, Run All, Stop, Restart, Files, and TOC dock.
- Horizontal coding shortcut bar.
- Edge swipe gestures: left edge opens Files, right edge opens Table of contents.
- GitHub Actions workflow that builds a small native debug APK on every push to `main`.

## Build

```bash
gradle assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.