import React, { useRef, forwardRef, useImperativeHandle } from 'react';
import { StyleSheet } from 'react-native';
import { WebView, WebViewMessageEvent } from 'react-native-webview';
import { INJECT_CSS } from '@/constants/injection';

// Desktop Chrome UA — bypasses Google's WebView sign-in block
const DESKTOP_UA =
  'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36';

export interface ColabWebViewRef {
  inject: (js: string) => void;
}

interface Props {
  onMessage?: (event: WebViewMessageEvent) => void;
}

const ColabWebView = forwardRef<ColabWebViewRef, Props>(({ onMessage }, ref) => {
  const webViewRef = useRef<WebView>(null);

  useImperativeHandle(ref, () => ({
    inject: (js: string) => {
      webViewRef.current?.injectJavaScript(js);
    },
  }));

  // Re-inject CSS after every navigation (Colab SPAs may lose it)
  const handleLoad = () => {
    webViewRef.current?.injectJavaScript(INJECT_CSS);
  };

  return (
    <WebView
      ref={webViewRef}
      source={{ uri: 'https://colab.research.google.com' }}
      style={styles.webview}
      userAgent={DESKTOP_UA}
      // Let Colab request desktop layout
      forceDarkOn={true}
      javaScriptEnabled={true}
      domStorageEnabled={true}
      thirdPartyCookiesEnabled={true}
      sharedCookiesEnabled={true}
      allowsInlineMediaPlayback={true}
      mediaPlaybackRequiresUserAction={false}
      // Inject touch optimizations once page loads
      injectedJavaScript={INJECT_CSS}
      onLoad={handleLoad}
      onLoadEnd={handleLoad}
      onMessage={onMessage}
      // Allow mixed content for Colab resources
      mixedContentMode="always"
      // Enable pinch zoom
      scalesPageToFit={true}
      setSupportMultipleWindows={false}
      // Show loading spinner
      startInLoadingState={true}
    />
  );
});

ColabWebView.displayName = 'ColabWebView';
export default ColabWebView;

const styles = StyleSheet.create({
  webview: {
    flex: 1,
    backgroundColor: '#1A1A2E',
  },
});
