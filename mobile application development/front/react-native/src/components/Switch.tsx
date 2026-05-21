import React, { useEffect, useRef, useState } from 'react';
import { Animated, Pressable, StyleSheet } from 'react-native';
import { useTokens } from '../theme/useTheme';

export function Switch({
  value: controlled,
  defaultOn = false,
  onChange,
}: {
  value?: boolean;
  defaultOn?: boolean;
  onChange?: (v: boolean) => void;
}) {
  const t = useTokens();
  const [internal, setInternal] = useState(defaultOn);
  const on = controlled ?? internal;
  const x = useRef(new Animated.Value(on ? 1 : 0)).current;

  useEffect(() => {
    Animated.timing(x, { toValue: on ? 1 : 0, duration: 180, useNativeDriver: true }).start();
  }, [on, x]);

  const tx = x.interpolate({ inputRange: [0, 1], outputRange: [0, 20] });

  function toggle() {
    const next = !on;
    if (controlled == null) setInternal(next);
    onChange?.(next);
  }

  return (
    <Pressable
      onPress={toggle}
      style={[styles.track, { backgroundColor: on ? t.accent : t.surface3 }]}
    >
      <Animated.View
        style={[
          styles.knob,
          { backgroundColor: on ? t.accentFg : t.textMute, transform: [{ translateX: tx }] },
        ]}
      />
    </Pressable>
  );
}

const styles = StyleSheet.create({
  track: { width: 46, height: 26, borderRadius: 14, padding: 3, justifyContent: 'center' },
  knob: { width: 20, height: 20, borderRadius: 10 },
});
