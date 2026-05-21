

import React, { useCallback, useMemo, useState } from 'react';
import {
  GestureResponderEvent,
  LayoutChangeEvent,
  PanResponder,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import Svg, {
  G,
  Line,
  Path,
  Rect,
  Text as SvgText,
} from 'react-native-svg';
import { Candle, Ccy } from '../data/market';
import { useTokens } from '../theme/useTheme';
import { FONT_MONO } from '../theme/themes';
import { fmtNum, fmtVol } from '../format';

type Props = {
  candles: Candle[];
  height?: number;
  kind?: 'line' | 'candles';
  showVol?: boolean;
  showMA?: boolean;
  showRSI?: boolean;
  ccy: Ccy;
};

export function ProChart({
  candles: rawCandles,
  height = 240,
  kind = 'candles',
  showVol = true,
  showMA = true,
  showRSI = false,
  ccy,
}: Props) {
  const tok = useTokens();
  const [w, setW] = useState(360);
  const [hover, setHover] = useState<number | null>(null);

  const drawableCandles = useMemo(
    () =>
      rawCandles.filter(
        (c) =>
          Number.isFinite(c.t) &&
          Number.isFinite(c.o) &&
          Number.isFinite(c.h) &&
          Number.isFinite(c.l) &&
          Number.isFinite(c.c) &&
          Number.isFinite(c.v)
      ),
    [rawCandles]
  );
  const hasCandles = drawableCandles.length > 0;
  const candles = hasCandles
    ? drawableCandles
    : [{ t: 0, o: 0, h: 0, l: 0, c: 0, v: 0 }];

  const onLayout = useCallback((e: LayoutChangeEvent) => {
    setW(Math.round(e.nativeEvent.layout.width));
  }, []);

  const padR = 52;
  const padL = 8;
  const padT = 8;
  const rsiH = showRSI ? 56 : 0;
  const volH = showVol ? 44 : 0;
  const padB = 22 + rsiH + (showVol ? 4 : 0) + (showRSI ? 4 : 0);
  const priceH = Math.max(80, height - padT - padB - volH);

  const { yMin, yMax } = useMemo(() => {
    const minP = Math.min(...candles.map((c) => c.l));
    const maxP = Math.max(...candles.map((c) => c.h));
    const span = maxP - minP;
    const pad = span > 0 ? span * 0.08 : Math.max(Math.abs(maxP) * 0.01, 1);
    return { yMin: minP - pad, yMax: maxP + pad };
  }, [candles]);

  const yToPx = useCallback(
    (p: number) => padT + ((yMax - p) / (yMax - yMin)) * priceH,
    [yMax, yMin, priceH]
  );

  const innerW = Math.max(40, w - padL - padR);
  const cw = innerW / candles.length;
  const candleW = Math.max(1.2, cw * 0.7);
  const xToPx = useCallback((i: number) => padL + i * cw + cw / 2, [cw]);

  const ma = useMemo(() => {
    if (!showMA) return null;
    const out: number[] = [];
    for (let i = 0; i < candles.length; i++) {
      const lo = Math.max(0, i - 19);
      let s = 0;
      for (let k = lo; k <= i; k++) s += candles[k].c;
      out.push(s / (i - lo + 1));
    }
    return out;
  }, [candles, showMA]);

  const rsi = useMemo(() => {
    if (!showRSI) return null;
    const period = 14;
    const out: (number | null)[] = new Array(candles.length).fill(null);
    let gains = 0;
    let losses = 0;
    for (let i = 1; i < candles.length; i++) {
      const d = candles[i].c - candles[i - 1].c;
      if (i <= period) {
        if (d > 0) gains += d;
        else losses -= d;
        if (i === period) {
          const avgG = gains / period;
          const avgL = losses / period;
          out[i] = 100 - 100 / (1 + avgG / (avgL || 1e-9));
        }
      } else {
        if (d > 0) {
          gains = gains - gains / period + d;
          losses = losses - losses / period;
        } else {
          gains = gains - gains / period;
          losses = losses - losses / period - d;
        }
        out[i] = 100 - 100 / (1 + gains / (losses || 1e-9));
      }
    }
    return out;
  }, [candles, showRSI]);

  const maxV = useMemo(() => Math.max(1, ...candles.map((c) => c.v)), [candles]);
  const volTop = padT + priceH + (showVol ? 4 : 0);

  const pan = useMemo(
    () =>
      PanResponder.create({
        onStartShouldSetPanResponder: () => true,
        onMoveShouldSetPanResponder: () => true,
        onPanResponderGrant: (e: GestureResponderEvent) => {
          const x = e.nativeEvent.locationX;
          const i = Math.max(0, Math.min(candles.length - 1, Math.floor((x - padL) / cw)));
          setHover(i);
        },
        onPanResponderMove: (e) => {
          const x = e.nativeEvent.locationX;
          const i = Math.max(0, Math.min(candles.length - 1, Math.floor((x - padL) / cw)));
          setHover(i);
        },
        onPanResponderRelease: () => setHover(null),
        onPanResponderTerminate: () => setHover(null),
      }),
    [cw, candles.length]
  );

  const linePath = candles
    .map(
      (c, i) =>
        (i === 0 ? 'M' : 'L') + xToPx(i).toFixed(1) + ',' + yToPx(c.c).toFixed(1)
    )
    .join(' ');
  const areaPath =
    linePath +
    ` L${xToPx(candles.length - 1)},${padT + priceH} L${xToPx(0)},${padT + priceH} Z`;
  const maPath = ma
    ? ma
        .map((v, i) => (i === 0 ? 'M' : 'L') + xToPx(i).toFixed(1) + ',' + yToPx(v).toFixed(1))
        .join(' ')
    : null;

  const ticks = useMemo(() => {
    const n = 4;
    const arr: number[] = [];
    for (let i = 0; i <= n; i++) {
      const v = yMin + ((yMax - yMin) * (n - i)) / n;
      arr.push(v);
    }
    return arr;
  }, [yMin, yMax]);

  const rsiTop = padT + priceH + volH + (showVol ? 4 : 0) + (showRSI ? 4 : 0);
  const rsiToPx = (v: number) => rsiTop + ((100 - v) / 100) * rsiH;

  let rsiSegs = '';
  if (showRSI && rsi) {
    let started = false;
    for (let i = 0; i < rsi.length; i++) {
      const v = rsi[i];
      if (v == null) {
        started = false;
        continue;
      }
      const px = `${xToPx(i).toFixed(1)},${rsiToPx(v).toFixed(1)}`;
      rsiSegs += (started ? ' L' : ' M') + px;
      started = true;
    }
    rsiSegs = rsiSegs.trim();
  }

  const lastPrice = candles[candles.length - 1].c;
  const hovered = hover != null ? candles[hover] : candles[candles.length - 1];
  const dp = ccy === 'RUB' && Math.abs(lastPrice) > 1000 ? 1 : 2;

  if (!hasCandles) {
    return (
      <View onLayout={onLayout} style={styles.wrap}>
        <Svg width={w} height={height} />
        <View style={[styles.readout, { borderTopColor: tok.hairline }]}>
          {['O', 'H', 'L', 'C', 'V'].map((label) => (
            <React.Fragment key={label}>
              <Text style={[styles.rdLbl, { color: tok.textDim }]}>{label}</Text>
              <Text style={[styles.rdV, { color: tok.text }]}>-</Text>
            </React.Fragment>
          ))}
        </View>
      </View>
    );
  }

  return (
    <View onLayout={onLayout} style={styles.wrap}>
      <View {...pan.panHandlers}>
        <Svg width={w} height={height}>
          {}
          {ticks.map((v, i) => {
            const y = yToPx(v);
            return (
              <G key={i}>
                <Line
                  x1={padL}
                  x2={w - padR}
                  y1={y}
                  y2={y}
                  stroke={tok.grid}
                  strokeWidth={1}
                />
                <SvgText
                  x={w - padR + 6}
                  y={y + 3}
                  fontSize={9}
                  fill={tok.textDim}
                  fontFamily={FONT_MONO}
                >
                  {fmtNum(v, dp)}
                </SvgText>
              </G>
            );
          })}

          {}
          {kind === 'line' ? (
            <G>
              <Path d={areaPath} fill={tok.accent} opacity={0.12} />
              <Path
                d={linePath}
                fill="none"
                stroke={tok.accent}
                strokeWidth={1.6}
                strokeLinejoin="round"
                strokeLinecap="round"
              />
            </G>
          ) : (
            candles.map((c, i) => {
              const up = c.c >= c.o;
              const x = xToPx(i);
              const yH = yToPx(c.h);
              const yL = yToPx(c.l);
              const yO = yToPx(c.o);
              const yC = yToPx(c.c);
              const top = Math.min(yO, yC);
              const bottom = Math.max(yO, yC);
              const fill = up ? tok.up : tok.down;
              return (
                <G key={i}>
                  <Line x1={x} x2={x} y1={yH} y2={yL} stroke={fill} strokeWidth={1} />
                  <Rect
                    x={x - candleW / 2}
                    y={top}
                    width={candleW}
                    height={Math.max(0.8, bottom - top)}
                    fill={fill}
                  />
                </G>
              );
            })
          )}

          {}
          {showMA && maPath ? (
            <Path
              d={maPath}
              fill="none"
              stroke={tok.ma}
              strokeWidth={1.2}
              opacity={0.85}
            />
          ) : null}

          {}
          <Line
            x1={padL}
            x2={w - padR}
            y1={yToPx(lastPrice)}
            y2={yToPx(lastPrice)}
            stroke={tok.accent}
            strokeDasharray="2,3"
            strokeWidth={1}
            opacity={0.6}
          />
          <Rect
            x={w - padR + 2}
            y={yToPx(lastPrice) - 9}
            width={padR - 4}
            height={18}
            fill={tok.accent}
            rx={2}
          />
          <SvgText
            x={w - padR + (padR - 4) / 2 + 2}
            y={yToPx(lastPrice) + 3}
            textAnchor="middle"
            fontSize={10}
            fill={tok.accentFg}
            fontFamily={FONT_MONO}
          >
            {fmtNum(lastPrice, dp)}
          </SvgText>

          {}
          {showVol ? (
            <G>
              {candles.map((c, i) => {
                const x = xToPx(i);
                const up = c.c >= c.o;
                const hh = (c.v / maxV) * (volH - 4);
                return (
                  <Rect
                    key={i}
                    x={x - candleW / 2}
                    y={volTop + (volH - 4) - hh}
                    width={candleW}
                    height={hh}
                    fill={up ? tok.up : tok.down}
                    opacity={0.55}
                  />
                );
              })}
              <SvgText
                x={padL + 4}
                y={volTop + 11}
                fontSize={9}
                fill={tok.textDim}
                fontFamily={FONT_MONO}
              >
                VOL
              </SvgText>
            </G>
          ) : null}

          {}
          {showRSI && rsiSegs ? (
            <G>
              <Rect
                x={padL}
                y={rsiTop}
                width={innerW}
                height={rsiH}
                fill={tok.surface2}
                opacity={0.6}
              />
              <Line
                x1={padL}
                x2={w - padR}
                y1={rsiTop + rsiH * 0.3}
                y2={rsiTop + rsiH * 0.3}
                stroke={tok.grid}
              />
              <Line
                x1={padL}
                x2={w - padR}
                y1={rsiTop + rsiH * 0.7}
                y2={rsiTop + rsiH * 0.7}
                stroke={tok.grid}
              />
              <Path
                d={rsiSegs}
                fill="none"
                stroke={tok.accent}
                strokeWidth={1.2}
              />
              <SvgText
                x={padL + 4}
                y={rsiTop + 11}
                fontSize={9}
                fill={tok.textDim}
                fontFamily={FONT_MONO}
              >
                RSI 14
              </SvgText>
              <SvgText
                x={w - padR + 6}
                y={rsiTop + rsiH * 0.3 + 3}
                fontSize={9}
                fill={tok.textDim}
                fontFamily={FONT_MONO}
              >
                70
              </SvgText>
              <SvgText
                x={w - padR + 6}
                y={rsiTop + rsiH * 0.7 + 3}
                fontSize={9}
                fill={tok.textDim}
                fontFamily={FONT_MONO}
              >
                30
              </SvgText>
            </G>
          ) : null}

          {}
          {hover != null ? (
            <G>
              <Line
                x1={xToPx(hover)}
                x2={xToPx(hover)}
                y1={padT}
                y2={
                  padT +
                  priceH +
                  (showVol ? volH + 4 : 0) +
                  (showRSI ? rsiH + 4 : 0)
                }
                stroke={tok.textDim}
                strokeDasharray="2,3"
                strokeWidth={1}
              />
              <Line
                x1={padL}
                x2={w - padR}
                y1={yToPx(candles[hover].c)}
                y2={yToPx(candles[hover].c)}
                stroke={tok.textDim}
                strokeDasharray="2,3"
                strokeWidth={1}
              />
              <Rect
                x={w - padR + 2}
                y={yToPx(candles[hover].c) - 9}
                width={padR - 4}
                height={18}
                fill={tok.surface2}
                stroke={tok.accent}
                rx={2}
              />
              <SvgText
                x={w - padR + (padR - 4) / 2 + 2}
                y={yToPx(candles[hover].c) + 3}
                textAnchor="middle"
                fontSize={10}
                fill={tok.text}
                fontFamily={FONT_MONO}
              >
                {fmtNum(candles[hover].c, dp)}
              </SvgText>
            </G>
          ) : null}
        </Svg>
      </View>

      {}
      <View style={[styles.readout, { borderTopColor: tok.hairline }]}>
        <Text style={[styles.rdLbl, { color: tok.textDim }]}>O</Text>
        <Text style={[styles.rdV, { color: tok.text }]}>{fmtNum(hovered.o, dp)}</Text>
        <Text style={[styles.rdLbl, { color: tok.textDim }]}>H</Text>
        <Text style={[styles.rdV, { color: tok.text }]}>{fmtNum(hovered.h, dp)}</Text>
        <Text style={[styles.rdLbl, { color: tok.textDim }]}>L</Text>
        <Text style={[styles.rdV, { color: tok.text }]}>{fmtNum(hovered.l, dp)}</Text>
        <Text style={[styles.rdLbl, { color: tok.textDim }]}>C</Text>
        <Text
          style={[
            styles.rdV,
            { color: hovered.c >= hovered.o ? tok.up : tok.down },
          ]}
        >
          {fmtNum(hovered.c, dp)}
        </Text>
        <Text style={[styles.rdLbl, { color: tok.textDim }]}>V</Text>
        <Text style={[styles.rdV, { color: tok.text }]}>{fmtVol(hovered.v)}</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: { overflow: 'hidden' },
  readout: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    alignItems: 'baseline',
    paddingHorizontal: 14,
    paddingTop: 8,
    paddingBottom: 10,
    borderTopWidth: 1,
    marginTop: 4,
    columnGap: 10,
    rowGap: 4,
  },
  rdLbl: { fontFamily: FONT_MONO, fontSize: 11, fontWeight: '700', marginRight: 3 },
  rdV: { fontFamily: FONT_MONO, fontSize: 11, fontWeight: '600' },
});
