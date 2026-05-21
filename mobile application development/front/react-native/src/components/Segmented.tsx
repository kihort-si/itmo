import React from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { useTokens } from '../theme/useTheme';
import { FONT_BOLD } from '../theme/themes';

export type SegOption<V extends string> = { value: V; label: string };

export function Segmented<V extends string>({
  value,
  onChange,
  options,
}: {
  value: V;
  onChange: (v: V) => void;
  options: SegOption<V>[];
}) {
  const t = useTokens();
  return (
    <View
      style={[
        styles.seg,
        { backgroundColor: t.surface, borderColor: t.hairline },
      ]}
    >
      {options.map((o) => {
        const on = o.value === value;
        return (
          <Pressable
            key={o.value}
            onPress={() => onChange(o.value)}
            style={[
              styles.opt,
              on && { backgroundColor: t.surface3 },
            ]}
          >
            <Text style={[styles.optTxt, { color: on ? t.text : t.textMute }]}>
              {o.label}
            </Text>
          </Pressable>
        );
      })}
    </View>
  );
}

const styles = StyleSheet.create({
  seg: {
    flexDirection: 'row',
    borderWidth: 1,
    borderRadius: 10,
    padding: 3,
    gap: 2,
  },
  opt: {
    flex: 1,
    paddingVertical: 8,
    paddingHorizontal: 12,
    borderRadius: 8,
    alignItems: 'center',
    justifyContent: 'center',
  },
  optTxt: { fontSize: 13, fontFamily: FONT_BOLD },
});
