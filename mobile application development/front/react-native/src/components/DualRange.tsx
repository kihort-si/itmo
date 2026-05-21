import React, { useCallback, useRef, useState } from 'react';
import {
  LayoutChangeEvent,
  PanResponder,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { useTokens } from '../theme/useTheme';
import { FONT_MONO } from '../theme/themes';

type Props = {
  min: number;
  max: number;
  step: number;
  value: [number, number];
  onChange: (v: [number, number]) => void;
  fmt: (v: number) => string;
};

export function DualRange({ min, max, step, value, onChange, fmt }: Props) {
  const t = useTokens();
  const [w, setW] = useState(280);
  const [lo, hi] = value;
  const span = max - min;
  const pctLo = ((lo - min) / span) * 100;
  const pctHi = ((hi - min) / span) * 100;

  const valRef = useRef(value);
  valRef.current = value;

  const onLayout = useCallback((e: LayoutChangeEvent) => {
    setW(e.nativeEvent.layout.width);
  }, []);

  const snap = useCallback(
    (v: number) => {
      const clamped = Math.max(min, Math.min(max, v));
      return Math.round(clamped / step) * step;
    },
    [min, max, step]
  );

  const makePan = (which: 'lo' | 'hi') =>
    PanResponder.create({
      onStartShouldSetPanResponder: () => true,
      onMoveShouldSetPanResponder: () => true,
      onPanResponderGrant: (e) => {
        const x = e.nativeEvent.locationX;
        const ratio = Math.max(0, Math.min(1, x / w));
        const v = snap(min + ratio * span);
        const [l, h] = valRef.current;
        if (which === 'lo') onChange([Math.min(v, h - step), h]);
        else onChange([l, Math.max(v, l + step)]);
      },
      onPanResponderMove: (e) => {
        const x = e.nativeEvent.locationX;
        const ratio = Math.max(0, Math.min(1, x / w));
        const v = snap(min + ratio * span);
        const [l, h] = valRef.current;
        if (which === 'lo') onChange([Math.min(v, h - step), h]);
        else onChange([l, Math.max(v, l + step)]);
      },
    });

  const panLo = useRef(makePan('lo')).current;
  const panHi = useRef(makePan('hi')).current;

  const knobLo = (pctLo / 100) * w - 11;
  const knobHi = (pctHi / 100) * w - 11;

  return (
    <View style={styles.wrap}>
      <View style={styles.trackRow} onLayout={onLayout}>
        <View style={[styles.track, { backgroundColor: t.hairline }]} />
        <View
          style={[
            styles.fill,
            {
              left: `${pctLo}%`,
              right: `${100 - pctHi}%`,
              backgroundColor: t.accent,
            },
          ]}
        />
        <View
          {...panLo.panHandlers}
          style={[
            styles.knob,
            {
              left: knobLo,
              backgroundColor: t.text,
              borderColor: t.accent,
            },
          ]}
        />
        <View
          {...panHi.panHandlers}
          style={[
            styles.knob,
            {
              left: knobHi,
              backgroundColor: t.text,
              borderColor: t.accent,
            },
          ]}
        />
      </View>
      <View style={styles.vals}>
        <Text style={[styles.valTxt, { color: t.text }]}>{fmt(lo)}</Text>
        <Text style={[styles.valTxt, { color: t.text }]}>{fmt(hi)}</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: { paddingVertical: 8, paddingHorizontal: 4 },
  trackRow: { height: 32, justifyContent: 'center', position: 'relative' },
  track: { position: 'absolute', left: 0, right: 0, top: 14, height: 4, borderRadius: 2 },
  fill: { position: 'absolute', top: 14, height: 4, borderRadius: 2 },
  knob: {
    position: 'absolute',
    top: 5,
    width: 22,
    height: 22,
    borderRadius: 11,
    borderWidth: 3,
  },
  vals: { flexDirection: 'row', justifyContent: 'space-between', marginTop: 8 },
  valTxt: { fontFamily: FONT_MONO, fontSize: 13 },
});
