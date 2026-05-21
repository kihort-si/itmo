

export type ThemeName = 'dark' | 'light';
export type VariantName = 'calm' | 'bold';
export type Density = 'regular' | 'compact';

export type Tokens = {
  bg: string;
  surface: string;
  surface2: string;
  surface3: string;
  hairline: string;
  hairline2: string;
  text: string;
  textMute: string;
  textDim: string;
  textInv: string;
  accent: string;
  accent2: string;
  accentFg: string;
  accentSoft: string;
  up: string;
  upSoft: string;
  down: string;
  downSoft: string;
  ma: string;
  grid: string;
  statusFg: string;

  rowPadY: number;
  rowPadX: number;
  rowMinH: number;
};

const darkCalm: Tokens = {
  bg: '#07090C',
  surface: '#0E1217',
  surface2: '#151A21',
  surface3: '#1B2129',
  hairline: '#1F262F',
  hairline2: '#2A323D',
  text: '#E7ECF1',
  textMute: '#8B96A4',
  textDim: '#5A6371',
  textInv: '#07090C',
  accent: '#00B07A',
  accent2: '#00875D',
  accentFg: '#00170F',
  accentSoft: 'rgba(0, 176, 122, 0.12)',
  up: '#2ED1A5',
  upSoft: 'rgba(46, 209, 165, 0.14)',
  down: '#F0668F',
  downSoft: 'rgba(240, 102, 143, 0.14)',
  ma: '#C7A14E',
  grid: 'rgba(255,255,255,0.05)',
  statusFg: '#E7ECF1',
  rowPadY: 12,
  rowPadX: 16,
  rowMinH: 60,
};

const lightCalm: Tokens = {
  bg: '#F5F6F7',
  surface: '#FFFFFF',
  surface2: '#F2F4F6',
  surface3: '#E9ECEF',
  hairline: '#E2E5E9',
  hairline2: '#CDD2D8',
  text: '#0E1217',
  textMute: '#5A6371',
  textDim: '#8B96A4',
  textInv: '#FFFFFF',
  accent: '#008A60',
  accent2: '#006A4A',
  accentFg: '#FFFFFF',
  accentSoft: 'rgba(0, 138, 96, 0.10)',
  up: '#0E9D72',
  upSoft: 'rgba(14, 157, 114, 0.12)',
  down: '#D14B70',
  downSoft: 'rgba(209, 75, 112, 0.12)',
  ma: '#B9802B',
  grid: 'rgba(0,0,0,0.06)',
  statusFg: '#0E1217',
  rowPadY: 12,
  rowPadX: 16,
  rowMinH: 60,
};

const darkBold: Tokens = {
  ...darkCalm,
  bg: '#04080F',
  surface: '#0B121C',
  surface2: '#131C2A',
  surface3: '#1C2638',
  hairline: '#1B2535',
  hairline2: '#2D3B53',
  accent: '#00E0A8',
  accent2: '#00B388',
  accentFg: '#001A12',
  accentSoft: 'rgba(0, 224, 168, 0.16)',
  up: '#38E5B5',
  upSoft: 'rgba(56, 229, 181, 0.18)',
  down: '#FF6F94',
  downSoft: 'rgba(255, 111, 148, 0.18)',
};

const lightBold: Tokens = {
  ...lightCalm,
  accent: '#00B388',
  accent2: '#008766',
  accentSoft: 'rgba(0, 179, 136, 0.14)',
};

export function getTokens(theme: ThemeName, variant: VariantName, density: Density): Tokens {
  let base: Tokens;
  if (theme === 'dark' && variant === 'calm') base = darkCalm;
  else if (theme === 'dark' && variant === 'bold') base = darkBold;
  else if (theme === 'light' && variant === 'calm') base = lightCalm;
  else base = lightBold;

  const dens =
    density === 'compact'
      ? { rowPadY: 8, rowPadX: 14, rowMinH: 48 }
      : { rowPadY: 12, rowPadX: 16, rowMinH: 60 };
  return { ...base, ...dens };
}

export const FONT_REGULAR = 'DMSans';
export const FONT_BOLD = 'DMSans-Bold';
export const FONT_MONO = 'JetBrainsMono';
