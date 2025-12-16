interface GaugeProps {
  value: number;
  min?: number;
  max?: number;
  width?: number;
  thickness?: number;
  bgColor?: string;
  fillFrom?: string;
  fillTo?: string;
  outlineColor?: string;
}

const polar = (cx: number, cy: number, r: number, angleDeg: number) => {
  const a: number = (angleDeg - 90) * (Math.PI / 180);
  return {x: cx + r * Math.cos(a), y: cy + r * Math.sin(a)};
};

const arcPath = (
  cx: number, cy: number,
  rOuter: number, rInner: number,
  start: number, end: number
): string => {
  const largeArc = end - start > 180 ? 1 : 0;

  const p1 = polar(cx, cy, rOuter, start);
  const p2 = polar(cx, cy, rOuter, end);
  const p3 = polar(cx, cy, rInner, end);
  const p4 = polar(cx, cy, rInner, start);

  return [
    `M ${p1.x} ${p1.y}`,
    `A ${rOuter} ${rOuter} 0 ${largeArc} 1 ${p2.x} ${p2.y}`,
    `L ${p3.x} ${p3.y}`,
    `A ${rInner} ${rInner} 0 ${largeArc} 0 ${p4.x} ${p4.y}`,
    "Z",
  ].join(" ");
};

function Gauge({
                        value, min = 0, max = 100, width = 520,
                        thickness = 100,
                        bgColor = "#19233A",
                        fillFrom = "#e05764",
                        fillTo = "#d04f59",
                        outlineColor = "#e0576480"
                      }: GaugeProps) {
  const clamped: number = Math.max(min, Math.min(max, value));
  const ratio: number = (clamped - min) / (max - min || 1);

  const W: number = width;
  const H: number = Math.round(W / 2.0);
  const cx: number = W / 2;
  const cy: number = H;
  const rOuter: number = Math.min(W, H) - 10;
  const rInner: number = rOuter - thickness;

  const startAngle = 270;
  const endAngleFull = 450;
  const endAngleValue: number = startAngle + 180 * ratio;

  const trackPath: string = arcPath(cx, cy, rOuter, rInner, startAngle, endAngleFull);
  const valuePath: string = arcPath(cx, cy, rOuter, rInner, startAngle, endAngleValue);

  const percent: number = Math.round(ratio * 100);

  return (
    <svg
      viewBox={`0 0 ${W} ${H}`}
      width={W}
      height={H}
      role="img"
      aria-label={`Gauge ${percent}%`}
      style={{display: "block", background: bgColor, borderRadius: 12}}
    >
      <defs>
        <linearGradient id="gaugeGradient" x1="0%" y1="0%" x2="100%" y2="0%">
          <stop offset="0%" stopColor={fillFrom}/>
          <stop offset="100%" stopColor={fillTo}/>
        </linearGradient>
        <mask id="cutout">
          <rect x="0" y="0" width={W} height={H} fill="white"/>
          <path d={valuePath} fill="black"/>
        </mask>
      </defs>

      <path d={trackPath} fill="none" stroke={outlineColor} strokeWidth={1.5}/>

      <path d={valuePath} fill="url(#gaugeGradient)"/>

      <path d={trackPath} fill="none" stroke={outlineColor} strokeWidth={1.5} mask="url(#cutout)"/>

      <text
        x={cx}
        y={cy - thickness / 3}
        textAnchor="middle"
        dominantBaseline="middle"
        fontSize={32}
        fontWeight={700}
        fill="#fff"
      >
        {percent}%
      </text>
    </svg>
  );
}

export default Gauge;