import React, { ReactNode } from 'react';
import { Pressable, StyleSheet, View } from 'react-native';
import { useTokens } from '../theme/useTheme';

export function IconButton({
  icon,
  onPress,
  badge,
}: {
  icon: ReactNode;
  onPress?: () => void;
  badge?: boolean;
}) {
  const t = useTokens();
  return (
    <Pressable
      onPress={onPress}
      hitSlop={6}
      style={({ pressed }) => [
        styles.btn,
        pressed && { backgroundColor: t.surface2 },
      ]}
    >
      {icon}
      {badge ? (
        <View style={[styles.badge, { backgroundColor: t.accent, borderColor: t.bg }]} />
      ) : null}
    </Pressable>
  );
}

const styles = StyleSheet.create({
  btn: {
    width: 44,
    height: 44,
    borderRadius: 22,
    alignItems: 'center',
    justifyContent: 'center',
  },
  badge: {
    position: 'absolute',
    top: 12,
    right: 12,
    width: 7,
    height: 7,
    borderRadius: 4,
    borderWidth: 2,
  },
});
