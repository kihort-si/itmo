import React, { useEffect, useMemo, useRef, useState } from 'react';
import {
  Animated,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import { useTheme } from '../theme/useTheme';
import { useI18n } from '../i18n/I18nContext';
import { brokerData } from '../data/repository';
import { useAuth } from '../hooks/useAuth';
import { useQuoteSubscription } from '../hooks/useQuoteSubscription';
import { getChartCandles } from '../services/marketService';
import { executeTrade } from '../services/tradingService';
import { fmtMoney, fmtNum } from '../format';
import { TopBar } from '../components/TopBar';
import { IconButton } from '../components/IconButton';
import { Segmented } from '../components/Segmented';
import { BottomSheet } from '../components/BottomSheet';
import {
  IconAdd,
  IconBack,
  IconCheck,
  IconClose,
  IconLock,
  IconMinus,
} from '../icons/Icons';
import { FONT_BOLD, FONT_MONO, FONT_REGULAR } from '../theme/themes';
import { useDevStore } from '../stores/devStore';

type Side = 'buy' | 'sell';
type OrderType = 'market' | 'limit';
const MARKET_REFRESH_MS = 5000;

export function TradeScreen({
  sym,
  initialSide,
  onBack,
  onClose,
  isAuthed,
  onSignIn,
  onPlaced,
}: {
  sym: string;
  initialSide: Side;
  onBack: () => void;
  onClose: () => void;
  isAuthed: boolean;
  onSignIn: () => void;
  onPlaced: () => void;
}) {
  const { tokens: t } = useTheme();
  const { T, lang } = useI18n();
  const { balance: cashRub } = useAuth();
  const ticker = brokerData.findTicker(sym);
  const liveQuote = useQuoteSubscription(sym);
  const mockMarketData = useDevStore((s) => s.mockMarketData);
  const [restPrice, setRestPrice] = useState<number | null>(null);
  const [side, setSide] = useState<Side>(initialSide);
  const [orderType, setOrderType] = useState<OrderType>('limit');
  const day = useMemo(
    () => (mockMarketData ? brokerData.dayStats(ticker) : null),
    [ticker, liveQuote, mockMarketData]
  );
  useEffect(() => {
    let cancelled = false;
    let inFlight = false;
    const refresh = () => {
      if (inFlight) return;
      inFlight = true;
      void getChartCandles(ticker, '1d', 'line').then((result) => {
        if (cancelled) return;
        const latest = result.candles[result.candles.length - 1];
        setRestPrice(latest?.c ?? null);
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
  }, [ticker, mockMarketData]);

  const lastPrice = mockMarketData ? liveQuote?.price ?? day?.price ?? 0 : restPrice ?? liveQuote?.price ?? 0;
  const ob = useMemo(
    () =>
      mockMarketData
        ? brokerData.genOrderbook(ticker)
        : { bids: [], asks: [], mid: lastPrice, tick: 0 },
    [ticker, lastPrice, mockMarketData]
  );
  const dp = lastPrice > 1000 ? 1 : 2;
  const bestAsk = ob.asks[0]?.price ?? lastPrice;
  const bestBid = ob.bids[0]?.price ?? lastPrice;
  const [limit, setLimit] = useState(side === 'buy' ? bestAsk : bestBid);
  const [qty, setQty] = useState(1);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [sent, setSent] = useState(false);
  const holdAnim = useRef(new Animated.Value(0)).current;
  const animRef = useRef<Animated.CompositeAnimation | null>(null);

  useEffect(() => {
    setLimit(side === 'buy' ? bestAsk : bestBid);
  }, [side, bestAsk, bestBid]);

  const totalCcy = limit * qty;
  const limitInRub = ticker.ccy === 'USD' ? limit * brokerData.fx.USD_RUB : limit;
  const totalRub = limitInRub * qty;
  const reserveRub = side === 'buy' ? totalRub * 1.005 : 0;
  const insufficient = side === 'buy' && reserveRub > cashRub;
  const fee = totalCcy * 0.0005;

  const tickStep = lastPrice > 1000 ? 0.5 : lastPrice > 100 ? 0.05 : 0.01;
  const adjLimit = (delta: number) =>
    setLimit((v) => Math.max(0, Number((v + delta * tickStep).toFixed(2))));

  function place() {
    if (!isAuthed) {
      onSignIn();
      return;
    }
    if (insufficient) return;
    setConfirmOpen(true);
  }

  function startHold() {
    holdAnim.setValue(0);
    animRef.current = Animated.timing(holdAnim, {
      toValue: 1,
      duration: 900,
      useNativeDriver: false,
    });
    animRef.current.start(({ finished }) => {
      if (finished) {
        void executeTrade(sym, qty, side)
          .then(() => {
            setSent(true);
            setTimeout(() => {
              setConfirmOpen(false);
              setSent(false);
              holdAnim.setValue(0);
              onPlaced();
            }, 1100);
          })
          .catch(() => {
            setConfirmOpen(false);
            holdAnim.setValue(0);
          });
      }
    });
  }

  function cancelHold() {
    animRef.current?.stop();
    if (!sent) holdAnim.setValue(0);
  }

  const obDisplayAsks = [...ob.asks].slice(0, 6).reverse();
  const obDisplayBids = ob.bids.slice(0, 6);
  const maxAskSize = Math.max(1, ...ob.asks.slice(0, 6).map((x) => x.size));
  const maxBidSize = Math.max(1, ...ob.bids.slice(0, 6).map((x) => x.size));

  const holdFillWidth = holdAnim.interpolate({ inputRange: [0, 1], outputRange: ['0%', '100%'] });

  return (
    <View style={{ flex: 1, backgroundColor: t.bg }}>
      <ScrollView contentContainerStyle={{ paddingBottom: 110 }}>
        <TopBar
          left={<IconButton icon={<IconBack size={22} color={t.text} />} onPress={onBack} />}
          title={`${side === 'buy' ? T.buy : T.sell} ${ticker.sym}`}
          subtitle={lang === 'ru' ? ticker.nameRu : ticker.nameEn}
          right={<IconButton icon={<IconClose size={22} color={t.text} />} onPress={onClose} />}
        />

        {}
        <View
          style={[styles.sideRow, { backgroundColor: t.surface, borderColor: t.hairline }]}
        >
          <Pressable
            onPress={() => setSide('buy')}
            style={[
              styles.sideTab,
              side === 'buy' && { backgroundColor: t.up },
            ]}
          >
            <Text
              style={[
                styles.sideTxt,
                { color: side === 'buy' ? '#052218' : t.textMute },
              ]}
            >
              {T.buy}
            </Text>
          </Pressable>
          <Pressable
            onPress={() => setSide('sell')}
            style={[
              styles.sideTab,
              side === 'sell' && { backgroundColor: t.down },
            ]}
          >
            <Text
              style={[
                styles.sideTxt,
                { color: side === 'sell' ? '#2A0815' : t.textMute },
              ]}
            >
              {T.sell}
            </Text>
          </Pressable>
        </View>

        {}
        <View style={{ paddingHorizontal: 16, paddingBottom: 14 }}>
          <Segmented
            value={orderType}
            onChange={setOrderType}
            options={[
              { value: 'market', label: T.market },
              { value: 'limit', label: T.limit },
            ]}
          />
        </View>

        {}
        <View style={[styles.ob, { backgroundColor: t.surface, borderColor: t.hairline }]}>
          <View style={[styles.obHead, { borderBottomColor: t.hairline }]}>
            <Text style={[styles.obHeadTxt, { color: t.textDim }]}>
              {T.bookPrice}, {ticker.ccy === 'RUB' ? '₽' : '$'}
            </Text>
            <Text style={[styles.obHeadTxt, { color: t.textDim }]}>{T.bookSize}</Text>
          </View>
          {obDisplayAsks.map((a, i) => {
            const wPct = (a.size / maxAskSize) * 100;
            return (
              <Pressable
                key={`a${i}`}
                onPress={() => {
                  setSide('buy');
                  setLimit(a.price);
                }}
                style={styles.obRow}
              >
                <View
                  style={[
                    styles.obBar,
                    { width: `${wPct}%`, backgroundColor: t.down, opacity: 0.18 },
                  ]}
                />
                <Text style={[styles.obPrice, { color: t.down }]}>{fmtNum(a.price, dp)}</Text>
                <Text style={[styles.obSize, { color: t.textMute }]}>{a.size}</Text>
              </Pressable>
            );
          })}
          <View
            style={[
              styles.obMid,
              {
                backgroundColor: t.surface2,
                borderTopColor: t.hairline,
                borderBottomColor: t.hairline,
              },
            ]}
          >
            <Text style={[styles.obMidLbl, { color: t.textDim }]}>{T.bookMid}</Text>
            <Text style={[styles.obMidV, { color: t.text }]}>{fmtNum(ob.mid, dp)}</Text>
            <Text style={[styles.obMidSpread, { color: t.textMute }]}>
              {T.spread} {fmtNum(ob.tick, 2)}
            </Text>
          </View>
          {obDisplayBids.map((b, i) => {
            const wPct = (b.size / maxBidSize) * 100;
            return (
              <Pressable
                key={`b${i}`}
                onPress={() => {
                  setSide('sell');
                  setLimit(b.price);
                }}
                style={styles.obRow}
              >
                <View
                  style={[
                    styles.obBar,
                    { width: `${wPct}%`, backgroundColor: t.up, opacity: 0.18 },
                  ]}
                />
                <Text style={[styles.obPrice, { color: t.up }]}>{fmtNum(b.price, dp)}</Text>
                <Text style={[styles.obSize, { color: t.textMute }]}>{b.size}</Text>
              </Pressable>
            );
          })}
        </View>

        {}
        <View style={{ paddingHorizontal: 16 }}>
          {}
          <View style={{ marginBottom: 14 }}>
            <Text style={[styles.tfLabel, { color: t.textMute }]}>{T.qty}</Text>
            <View
              style={[styles.stepper, { backgroundColor: t.surface, borderColor: t.hairline }]}
            >
              <Pressable
                onPress={() => setQty((q) => Math.max(1, q - 1))}
                style={[styles.stepBtn, { borderRightColor: t.hairline }]}
              >
                <IconMinus size={18} color={t.text} />
              </Pressable>
              <TextInput
                value={String(qty)}
                onChangeText={(v) => {
                  const n = Math.max(1, Math.floor(Number(v) || 1));
                  setQty(n);
                }}
                keyboardType="number-pad"
                style={[styles.stepInput, { color: t.text }]}
              />
              <Pressable
                onPress={() => setQty((q) => q + 1)}
                style={[styles.stepBtn, { borderLeftColor: t.hairline, borderLeftWidth: 1 }]}
              >
                <IconAdd size={18} color={t.text} />
              </Pressable>
            </View>
            <View style={styles.quickRow}>
              {[1, 5, 10, 50, 100].map((q) => {
                const on = qty === q;
                return (
                  <Pressable
                    key={q}
                    onPress={() => setQty(q)}
                    style={[
                      styles.quick,
                      {
                        backgroundColor: on ? t.text : t.surface,
                        borderColor: on ? t.text : t.hairline,
                      },
                    ]}
                  >
                    <Text
                      style={[
                        styles.quickTxt,
                        { color: on ? t.textInv : t.textMute },
                      ]}
                    >
                      {q}
                    </Text>
                  </Pressable>
                );
              })}
            </View>
          </View>

          {}
          {orderType === 'limit' ? (
            <View style={{ marginBottom: 14 }}>
              <Text style={[styles.tfLabel, { color: t.textMute }]}>
                {side === 'buy' ? T.upperPrice : T.lowerPrice}
              </Text>
              <View
                style={[styles.stepper, { backgroundColor: t.surface, borderColor: t.hairline }]}
              >
                <Pressable
                  onPress={() => adjLimit(-1)}
                  style={[styles.stepBtn, { borderRightColor: t.hairline }]}
                >
                  <IconMinus size={18} color={t.text} />
                </Pressable>
                <TextInput
                  value={limit.toFixed(dp)}
                  onChangeText={(v) => setLimit(Number(v.replace(',', '.')) || 0)}
                  keyboardType="decimal-pad"
                  style={[styles.stepInput, { color: t.text }]}
                />
                <Pressable
                  onPress={() => adjLimit(1)}
                  style={[styles.stepBtn, { borderLeftColor: t.hairline, borderLeftWidth: 1 }]}
                >
                  <IconAdd size={18} color={t.text} />
                </Pressable>
              </View>
              <View style={styles.hint}>
                <Text style={{ fontSize: 11, color: t.textMute }}>
                  {T.bestBid}:{' '}
                  <Text style={{ color: t.down, fontFamily: FONT_MONO }}>
                    {fmtNum(bestBid, dp)}
                  </Text>
                </Text>
                <Text style={{ fontSize: 11, color: t.textMute }}>
                  {T.bestAsk}:{' '}
                  <Text style={{ color: t.up, fontFamily: FONT_MONO }}>
                    {fmtNum(bestAsk, dp)}
                  </Text>
                </Text>
              </View>
            </View>
          ) : null}

          {}
          <View style={[styles.summary, { backgroundColor: t.surface, borderColor: t.hairline }]}>
            <SumRow label={T.total} t={t}>
              <Text style={[styles.sumV, { color: t.text }]}>
                {fmtMoney(totalCcy, ticker.ccy)}
              </Text>
              {ticker.ccy !== 'RUB' ? (
                <Text style={[styles.sumVDim, { color: t.textMute }]}>
                  {' '}
                  ≈ {fmtMoney(totalRub, 'RUB', 0)}
                </Text>
              ) : null}
            </SumRow>
            <SumRow label={T.fee} t={t}>
              <Text style={[styles.sumVDim, { color: t.textMute }]}>
                {fmtMoney(fee, ticker.ccy)}
              </Text>
            </SumRow>
            {side === 'buy' ? (
              <SumRow label={T.estReserve} t={t}>
                <Text style={[styles.sumV, { color: t.text }]}>
                  {fmtMoney(reserveRub, 'RUB', 0)}
                </Text>
              </SumRow>
            ) : null}
            <SumRow label={T.available} t={t}>
              <Text style={[styles.sumV, { color: insufficient ? t.down : t.text }]}>
                {fmtMoney(cashRub, 'RUB', 0)}
              </Text>
            </SumRow>
          </View>

          {insufficient ? (
            <View style={[styles.err, { backgroundColor: t.downSoft }]}>
              <IconLock size={14} color={t.down} />
              <Text style={{ color: t.down, fontSize: 13, marginLeft: 8, fontFamily: FONT_BOLD }}>
                {T.insufficient}
              </Text>
            </View>
          ) : null}
        </View>
      </ScrollView>

      {}
      <View style={[styles.cta, { backgroundColor: t.bg }]}>
        <Pressable
          onPress={place}
          disabled={insufficient}
          style={[
            styles.ctaBtn,
            {
              backgroundColor: side === 'buy' ? t.up : t.down,
              opacity: insufficient ? 0.4 : 1,
            },
          ]}
        >
          <Text style={[styles.ctaLabel, { color: side === 'buy' ? '#052218' : '#2A0815' }]}>
            {!isAuthed ? T.signIn : side === 'buy' ? T.placeBuy : T.placeSell}
          </Text>
          <Text
            style={[styles.ctaPrice, { color: side === 'buy' ? '#052218' : '#2A0815' }]}
          >
            {fmtMoney(totalCcy, ticker.ccy)}
          </Text>
        </Pressable>
      </View>

      {}
      <BottomSheet
        open={confirmOpen}
        onClose={() => {
          if (!sent) {
            setConfirmOpen(false);
            cancelHold();
          }
        }}
      >
        {!sent ? (
          <View style={{ paddingHorizontal: 4 }}>
            <Text style={[styles.confTitle, { color: t.text }]}>{T.confirmTitle}</Text>
            <View
              style={[
                styles.confCard,
                { backgroundColor: t.surface2, borderColor: t.hairline },
              ]}
            >
              <ConfRow label={T.action} t={t} divider>
                <Text style={{ color: side === 'buy' ? t.up : t.down, fontFamily: FONT_MONO }}>
                  {side === 'buy' ? T.typeBuy : T.typeSell}
                </Text>
              </ConfRow>
              <ConfRow label={T.instrument} t={t} divider>
                <Text style={{ color: t.text, fontFamily: FONT_MONO }}>
                  {ticker.sym} · {ticker.market}
                </Text>
              </ConfRow>
              <ConfRow label={T.qty} t={t} divider>
                <Text style={{ color: t.text, fontFamily: FONT_MONO }}>{qty}</Text>
              </ConfRow>
              <ConfRow
                label={orderType === 'market' ? T.market : side === 'buy' ? T.upperPrice : T.lowerPrice}
                t={t}
                divider
              >
                <Text style={{ color: t.text, fontFamily: FONT_MONO }}>
                  {fmtMoney(limit, ticker.ccy)}
                </Text>
              </ConfRow>
              <ConfRow label={T.total} t={t} big>
                <Text
                  style={{
                    color: t.text,
                    fontFamily: FONT_MONO,
                    fontSize: 19,
                  }}
                >
                  {fmtMoney(totalCcy, ticker.ccy)}
                </Text>
              </ConfRow>
            </View>

            <Pressable
              onPressIn={startHold}
              onPressOut={cancelHold}
              style={[
                styles.holdBtn,
                { backgroundColor: side === 'buy' ? t.up : t.down },
              ]}
            >
              <Animated.View
                style={[
                  styles.holdFill,
                  {
                    width: holdFillWidth,
                  },
                ]}
              />
              <Text
                style={[
                  styles.holdLabel,
                  { color: side === 'buy' ? '#052218' : '#2A0815' },
                ]}
              >
                {side === 'buy' ? T.confirmBuy : T.confirmSell}
              </Text>
            </Pressable>
            <Pressable
              onPress={() => setConfirmOpen(false)}
              style={[
                styles.btnGhost,
                { borderColor: t.hairline },
              ]}
            >
              <Text style={{ color: t.text, fontFamily: FONT_BOLD, fontSize: 14 }}>
                {T.cancel}
              </Text>
            </Pressable>
          </View>
        ) : (
          <View style={{ alignItems: 'center', paddingVertical: 16 }}>
            <View style={[styles.successGlyph, { backgroundColor: t.upSoft }]}>
              <IconCheck size={36} color={t.up} />
            </View>
            <Text style={[styles.successTitle, { color: t.text }]}>{T.sentTitle}</Text>
            <Text style={[styles.successSub, { color: t.textMute }]}>
              {side === 'buy' ? T.sentSubBuy : T.sentSubSell}
            </Text>
            <View
              style={[
                styles.successCard,
                { backgroundColor: t.surface2, borderColor: t.hairline },
              ]}
            >
              <Text style={{ color: t.text, fontFamily: FONT_MONO, fontSize: 14 }}>
                {ticker.sym} · {qty} × {fmtMoney(limit, ticker.ccy)}
              </Text>
              <Text
                style={{
                  color: t.textMute,
                  fontFamily: FONT_MONO,
                  fontSize: 13,
                  marginTop: 4,
                }}
              >
                = {fmtMoney(totalCcy, ticker.ccy)}
              </Text>
            </View>
          </View>
        )}
      </BottomSheet>
    </View>
  );
}

function SumRow({
  label,
  children,
  t,
}: {
  label: string;
  children: React.ReactNode;
  t: ReturnType<typeof useTheme>['tokens'];
}) {
  return (
    <View style={styles.sumRow}>
      <Text style={{ fontSize: 13, color: t.textMute, fontFamily: FONT_REGULAR }}>{label}</Text>
      <View style={{ flexDirection: 'row', alignItems: 'baseline' }}>{children}</View>
    </View>
  );
}

function ConfRow({
  label,
  children,
  t,
  divider,
  big,
}: {
  label: string;
  children: React.ReactNode;
  t: ReturnType<typeof useTheme>['tokens'];
  divider?: boolean;
  big?: boolean;
}) {
  return (
    <View
      style={[
        styles.confRow,
        {
          borderBottomWidth: divider ? 1 : 0,
          borderColor: t.hairline,
          paddingTop: big ? 14 : 12,
        },
      ]}
    >
      <Text style={{ fontSize: 13, color: t.textMute, fontFamily: FONT_REGULAR }}>{label}</Text>
      <View>{children}</View>
    </View>
  );
}

const styles = StyleSheet.create({
  sideRow: {
    flexDirection: 'row',
    gap: 4,
    marginHorizontal: 16,
    marginBottom: 12,
    padding: 4,
    borderRadius: 14,
    borderWidth: 1,
  },
  sideTab: { flex: 1, paddingVertical: 12, alignItems: 'center', borderRadius: 10 },
  sideTxt: { fontSize: 14, fontFamily: FONT_BOLD, letterSpacing: 0.5, textTransform: 'uppercase' },
  ob: { marginHorizontal: 16, marginBottom: 14, borderRadius: 14, borderWidth: 1, overflow: 'hidden' },
  obHead: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingHorizontal: 14,
    paddingVertical: 8,
    borderBottomWidth: 1,
  },
  obHeadTxt: { fontSize: 10, fontFamily: FONT_BOLD, letterSpacing: 0.6, textTransform: 'uppercase' },
  obRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 14,
    paddingVertical: 7,
    position: 'relative',
  },
  obBar: { position: 'absolute', top: 0, bottom: 0, right: 0 },
  obPrice: { fontSize: 13, fontFamily: FONT_MONO },
  obSize: { fontSize: 12, fontFamily: FONT_MONO },
  obMid: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 14,
    paddingVertical: 10,
    borderTopWidth: 1,
    borderBottomWidth: 1,
  },
  obMidLbl: { fontSize: 10, fontFamily: FONT_BOLD, letterSpacing: 0.6, textTransform: 'uppercase' },
  obMidV: { fontSize: 16, fontFamily: FONT_MONO },
  obMidSpread: { fontSize: 10, fontFamily: FONT_MONO },
  tfLabel: {
    fontSize: 11,
    fontFamily: FONT_BOLD,
    letterSpacing: 0.6,
    textTransform: 'uppercase',
    marginBottom: 8,
  },
  stepper: {
    flexDirection: 'row',
    alignItems: 'stretch',
    borderRadius: 14,
    borderWidth: 1,
    overflow: 'hidden',
    height: 52,
  },
  stepBtn: {
    width: 52,
    alignItems: 'center',
    justifyContent: 'center',
    borderRightWidth: 1,
  },
  stepInput: {
    flex: 1,
    textAlign: 'center',
    fontSize: 22,
    fontFamily: FONT_MONO,
    padding: 0,
  },
  quickRow: { flexDirection: 'row', gap: 6, marginTop: 8 },
  quick: {
    flex: 1,
    paddingVertical: 7,
    alignItems: 'center',
    borderRadius: 8,
    borderWidth: 1,
  },
  quickTxt: { fontSize: 12, fontFamily: FONT_MONO },
  hint: { flexDirection: 'row', justifyContent: 'space-between', marginTop: 8 },
  summary: {
    marginTop: 6,
    padding: 14,
    borderRadius: 14,
    borderWidth: 1,
  },
  sumRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'baseline',
    paddingVertical: 4,
  },
  sumV: { fontSize: 13, fontFamily: FONT_MONO },
  sumVDim: { fontSize: 12, fontFamily: FONT_MONO },
  err: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 12,
    padding: 12,
    borderRadius: 12,
  },
  cta: {
    position: 'absolute',
    left: 0,
    right: 0,
    bottom: 0,
    paddingHorizontal: 16,
    paddingTop: 12,
    paddingBottom: 16,
  },
  ctaBtn: {
    height: 56,
    borderRadius: 16,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 18,
  },
  ctaLabel: { fontSize: 16, fontFamily: FONT_BOLD, letterSpacing: 0.32 },
  ctaPrice: { fontSize: 13, fontFamily: FONT_MONO, opacity: 0.85 },
  confTitle: { fontSize: 18, fontFamily: FONT_BOLD, marginBottom: 14 },
  confCard: {
    paddingHorizontal: 14,
    paddingVertical: 6,
    borderRadius: 14,
    borderWidth: 1,
    marginBottom: 16,
  },
  confRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'baseline',
    paddingBottom: 12,
  },
  holdBtn: {
    width: '100%',
    height: 60,
    borderRadius: 16,
    overflow: 'hidden',
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 8,
  },
  holdFill: {
    position: 'absolute',
    top: 0,
    left: 0,
    bottom: 0,
    backgroundColor: 'rgba(255,255,255,0.18)',
  },
  holdLabel: {
    fontSize: 15,
    fontFamily: FONT_BOLD,
    letterSpacing: 0.9,
    textTransform: 'uppercase',
  },
  btnGhost: {
    width: '100%',
    height: 52,
    borderRadius: 12,
    borderWidth: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  successGlyph: {
    width: 72,
    height: 72,
    borderRadius: 36,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 14,
  },
  successTitle: { fontSize: 22, fontFamily: FONT_BOLD, marginBottom: 4 },
  successSub: { fontSize: 14, marginBottom: 18, textAlign: 'center' },
  successCard: {
    padding: 14,
    borderRadius: 12,
    borderWidth: 1,
    alignItems: 'center',
    minWidth: 220,
  },
});
