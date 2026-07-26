import React from 'react';
import {
  View,
  TouchableOpacity,
  Text,
  StyleSheet,
  ScrollView,
} from 'react-native';
import * as Haptics from 'expo-haptics';
import { buildJSKey } from '@/constants/injection';

interface Props {
  onInject: (js: string) => void;
}

const SHORTCUTS = [
  { label: 'Tab', js: buildJSKey('Tab') },
  { label: 'Ctrl', js: buildJSKey('Control') },
  { label: 'Shift', js: buildJSKey('Shift') },
  { label: 'Alt', js: buildJSKey('Alt') },
  { label: '( )', js: `(function(){document.execCommand('insertText',false,'()');})();true;` },
  { label: '[ ]', js: `(function(){document.execCommand('insertText',false,'[]');})();true;` },
  { label: '{ }', js: `(function(){document.execCommand('insertText',false,'{}');})();true;` },
  { label: ':', js: `(function(){document.execCommand('insertText',false,':');})();true;` },
  { label: '#', js: `(function(){document.execCommand('insertText',false,'#');})();true;` },
  { label: '/', js: `(function(){document.execCommand('insertText',false,'/');})();true;` },
  { label: '=', js: `(function(){document.execCommand('insertText',false,'=');})();true;` },
  { label: "'", js: `(function(){document.execCommand('insertText',false,"'");})();true;` },
  { label: '"', js: `(function(){document.execCommand('insertText',false,'"');})();true;` },
  { label: '_', js: `(function(){document.execCommand('insertText',false,'_');})();true;` },
  { label: '->', js: `(function(){document.execCommand('insertText',false,'->');})(  );true;` },
  { label: '←', js: buildJSKey('ArrowLeft') },
  { label: '→', js: buildJSKey('ArrowRight') },
  { label: '↑', js: buildJSKey('ArrowUp') },
  { label: '↓', js: buildJSKey('ArrowDown') },
  { label: 'Del', js: buildJSKey('Backspace') },
  { label: 'Esc', js: buildJSKey('Escape') },
  { label: '⇧+↵', js: buildJSKey('Enter', { shift: true }) },
];

export default function ShortcutBar({ onInject }: Props) {
  const handlePress = (js: string) => {
    Haptics.selectionAsync();
    onInject(js);
  };

  return (
    <View style={styles.container}>
      <ScrollView
        horizontal
        showsHorizontalScrollIndicator={false}
        contentContainerStyle={styles.scroll}
        keyboardShouldPersistTaps="handled"
      >
        {SHORTCUTS.map((s) => (
          <TouchableOpacity
            key={s.label}
            style={styles.key}
            onPress={() => handlePress(s.js)}
            activeOpacity={0.6}
          >
            <Text style={styles.keyText}>{s.label}</Text>
          </TouchableOpacity>
        ))}
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#181825',
    borderTopWidth: 1,
    borderTopColor: '#2A2A3E',
    paddingVertical: 6,
  },
  scroll: {
    paddingHorizontal: 8,
    gap: 6,
  },
  key: {
    backgroundColor: '#2A2A3E',
    borderRadius: 8,
    paddingHorizontal: 12,
    paddingVertical: 8,
    minWidth: 40,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    borderColor: '#444',
    borderBottomWidth: 3,
    borderBottomColor: '#111',
  },
  keyText: {
    color: '#E0E0E0',
    fontSize: 13,
    fontWeight: '600',
    fontFamily: 'monospace',
  },
});
