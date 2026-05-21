import React, { useEffect, useMemo, useState } from 'react';
import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { useTheme } from '../theme/useTheme';
import { useI18n } from '../i18n/I18nContext';
import { brokerData } from '../data/repository';
import type { Candle } from '../data/market';
import { useQuoteSubscription } from '../hooks/useQuoteSubscription';
import { fmtNum, fmtPct, fmtSigned, fmtVol } from '../format';
import { TopBar } from '../components/TopBar';
import { IconButton } from '../components/IconButton';
import { ProChart } from '../components/ProChart';
import {
  IconBack,
  IconChartCandles,
  IconChartLine,
  IconDots,
  IconStar,
  IconStarSolid,
} from '../icons/Icons';
import { FONT_BOLD, FONT_MONO, FONT_REGULAR } from '../theme/themes';
import { getChartCandles } from '../services/marketService';
import { useDevStore } from '../stores/devStore';

type Period = '10min' | '1h' | '6h' | '1d' | '10d' | '1m';
const MARKET_REFRESH_MS = 5000;

export function StockScreen({
  sym,
  onBack,
  onTrade,
  favorites,
  toggleFavorite,
}: {
  sym: string;
  onBack: () => void;
  onTrade: (sym: string, side: 'buy' | 'sell') => void;
  favorites: Record<string, boolean>;
  toggleFavorite: (sym: string) => void;
}) {
  const { tokens: t, tweaks } = useTheme();
  const { T, lang } = useI18n();
  const ticker = brokerData.findTicker(sym);
  const liveQuote = useQuoteSubscription(sym);
  const [period, setPeriod] = useState<Period>('1m');
  const [kind, setKind] = useState<'line' | 'candles'>(tweaks.defaultChartKind);
  const [showVol, setShowVol] = useState(true);
  const [showMA, setShowMA] = useState(true);
  const [showRSI, setShowRSI] = useState(false);
  const [candles, setCandles] = useState<Candle[]>([]);
  const mockMarketData = useDevStore((s) => s.mockMarketData);

  useEffect(() => {
    let cancelled = false;
    let inFlight = false;
    const refresh = () => {
      if (inFlight) return;
      inFlight = true;
      void getChartCandles(ticker, period, kind).then((result) => {
        if (!cancelled) {
          setCandles(result.candles);
        }
      }).finally(() => {
        inFlight = false;
      });
    };
    refresh();
    const timer = setInterval(refresh, MARKET_REFRESH_MS);
    return () => {
      cancelled = true;
      clearInterval(timer);
    };
  }, [ticker, period, kind, mockMarketData]);

  const currentCandles = candles;
  const neutralCandle: Candle = {
    t: 0,
    o: ticker.base,
    h: ticker.base,
    l: ticker.base,
    c: ticker.base,
    v: 0,
  };

  const last = currentCandles[currentCandles.length - 1] ?? neutralCandle;
  const first = currentCandles[0] ?? last;
  const change = last.c - first.c;
  const changePct = first.c === 0 ? 0 : (change / first.c) * 100;
  const statCandle = currentCandles[currentCandles.length - 1] ?? neutralCandle;
  const prevStatCandle = currentCandles[currentCandles.length - 2] ?? statCandle;
  const heroPrice = mockMarketData ? liveQuote?.price ?? last.c : last.c;
  const heroChangePct = mockMarketData ? liveQuote?.changePct ?? changePct : changePct;
  const up = heroChangePct >= 0;
  const dpHero = heroPrice > 1000 ? 1 : 2;
  const dpStat = ticker.base > 1000 ? 1 : 2;

  const periodLabel: Record<Period, string> = {
    '10min': '10М',
    '1h': '1Ч',
    '6h': '6Ч',
    '1d': T.period1d,
    '10d': '10Д',
    '1m': T.period1m,
  };

  const stats: { label: string; value: string; color?: string }[] = [
    { label: T.open, value: fmtNum(statCandle.o, dpStat) },
    { label: T.high, value: fmtNum(statCandle.h, dpStat) },
    { label: T.low, value: fmtNum(statCandle.l, dpStat) },
    { label: T.prevClose, value: fmtNum(prevStatCandle.c, dpStat) },
    { label: T.volume, value: fmtVol(statCandle.v) },
    { label: T.bid, value: fmtNum(last.c - (last.c > 100 ? 0.05 : 0.01), 2), color: t.down },
    { label: T.ask, value: fmtNum(last.c + (last.c > 100 ? 0.05 : 0.01), 2), color: t.up },
    { label: T.spread, value: fmtNum(last.c > 100 ? 0.1 : 0.02, 2) },
  ];

  return (
    <View style={{ flex: 1, backgroundColor: t.bg }}>
      <ScrollView contentContainerStyle={{ paddingBottom: 110 }}>
        <TopBar
          left={<IconButton icon={<IconBack size={22} color={t.text} />} onPress={onBack} />}
          title={ticker.sym}
          subtitle={lang === 'ru' ? ticker.nameRu : ticker.nameEn}
          right={
            <View style={{ flexDirection: 'row', gap: 4 }}>
              <IconButton
                icon={
                  favorites[sym] ? (
                    <IconStarSolid size={20} color={t.accent} />
                  ) : (
                    <IconStar size={20} color={t.text} />
                  )
                }
                onPress={() => toggleFavorite(sym)}
              />
              <IconButton icon={<IconDots size={22} color={t.text} />} />
            </View>
          }
        />

        <View style={styles.hero}>
          <View style={styles.heroRow}>
            <Text style={[styles.heroPrice, { color: t.text }]}>
              {fmtNum(heroPrice, dpHero)}
            </Text>
            <Text style={[styles.heroCcy, { color: t.textMute }]}>
              {ticker.ccy === 'RUB' ? '₽' : '$'}
            </Text>
          </View>
          <View style={styles.heroChg}>
            <Text style={{ color: up ? t.up : t.down, fontSize: 11 }}>{up ? '▲' : '▼'}</Text>
            <Text style={[styles.heroChgTxt, { color: up ? t.up : t.down }]}>
              {fmtSigned(mockMarketData ? liveQuote?.change ?? change : change, 2)}
            </Text>
            <Text style={[styles.heroChgTxt, { color: up ? t.up : t.down }]}>
              {fmtPct(heroChangePct)}
            </Text>
            <View style={[styles.heroTag, { backgroundColor: t.surface2 }]}>
              <Text style={[styles.heroTagTxt, { color: t.textMute }]}>{periodLabel[period]}</Text>
            </View>
          </View>
        </View>

        {}
        <View
          style={[
            styles.chartWrap,
            { backgroundColor: t.surface, borderColor: t.hairline },
          ]}
        >
          <ProChart
            candles={currentCandles}
            kind={kind}
            showVol={showVol}
            showMA={showMA}
            showRSI={showRSI}
            ccy={ticker.ccy}
          />
        </View>

        {}
        <View
          style={[styles.periodRow, { backgroundColor: t.surface, borderColor: t.hairline }]}
        >
          {(['10min', '1h', '6h', '1d', '10d', '1m'] as Period[]).map((p) => {
            const on = period === p;
            return (
              <Pressable
                key={p}
                onPress={() => setPeriod(p)}
                style={[
                  styles.periodBtn,
                  on && { backgroundColor: t.text },
                ]}
              >
                <Text
                  style={[
                    styles.periodTxt,
                    { color: on ? t.textInv : t.textMute },
                  ]}
                >
                  {periodLabel[p]}
                </Text>
              </Pressable>
            );
          })}
        </View>

        {}
        <View style={styles.tools}>
          <View
            style={[styles.kindToggle, { backgroundColor: t.surface, borderColor: t.hairline }]}
          >
            <Pressable
              onPress={() => setKind('line')}
              style={[
                styles.kindBtn,
                kind === 'line' && { backgroundColor: t.surface3 },
              ]}
            >
              <IconChartLine size={14} color={kind === 'line' ? t.text : t.textMute} />
              <Text
                style={[
                  styles.kindTxt,
                  { color: kind === 'line' ? t.text : t.textMute },
                ]}
              >
                {T.chartLine}
              </Text>
            </Pressable>
            <Pressable
              onPress={() => setKind('candles')}
              style={[
                styles.kindBtn,
                kind === 'candles' && { backgroundColor: t.surface3 },
              ]}
            >
              <IconChartCandles size={14} color={kind === 'candles' ? t.text : t.textMute} />
              <Text
                style={[
                  styles.kindTxt,
                  { color: kind === 'candles' ? t.text : t.textMute },
                ]}
              >
                {T.chartCandles}
              </Text>
            </Pressable>
          </View>
          <View style={{ flex: 1 }} />
          <StudyChip label={`${T.ma}20`} on={showMA} onPress={() => setShowMA((v) => !v)} />
          <StudyChip label={T.vol} on={showVol} onPress={() => setShowVol((v) => !v)} />
          <StudyChip label={T.rsi} on={showRSI} onPress={() => setShowRSI((v) => !v)} />
        </View>

        {}
        <View
          style={[
            styles.stats,
            { backgroundColor: t.surface, borderColor: t.hairline },
          ]}
        >
          {stats.map((s, i) => (
            <View
              key={s.label + i}
              style={[
                styles.stat,
                {
                  borderRightWidth: (i % 4) === 3 ? 0 : 1,
                  borderBottomWidth: i >= 4 ? 0 : 1,
                  borderColor: t.hairline,
                },
              ]}
            >
              <Text style={[styles.statLbl, { color: t.textDim }]}>{s.label}</Text>
              <Text style={[styles.statV, { color: s.color || t.text }]}>{s.value}</Text>
            </View>
          ))}
        </View>

        {}
        <View style={[styles.about, { backgroundColor: t.surface, borderColor: t.hairline }]}>
          <AboutRow label={T.sector} value={lang === 'ru' ? ticker.sector : ticker.sectorEn} t={t} divider />
          <AboutRow label={T.exchangeLabel} value={ticker.market} t={t} divider />
          <AboutRow label={T.currency} value={ticker.ccy} t={t} divider />
          <AboutRow label={T.lot} value="1" t={t} />
        </View>

        <Text style={[styles.risk, { color: t.textDim }]}>{T.riskWarning}</Text>
      </ScrollView>

      {}
      <View
        style={[styles.cta, { backgroundColor: t.bg }]}
        pointerEvents="box-none"
      >
        <Pressable
          onPress={() => onTrade(sym, 'sell')}
          style={[styles.ctaBtn, { backgroundColor: t.down }]}
        >
          <Text style={styles.ctaLabel}>{T.sell}</Text>
          <Text style={styles.ctaPrice}>
            {fmtNum(last.c - (last.c > 100 ? 0.05 : 0.01), 2)}
          </Text>
        </Pressable>
        <Pressable
          onPress={() => onTrade(sym, 'buy')}
          style={[styles.ctaBtn, { backgroundColor: t.up }]}
        >
          <Text style={styles.ctaLabel}>{T.buy}</Text>
          <Text style={styles.ctaPrice}>
            {fmtNum(last.c + (last.c > 100 ? 0.05 : 0.01), 2)}
          </Text>
        </Pressable>
      </View>
    </View>
  );
}

function AboutRow({
  label,
  value,
  t,
  divider,
}: {
  label: string;
  value: string;
  t: ReturnType<typeof useTheme>['tokens'];
  divider?: boolean;
}) {
  return (
    <View
      style={[
        styles.aboutRow,
        {
          borderBottomWidth: divider ? 1 : 0,
          borderColor: t.hairline,
        },
      ]}
    >
      <Text style={{ fontSize: 13, color: t.textMute, fontFamily: FONT_REGULAR }}>{label}</Text>
      <Text style={{ fontSize: 13, color: t.text, fontFamily: FONT_BOLD }}>{value}</Text>
    </View>
  );
}

function StudyChip({
  label,
  on,
  onPress,
}: {
  label: string;
  on: boolean;
  onPress: () => void;
}) {
  const { tokens: t } = useTheme();
  return (
    <Pressable
      onPress={onPress}
      style={[
        styles.study,
        {
          backgroundColor: on ? t.accentSoft : t.surface,
          borderColor: on ? t.accent : t.hairline,
        },
      ]}
    >
      <Text
        style={[
          styles.studyTxt,
          { color: on ? t.accent : t.textMute },
        ]}
      >
        {label}
      </Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  hero: { paddingHorizontal: 20, paddingTop: 4, paddingBottom: 18 },
  heroRow: { flexDirection: 'row', alignItems: 'baseline', gap: 8 },
  heroPrice: { fontSize: 38, fontFamily: FONT_BOLD, letterSpacing: -0.95 },
  heroCcy: { fontSize: 18, fontFamily: FONT_BOLD },
  heroChg: { flexDirection: 'row', alignItems: 'center', gap: 8, marginTop: 10, flexWrap: 'wrap' },
  heroChgTxt: { fontSize: 14, fontFamily: FONT_MONO },
  heroTag: { paddingHorizontal: 7, paddingVertical: 2, borderRadius: 100, marginLeft: 4 },
  heroTagTxt: { fontSize: 11, fontFamily: FONT_BOLD },
  chartWrap: {
    marginHorizontal: 16,
    paddingHorizontal: 4,
    paddingTop: 8,
    borderRadius: 16,
    borderWidth: 1,
    overflow: 'hidden',
  },
  periodRow: {
    flexDirection: 'row',
    gap: 4,
    marginHorizontal: 16,
    marginTop: 12,
    marginBottom: 8,
    padding: 4,
    borderRadius: 100,
    borderWidth: 1,
  },
  periodBtn: {
    flex: 1,
    paddingVertical: 8,
    borderRadius: 100,
    alignItems: 'center',
  },
  periodTxt: {
    fontSize: 12,
    fontFamily: FONT_BOLD,
    letterSpacing: 0.4,
    textTransform: 'uppercase',
  },
  tools: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    paddingHorizontal: 16,
    paddingBottom: 14,
    flexWrap: 'wrap',
  },
  kindToggle: { flexDirection: 'row', borderRadius: 10, padding: 3, borderWidth: 1 },
  kindBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 5,
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderRadius: 8,
  },
  kindTxt: { fontSize: 12, fontFamily: FONT_BOLD },
  study: {
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderRadius: 8,
    borderWidth: 1,
  },
  studyTxt: {
    fontSize: 11,
    fontFamily: FONT_BOLD,
    letterSpacing: 0.4,
    textTransform: 'uppercase',
  },
  stats: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    marginHorizontal: 16,
    marginBottom: 16,
    borderRadius: 14,
    borderWidth: 1,
    overflow: 'hidden',
  },
  stat: { width: '25%', padding: 10 },
  statLbl: {
    fontSize: 10,
    fontFamily: FONT_BOLD,
    letterSpacing: 0.6,
    textTransform: 'uppercase',
    marginBottom: 4,
  },
  statV: { fontSize: 13, fontFamily: FONT_MONO },
  about: {
    marginHorizontal: 16,
    marginBottom: 16,
    borderRadius: 14,
    borderWidth: 1,
    overflow: 'hidden',
  },
  aboutRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 12,
    paddingHorizontal: 14,
  },
  risk: { fontSize: 11, padding: 16, textAlign: 'center', lineHeight: 16 },
  cta: {
    position: 'absolute',
    left: 0,
    right: 0,
    bottom: 0,
    flexDirection: 'row',
    gap: 8,
    paddingHorizontal: 16,
    paddingTop: 12,
    paddingBottom: 16,
  },
  ctaBtn: {
    flex: 1,
    height: 56,
    borderRadius: 16,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 18,
  },
  ctaLabel: { fontSize: 16, fontFamily: FONT_BOLD, color: '#052218', letterSpacing: 0.32 },
  ctaPrice: { fontSize: 13, fontFamily: FONT_MONO, color: '#052218', opacity: 0.85 },
});
