import React, { useState } from 'react';
import {
  View,
  TouchableOpacity,
  StyleSheet,
  Text,
  Animated,
  PanResponder,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import * as Haptics from 'expo-haptics';
import {
  JS_RUN_CELL,
  JS_RUN_ALL,
  JS_INTERRUPT,
  JS_RESTART,
} from '@/constants/injection';

interface Props {
  onInject: (js: string) => void;
  onOpenFiles: () => void;
  onOpenTOC: () => void;
}

const DOCK_BUTTONS = [
  { id: 'run', icon: 'play-circle', color: '#4CAF50', label: 'Run', js: JS_RUN_CELL },
  { id: 'runAll', icon: 'play-skip-forward', color: '#2196F3', label: 'All', js: JS_RUN_ALL },
  { id: 'stop', icon: 'stop-circle', color: '#FF5722', label: 'Stop', js: JS_INTERRUPT },
  { id: 'restart', icon: 'refresh-circle', color: '#FF9800', label: 'Restart', js: JS_RESTART },
] as const;

export default function FloatingDock({ onInject, onOpenFiles, onOpenTOC }: Props) {
  const [expanded, setExpanded] = useState(true);
  const pan = React.useRef(new Animated.ValueXY({ x: 0, y: 300 })).current;

  const panResponder = React.useRef(
    PanResponder.create({
      onMoveShouldSetPanResponder: (_, gs) =>
        Math.abs(gs.dx) > 5 || Math.abs(gs.dy) > 5,
      onPanResponderMove: Animated.event([null, { dx: pan.x, dy: pan.y }], {
        useNativeDriver: false,
      }),
      onPanResponderRelease: () => {
        pan.extractOffset();
      },
    })
  ).current;

  const handlePress = (js: string) => {
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium);
    onInject(js);
  };

  return (
    <Animated.View
      style={[styles.dock, { transform: pan.getTranslateTransform() }]}
      {...panResponder.panHandlers}
    >
      {/* Collapse toggle */}
      <TouchableOpacity
        style={styles.collapseBtn}
        onPress={() => setExpanded((v) => !v)}
        activeOpacity={0.7}
      >
        <Ionicons
          name={expanded ? 'chevron-down' : 'chevron-up'}
          size={16}
          color="#aaa"
        />
      </TouchableOpacity>

      {expanded && (
        <>
          {DOCK_BUTTONS.map((btn) => (
            <TouchableOpacity
              key={btn.id}
              style={[styles.btn, { borderColor: btn.color + '44' }]}
              onPress={() => handlePress(btn.js)}
              activeOpacity={0.7}
            >
              <Ionicons name={btn.icon as any} size={26} color={btn.color} />
              <Text style={[styles.btnLabel, { color: btn.color }]}>{btn.label}</Text>
            </TouchableOpacity>
          ))}

          <View style={styles.divider} />

          <TouchableOpacity
            style={styles.sideBtn}
            onPress={() => { Haptics.selectionAsync(); onOpenFiles(); }}
            activeOpacity={0.7}
          >
            <Ionicons name="folder-open-outline" size={22} color="#90CAF9" />
          </TouchableOpacity>

          <TouchableOpacity
            style={styles.sideBtn}
            onPress={() => { Haptics.selectionAsync(); onOpenTOC(); }}
            activeOpacity={0.7}
          >
            <Ionicons name="list-outline" size={22} color="#90CAF9" />
          </TouchableOpacity>
        </>
      )}

      {/* Drag handle */}
      <View style={styles.handle}>
        <Ionicons name="reorder-three-outline" size={18} color="#555" />
      </View>
    </Animated.View>
  );
}

const styles = StyleSheet.create({
  dock: {
    position: 'absolute',
    right: 12,
    top: 100,
    backgroundColor: '#1E1E2E',
    borderRadius: 18,
    paddingVertical: 8,
    paddingHorizontal: 6,
    alignItems: 'center',
    gap: 6,
    elevation: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.4,
    shadowRadius: 8,
    borderWidth: 1,
    borderColor: '#333',
    zIndex: 999,
    minWidth: 58,
  },
  collapseBtn: {
    paddingVertical: 2,
  },
  btn: {
    width: 50,
    height: 50,
    borderRadius: 14,
    backgroundColor: '#2A2A3E',
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    gap: 2,
  },
  btnLabel: {
    fontSize: 9,
    fontWeight: '700',
    letterSpacing: 0.3,
  },
  sideBtn: {
    width: 50,
    height: 40,
    borderRadius: 12,
    backgroundColor: '#2A2A3E',
    alignItems: 'center',
    justifyContent: 'center',
  },
  divider: {
    width: 36,
    height: 1,
    backgroundColor: '#333',
    marginVertical: 2,
  },
  handle: {
    paddingTop: 2,
  },
});
