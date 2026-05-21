

import React from 'react';
import Svg, { Path, Circle, Rect, G } from 'react-native-svg';

export type IconProps = {
  size?: number;
  color?: string;
  fill?: string;
  strokeWidth?: number;
};

type Maker = (children: React.ReactNode, opts?: { defaultFill?: string }) => React.FC<IconProps>;

const make: Maker = (children, opts) => ({
  size = 22,
  color = '#E7ECF1',
  fill = opts?.defaultFill ?? 'none',
  strokeWidth = 1.7,
}) => (
  <Svg
    width={size}
    height={size}
    viewBox="0 0 24 24"
    fill={fill}
    stroke={color}
    strokeWidth={strokeWidth}
    strokeLinecap="round"
    strokeLinejoin="round"
  >
    {children}
  </Svg>
);

export const IconSearch = make(
  <G><Circle cx="11" cy="11" r="7" /><Path d="M20 20 l -3.5 -3.5" /></G>
);

export const IconFilter = make(
  <G><Path d="M3 5h18" /><Path d="M6 12h12" /><Path d="M10 19h4" /></G>
);

export const IconSort = make(
  <G><Path d="M7 4v16" /><Path d="m4 8 3-4 3 4" /><Path d="M17 4v16" /><Path d="m14 16 3 4 3-4" /></G>
);

export const IconBack = make(<Path d="M15 6 9 12l6 6" />);

export const IconClose = make(
  <G><Path d="M6 6l12 12" /><Path d="M18 6 6 18" /></G>
);

export const IconStar = make(<Path d="m12 3 2.7 5.7 6.3.9-4.6 4.4 1.1 6.3L12 17.6 6.5 20.3l1.1-6.3L3 9.6l6.3-.9z" />);

export const IconStarSolid: React.FC<IconProps> = ({ size = 22, color = '#E7ECF1' }) => (
  <Svg width={size} height={size} viewBox="0 0 24 24" fill={color}>
    <Path d="m12 3 2.7 5.7 6.3.9-4.6 4.4 1.1 6.3L12 17.6 6.5 20.3l1.1-6.3L3 9.6l6.3-.9z" />
  </Svg>
);

export const IconBell = make(
  <G>
    <Path d="M6 16V11a6 6 0 0 1 12 0v5l1.5 2H4.5z" />
    <Path d="M10 20a2 2 0 0 0 4 0" />
  </G>
);

export const IconMenu = make(
  <G><Path d="M4 7h16" /><Path d="M4 12h16" /><Path d="M4 17h16" /></G>
);

export const IconDots: React.FC<IconProps> = ({ size = 22, color = '#E7ECF1' }) => (
  <Svg width={size} height={size} viewBox="0 0 24 24">
    <Circle cx="6" cy="12" r="1.4" fill={color} />
    <Circle cx="12" cy="12" r="1.4" fill={color} />
    <Circle cx="18" cy="12" r="1.4" fill={color} />
  </Svg>
);

export const IconChartCandles = make(
  <G>
    <Path d="M7 4v3" />
    <Path d="M7 17v3" />
    <Rect x="5" y="7" width="4" height="10" />
    <Path d="M17 6v3" />
    <Path d="M17 14v4" />
    <Rect x="15" y="9" width="4" height="5" />
  </G>
);

export const IconChartLine = make(
  <G><Path d="M3 17 9 11l4 4 8-9" /><Path d="M14 3h7v7" /></G>
);

export const IconIndicators = make(
  <G><Path d="M4 19h16" /><Path d="M4 15h4" /><Path d="M11 11h4" /><Path d="M18 6h2" /></G>
);

export const IconAdd = make(
  <G><Path d="M12 5v14" /><Path d="M5 12h14" /></G>
);

export const IconMinus = make(<Path d="M5 12h14" />);

export const IconUp = make(<Path d="m6 14 6-6 6 6" />);
export const IconDown = make(<Path d="m6 10 6 6 6-6" />);
export const IconRight = make(<Path d="M9 6 15 12l-6 6" />);

export const IconLock = make(
  <G>
    <Rect x="5" y="11" width="14" height="10" rx="2" />
    <Path d="M8 11V8a4 4 0 0 1 8 0v3" />
  </G>
);

export const IconUser = make(
  <G><Circle cx="12" cy="8" r="4" /><Path d="M4 21a8 8 0 0 1 16 0" /></G>
);

export const IconSignal = make(
  <G>
    <Path d="M3 18h2v-3H3z" />
    <Path d="M8 18h2V11H8z" />
    <Path d="M13 18h2V8h-2z" />
    <Path d="M18 18h2V4h-2z" />
  </G>
);

export const IconExchange = make(
  <G>
    <Path d="M3 7h14" /><Path d="m13 3 4 4-4 4" />
    <Path d="M21 17H7" /><Path d="m11 13-4 4 4 4" />
  </G>
);

export const IconBriefcase = make(
  <G>
    <Rect x="3" y="7" width="18" height="13" rx="2" />
    <Path d="M9 7V5a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v2" />
    <Path d="M3 13h18" />
  </G>
);

export const IconHistory = make(
  <G>
    <Path d="M3 12a9 9 0 1 0 3-6.7" />
    <Path d="M3 4v5h5" />
    <Path d="M12 8v4l3 2" />
  </G>
);

export const IconCheck = make(<Path d="m5 12 4 4 10-10" />);

export const IconShield = make(<Path d="M12 3 4 6v6c0 5 3.5 8 8 9 4.5-1 8-4 8-9V6z" />);

export const IconLogout = make(
  <G>
    <Path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
    <Path d="m17 16 5-4-5-4" />
    <Path d="M22 12H10" />
  </G>
);

export const IconEye = make(
  <G>
    <Path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12z" />
    <Circle cx="12" cy="12" r="3" />
  </G>
);

export const IconDelete = make(
  <G>
    <Path d="M4 7h16" /><Path d="M10 11v6" /><Path d="M14 11v6" />
    <Path d="M6 7l1 13a2 2 0 0 0 2 2h6a2 2 0 0 0 2-2l1-13" />
    <Path d="M9 7V4h6v3" />
  </G>
);

export const IconSwap = make(
  <G>
    <Path d="M7 7h14" /><Path d="m17 3 4 4-4 4" />
    <Path d="M17 17H3" /><Path d="m7 13-4 4 4 4" />
  </G>
);

export const IconSliders = make(
  <G>
    <Path d="M4 6h10" /><Path d="M18 6h2" />
    <Circle cx="16" cy="6" r="2" />
    <Path d="M4 12h4" /><Path d="M12 12h8" />
    <Circle cx="10" cy="12" r="2" />
    <Path d="M4 18h14" /><Path d="M22 18h-2" />
    <Circle cx="20" cy="18" r="2" />
  </G>
);
