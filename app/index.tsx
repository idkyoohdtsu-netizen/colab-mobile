import React, { useRef, useState } from 'react';
import {
  View,
  StyleSheet,
  KeyboardAvoidingView,
  Platform,
  PanResponder,
  Dimensions,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import ColabWebView, { ColabWebViewRef } from '@/components/ColabWebView';
import FloatingDock from '@/components/FloatingDock';
import ShortcutBar from '@/components/ShortcutBar';
import { JS_OPEN_FILES, JS_OPEN_TOC } from '@/constants/injection';

const SCREEN_WIDTH = Dimensions.get('window').width;
const EDGE_THRESHOLD = 30; // px from screen edge to trigger gesture

export default function HomeScreen() {
  const insets = useSafeAreaInsets();
  const webViewRef = useRef<ColabWebViewRef>(null);
  const [keyboardVisible, setKeyboardVisible] = useState(false);

  const inject = (js: string) => {
    webViewRef.current?.inject(js);
  };

  // Edge swipe gesture: left edge → Files, right edge → TOC
  const edgePanResponder = PanResponder.create({
    onMoveShouldSetPanResponder: (evt, gs) => {
      const { pageX } = evt.nativeEvent;
      const isLeftEdge = pageX < EDGE_THRESHOLD && gs.dx > 20;
      const isRightEdge = pageX > SCREEN_WIDTH - EDGE_THRESHOLD && gs.dx < -20;
      return isLeftEdge || isRightEdge;
    },
    onPanResponderRelease: (evt, gs) => {
      const { pageX } = evt.nativeEvent;
      if (pageX < EDGE_THRESHOLD + 60 && gs.dx > 20) {
        inject(JS_OPEN_FILES);
      } else if (pageX > SCREEN_WIDTH - EDGE_THRESHOLD - 60 && gs.dx < -20) {
        inject(JS_OPEN_TOC);
      }
    },
  });

  return (
    <View
      style={[styles.container, { paddingTop: insets.top }]}
      {...edgePanResponder.panHandlers}
    >
      <KeyboardAvoidingView
        style={styles.flex}
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
      >
        {/* Main WebView */}
        <ColabWebView ref={webViewRef} />

        {/* Shortcut bar above keyboard */}
        <ShortcutBar onInject={inject} />
      </KeyboardAvoidingView>

      {/* Floating dock — outside KeyboardAvoidingView so it stays fixed */}
      <FloatingDock
        onInject={inject}
        onOpenFiles={() => inject(JS_OPEN_FILES)}
        onOpenTOC={() => inject(JS_OPEN_TOC)}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0F0F1A',
  },
  flex: {
    flex: 1,
  },
});
