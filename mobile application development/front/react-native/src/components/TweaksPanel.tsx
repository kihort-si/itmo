

import React, { useState } from 'react';
import {
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { useTheme } from '../theme/useTheme';
import { useAuthStore } from '../stores/authStore';
import { useDevStore } from '../stores/devStore';
import { BottomSheet } from './BottomSheet';
import { IconSliders } from '../icons/Icons';
import { FONT_BOLD } from '../theme/themes';
import { Switch } from './Switch';

function RadioGroup({
  label,
  value,
  options,
  onChange,
}: {
  label: string;
  value: string;
  options: ReadonlyArray<{ value: string; label: string }>;
  onChange: (v: string) => void;
}) {
  const { tokens: t } = useTheme();
  return (
    <View style={styles.group}>
      <Text style={[styles.groupLbl, { color: t.textMute }]}>{label}</Text>
      <View style={[styles.opts, { backgroundColor: t.surface, borderColor: t.hairline }]}>
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
    </View>
  );
}

export function TweaksPanel() {
  const { tweaks, setTweak, tokens: t } = useTheme();
  const [open, setOpen] = useState(false);
  const authed = useAuthStore((s) => !!s.jwt);
  const mockMarketData = useDevStore((s) => s.mockMarketData);
  const setMockMarketData = useDevStore((s) => s.setMockMarketData);

  return (
    <>
      <Pressable
        onPress={() => setOpen(true)}
        style={[
          styles.fab,
          { backgroundColor: t.surface, borderColor: t.hairline2 },
        ]}
        hitSlop={6}
      >
        <IconSliders size={20} color={t.text} />
      </Pressable>

      <BottomSheet open={open} onClose={() => setOpen(false)} title="Tweaks">
        <View style={[styles.section, { borderColor: t.hairline }]}>
          <Text style={[styles.sectionTitle, { color: t.textMute }]}>Variant</Text>
          <RadioGroup
            label="Aesthetic"
            value={tweaks.variant}
            options={[
              { value: 'calm', label: 'Calm' },
              { value: 'bold', label: 'Bold' },
            ]}
            onChange={(v) => setTweak('variant', v as 'calm' | 'bold')}
          />
          <RadioGroup
            label="Theme"
            value={tweaks.theme}
            options={[
              { value: 'dark', label: 'Dark' },
              { value: 'light', label: 'Light' },
            ]}
            onChange={(v) => setTweak('theme', v as 'dark' | 'light')}
          />
        </View>

        <View style={[styles.section, { borderColor: t.hairline }]}>
          <Text style={[styles.sectionTitle, { color: t.textMute }]}>Display</Text>
          <RadioGroup
            label="Density"
            value={tweaks.density}
            options={[
              { value: 'regular', label: 'Regular' },
              { value: 'compact', label: 'Compact' },
            ]}
            onChange={(v) => setTweak('density', v as 'regular' | 'compact')}
          />
          <RadioGroup
            label="Default chart"
            value={tweaks.defaultChartKind}
            options={[
              { value: 'line', label: 'Line' },
              { value: 'candles', label: 'Candles' },
            ]}
            onChange={(v) => setTweak('defaultChartKind', v as 'line' | 'candles')}
          />
          <RadioGroup
            label="Language"
            value={tweaks.lang}
            options={[
              { value: 'ru', label: 'Русский' },
              { value: 'en', label: 'English' },
            ]}
            onChange={(v) => setTweak('lang', v as 'ru' | 'en')}
          />
        </View>

        <View style={[styles.section, { borderColor: t.hairline }]}>
          <Text style={[styles.sectionTitle, { color: t.textMute }]}>Account</Text>
          <RadioGroup
            label="Auth state"
            value={authed ? 'in' : 'out'}
            options={[
              { value: 'out', label: 'Signed out' },
              { value: 'in', label: 'Signed in' },
            ]}
            onChange={(v) => {
              if (v === 'in') {
                void useAuthStore.getState().login({ email: 'dev@itmo.ru', password: 'devpass' });
              } else {
                void useAuthStore.getState().logout();
              }
            }}
          />
        </View>

        <View style={[styles.section, { borderColor: t.hairline }]}>
          <Text style={[styles.sectionTitle, { color: t.textMute }]}>Market data</Text>
          <View
            style={[
              styles.toggleRow,
              { backgroundColor: t.surface, borderColor: t.hairline },
            ]}
          >
            <View style={{ flex: 1 }}>
              <Text style={[styles.toggleTitle, { color: t.text }]}>Use local mock charts</Text>
              <Text style={[styles.toggleSub, { color: t.textMute }]}>
                Off: BFF/core-mds. On: local fallback data.
              </Text>
            </View>
            <Switch value={mockMarketData} onChange={setMockMarketData} />
          </View>
        </View>
      </BottomSheet>
    </>
  );
}

const styles = StyleSheet.create({
  fab: {
    position: 'absolute',
    top: 56,
    right: 12,
    width: 40,
    height: 40,
    borderRadius: 20,
    borderWidth: 1,
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 99,
  },
  section: { marginBottom: 18 },
  sectionTitle: {
    fontFamily: FONT_BOLD,
    fontSize: 11,
    letterSpacing: 0.6,
    textTransform: 'uppercase',
    marginBottom: 10,
  },
  group: { marginBottom: 14 },
  groupLbl: { fontSize: 12, marginBottom: 6 },
  opts: { flexDirection: 'row', borderWidth: 1, borderRadius: 10, padding: 3, gap: 2 },
  toggleRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    borderWidth: 1,
    borderRadius: 12,
    paddingVertical: 12,
    paddingHorizontal: 14,
  },
  toggleTitle: { fontFamily: FONT_BOLD, fontSize: 13 },
  toggleSub: { fontSize: 12, marginTop: 3 },
  opt: {
    flex: 1,
    paddingVertical: 8,
    paddingHorizontal: 10,
    borderRadius: 8,
    alignItems: 'center',
  },
  optTxt: { fontFamily: FONT_BOLD, fontSize: 12 },
});
