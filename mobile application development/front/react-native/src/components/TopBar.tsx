import React, { ReactNode } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { useTokens } from '../theme/useTheme';
import { FONT_BOLD, FONT_REGULAR } from '../theme/themes';

type Props = {
  left?: ReactNode;
  right?: ReactNode;
  title?: string;
  subtitle?: string;
  large?: boolean;
  transparent?: boolean;
};

export function TopBar({ left, right, title, subtitle, large, transparent }: Props) {
  const t = useTokens();
  return (
    <View style={[styles.bar, { backgroundColor: transparent ? 'transparent' : t.bg }]}>
      <View style={styles.row}>
        <View style={styles.side}>{left}</View>
        {!large ? (
          <View style={styles.titles}>
            {title ? (
              <Text style={[styles.title, { color: t.text }]} numberOfLines={1}>
                {title}
              </Text>
            ) : null}
            {subtitle ? (
              <Text style={[styles.sub, { color: t.textMute }]} numberOfLines={1}>
                {subtitle}
              </Text>
            ) : null}
          </View>
        ) : (
          <View style={{ flex: 1 }} />
        )}
        <View style={[styles.side, { justifyContent: 'flex-end' }]}>{right}</View>
      </View>
      {large ? (
        <View style={styles.largeBlock}>
          {title ? (
            <Text style={[styles.titleLg, { color: t.text }]} numberOfLines={1}>
              {title}
            </Text>
          ) : null}
          {subtitle ? <Text style={[styles.subLg, { color: t.textMute }]}>{subtitle}</Text> : null}
        </View>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  bar: { zIndex: 5 },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 8,
    paddingVertical: 6,
    minHeight: 56,
  },
  side: {
    flexDirection: 'row',
    alignItems: 'center',
    minWidth: 48,
  },
  titles: { flex: 1, minWidth: 0, paddingHorizontal: 4 },
  title: { fontSize: 17, fontFamily: FONT_BOLD, lineHeight: 22, letterSpacing: -0.085 },
  sub: { fontSize: 12, fontFamily: FONT_REGULAR, lineHeight: 16, marginTop: 2 },
  largeBlock: { paddingHorizontal: 20, paddingTop: 4, paddingBottom: 16 },
  titleLg: { fontSize: 30, fontFamily: FONT_BOLD, letterSpacing: -0.75, lineHeight: 36 },
  subLg: { fontSize: 13, fontFamily: FONT_REGULAR, marginTop: 2 },
});
