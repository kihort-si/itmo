import React from 'react';
import Svg, { Path } from 'react-native-svg';

export function Sparkline({
  data,
  w = 64,
  h = 22,
  color,
  fill = false,
}: {
  data: number[];
  w?: number;
  h?: number;
  color: string;
  fill?: boolean;
}) {
  const points = data?.filter((value) => Number.isFinite(value)) ?? [];
  if (points.length < 2) {
    return <Svg width={w} height={h} />;
  }
  const min = Math.min(...points);
  const max = Math.max(...points);
  const span = max - min || 1;
  const step = w / (points.length - 1);
  const pts: [number, number][] = points.map((v, i) => [
    i * step,
    h - ((v - min) / span) * (h - 2) - 1,
  ]);
  const path = pts
    .map((p, i) => (i === 0 ? 'M' : 'L') + p[0].toFixed(1) + ',' + p[1].toFixed(1))
    .join(' ');
  const areaPath = path + ` L${w},${h} L0,${h} Z`;
  return (
    <Svg width={w} height={h} viewBox={`0 0 ${w} ${h}`}>
      {fill ? <Path d={areaPath} fill={color} opacity={0.12} /> : null}
      <Path
        d={path}
        fill="none"
        stroke={color}
        strokeWidth={1.4}
        strokeLinejoin="round"
        strokeLinecap="round"
      />
    </Svg>
  );
}
