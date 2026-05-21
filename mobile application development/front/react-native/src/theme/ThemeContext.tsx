import React, { createContext, useContext, useMemo, useState, ReactNode } from 'react';
import { getTokens, ThemeName, VariantName, Density, Tokens } from './themes';

export type Tweaks = {
  theme: ThemeName;
  variant: VariantName;
  density: Density;
  defaultChartKind: 'line' | 'candles';
  lang: 'ru' | 'en';
};

const TWEAK_DEFAULTS: Tweaks = {
  theme: 'dark',
  variant: 'calm',
  density: 'regular',
  defaultChartKind: 'candles',
  lang: 'ru',
};

type Ctx = {
  tweaks: Tweaks;
  setTweak: <K extends keyof Tweaks>(k: K, v: Tweaks[K]) => void;
  tokens: Tokens;
};

const ThemeCtx = createContext<Ctx | null>(null);

export function ThemeProvider({ children }: { children: ReactNode }) {
  const [tweaks, setTweaks] = useState<Tweaks>(TWEAK_DEFAULTS);

  const setTweak = <K extends keyof Tweaks>(k: K, v: Tweaks[K]) =>
    setTweaks((p) => ({ ...p, [k]: v }));

  const tokens = useMemo(
    () => getTokens(tweaks.theme, tweaks.variant, tweaks.density),
    [tweaks.theme, tweaks.variant, tweaks.density]
  );

  const value = useMemo(() => ({ tweaks, setTweak, tokens }), [tweaks, tokens]);
  return <ThemeCtx.Provider value={value}>{children}</ThemeCtx.Provider>;
}

export function useTheme(): Ctx {
  const v = useContext(ThemeCtx);
  if (!v) throw new Error('useTheme must be used within ThemeProvider');
  return v;
}

export function useTokens(): Tokens {
  return useTheme().tokens;
}
