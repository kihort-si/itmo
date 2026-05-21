import React, { useEffect, useMemo, useState } from 'react';
import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { useTheme } from '../theme/useTheme';
import { useI18n } from '../i18n/I18nContext';
import { brokerData } from '../data/repository';
import { usePortfolio } from '../hooks/usePortfolio';
import { fmtMoney, fmtPct, fmtSigned } from '../format';
import { TopBar } from '../components/TopBar';
import { IconButton } from '../components/IconButton';
import { Sparkline } from '../components/Sparkline';
import {
  IconAdd,
  IconBell,
  IconBriefcase,
  IconClose,
  IconHistory,
  IconRight,
  IconSwap,
} from '../icons/Icons';
import { FONT_BOLD, FONT_MONO, FONT_REGULAR } from '../theme/themes';
import { getFallbackPortfolioSeries, getSparkline } from '../services/marketService';
import { useDevStore } from '../stores/devStore';

type Ccy = 'RUB' | 'USD' | 'EUR';
const MARKET_REFRESH_MS = 5000;

export function PortfolioScreen({
  isAuthed,
  onSignIn,
  onOpenStock,
  onOpenHistory,
}: {
  isAuthed: boolean;
  onSignIn: () => void;
  onOpenStock: (sym: string) => void;
  onOpenHistory: () => void;
}) {
  if (!isAuthed) {
    return <PortfolioAuthPrompt onSignIn={onSignIn} />;
  }
  return (
    <PortfolioContent onOpenStock={onOpenStock} onOpenHistory={onOpenHistory} />
  );
}

function PortfolioAuthPrompt({ onSignIn }: { onSignIn: () => void }) {
  const { tokens: t } = useTheme();
  const { T } = useI18n();
  return (
    <View style={{ flex: 1, backgroundColor: t.bg }}>
      <TopBar large title={T.portfolio} />
      <View style={styles.authPrompt}>
        <View style={[styles.authGlyph, { backgroundColor: t.accentSoft }]}>
          <IconBriefcase size={48} color={t.accent} />
        </View>
        <Text style={[styles.authTitle, { color: t.text }]}>{T.authPromptTitle}</Text>
        <Text style={[styles.authSub, { color: t.textMute }]}>{T.authPromptSub}</Text>
        <Pressable
          onPress={onSignIn}
          style={[styles.btn, { backgroundColor: t.accent, alignSelf: 'stretch' }]}
        >
          <Text style={[styles.btnTxt, { color: t.accentFg }]}>{T.signIn}</Text>
        </Pressable>
      </View>
    </View>
  );
}

function PortfolioContent({
  onOpenStock,
  onOpenHistory,
}: {
  onOpenStock: (sym: string) => void;
  onOpenHistory: () => void;
}) {
  const { tokens: t } = useTheme();
  const { T } = useI18n();
  const [currency, setCurrency] = useState<Ccy>('RUB');
  const { summary } = usePortfolio();

  const enriched = summary?.holdings ?? [];
  const totalRub = summary?.totalValueRub ?? 0;
  const totalPnlRub = summary?.totalPnlRub ?? 0;
  const totalPnlPct = summary?.totalPnlPct ?? 0;
  const cashRub = summary?.cashRub ?? 0;
  const [portfolioSpark, setPortfolioSpark] = useState<number[]>([]);
  const sparkKey = enriched.map((holding) => `${holding.symbol}:${holding.quantity}`).join('|');
  const mockMarketData = useDevStore((s) => s.mockMarketData);

  const ccyRate =
    currency === 'RUB' ? 1 : currency === 'USD' ? brokerData.fx.RUB_USD : brokerData.fx.RUB_EUR;
  const totalDisp = totalRub * ccyRate;
  const cashDisp = cashRub * ccyRate;
  const totalPnlDisp = totalPnlRub * ccyRate;

  useEffect(() => {
    let cancelled = false;
    let inFlight = false;
    if (enriched.length === 0) {
      setPortfolioSpark(cashRub > 0 ? [cashRub, cashRub] : []);
      return;
    }
    const refresh = () => {
      if (inFlight) return;
      inFlight = true;
      void Promise.all(
        enriched.map(async (holding) => {
          const result = await getSparkline(holding.ticker, '1m');
          return { holding, spark: result.spark };
        })
      ).then((items) => {
        if (cancelled) return;
        const length = Math.max(...items.map((item) => item.spark.length), 0);
        if (length < 2) {
          setPortfolioSpark(
            mockMarketData
              ? getFallbackPortfolioSeries(
                  enriched.map((holding) => ({ ticker: holding.ticker, quantity: holding.quantity })),
                  cashRub
                )
              : []
          );
          return;
        }
        const totals = new Array(length).fill(0);
        items.forEach(({ holding, spark }) => {
          const rate = holding.ticker.ccy === 'USD' ? brokerData.fx.USD_RUB : 1;
          spark.forEach((point, index) => {
            totals[index] += point * holding.quantity * rate;
          });
        });
        setPortfolioSpark(totals.map((value) => value + cashRub));
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
  }, [cashRub, sparkKey, mockMarketData]);

  const portSpark = useMemo(
    () =>
      portfolioSpark.length >= 2 || !mockMarketData
        ? portfolioSpark
        : getFallbackPortfolioSeries(
            enriched.map((holding) => ({ ticker: holding.ticker, quantity: holding.quantity })),
            cashRub
          ),
    [cashRub, enriched, mockMarketData, portfolioSpark]
  );

  function cycleCcy() {
    const next: Record<Ccy, Ccy> = { RUB: 'USD', USD: 'EUR', EUR: 'RUB' };
    setCurrency(next[currency]);
  }

  return (
    <ScrollView
      style={{ flex: 1, backgroundColor: t.bg }}
      contentContainerStyle={{ paddingBottom: 100 }}
    >
      <TopBar
        large
        title={T.portfolio}
        right={<IconButton icon={<IconBell size={22} color={t.text} />} />}
      />

      <View
        style={[
          styles.balance,
          { backgroundColor: t.surface, borderColor: t.hairline },
        ]}
      >
        <View style={styles.balanceHead}>
          <Text style={[styles.balanceLbl, { color: t.textMute }]}>{T.totalValue}</Text>
          <Pressable
            onPress={cycleCcy}
            style={[styles.ccyPill, { backgroundColor: t.surface2, borderColor: t.hairline }]}
          >
            <IconSwap size={12} color={t.text} />
            <Text style={{ color: t.text, fontSize: 12, fontFamily: FONT_BOLD }}>{currency}</Text>
          </Pressable>
        </View>
        <Text style={[styles.balanceV, { color: t.text }]}>
          {fmtMoney(totalDisp, currency, totalDisp > 100000 ? 0 : 2)}
        </Text>
        <View style={styles.balanceChg}>
          <Text
            style={[
              styles.balanceChgTxt,
              { color: totalPnlRub >= 0 ? t.up : t.down },
            ]}
          >
            {fmtSigned(totalPnlDisp, totalPnlDisp > 1000 ? 0 : 2)}
          </Text>
          <Text
            style={[
              styles.balanceChgTxt,
              { color: totalPnlRub >= 0 ? t.up : t.down },
            ]}
          >
            {fmtPct(totalPnlPct)}
          </Text>
          <View style={[styles.tag, { backgroundColor: t.surface2 }]}>
            <Text style={{ fontSize: 11, color: t.textMute, fontFamily: FONT_BOLD }}>
              {T.allTime}
            </Text>
          </View>
        </View>
        <View style={{ marginTop: 12 }}>
          <Sparkline
            data={portSpark}
            w={320}
            h={56}
            color={totalPnlRub >= 0 ? t.up : t.down}
            fill
          />
        </View>
      </View>

      <Pressable
        onPress={onOpenHistory}
        style={[
          styles.cash,
          { backgroundColor: t.surface, borderColor: t.hairline },
        ]}
      >
        <View style={[styles.cashGlyph, { backgroundColor: t.accentSoft }]}>
          <IconBriefcase size={20} color={t.accent} />
        </View>
        <View style={{ flex: 1 }}>
          <Text style={{ fontSize: 12, color: t.textMute, fontFamily: FONT_REGULAR }}>
            {T.cashWallet}
          </Text>
          <Text style={[styles.cashV, { color: t.text }]}>
            {fmtMoney(cashDisp, currency, 0)}
          </Text>
        </View>
        <View style={[styles.cashAdd, { backgroundColor: t.accentSoft }]}>
          <IconAdd size={16} color={t.accent} />
          <Text style={{ color: t.accent, fontSize: 13, fontFamily: FONT_BOLD }}>{T.add}</Text>
        </View>
      </Pressable>

      <View style={styles.secHead}>
        <Text style={[styles.secTxt, { color: t.textMute }]}>{T.holdings}</Text>
        <Text style={[styles.secCount, { color: t.textDim }]}>{enriched.length}</Text>
      </View>
      <View style={{ paddingHorizontal: 16, gap: 4 }}>
        {enriched.map((h) => (
          <Pressable
            key={h.symbol}
            onPress={() => onOpenStock(h.symbol)}
            style={[
              styles.holding,
              { backgroundColor: t.surface, borderColor: t.hairline },
            ]}
          >
            <View
              style={[
                styles.holdingIcon,
                {
                  backgroundColor:
                    h.ticker.market === 'MOEX'
                      ? 'rgba(199,161,78,0.05)'
                      : 'rgba(107,182,255,0.05)',
                  borderColor:
                    h.ticker.market === 'MOEX'
                      ? 'rgba(199,161,78,0.3)'
                      : 'rgba(107,182,255,0.3)',
                },
              ]}
            >
              <Text
                style={{
                  fontFamily: FONT_MONO,
                  fontSize: 12,
                  color: h.ticker.market === 'MOEX' ? '#C7A14E' : '#6BB6FF',
                }}
              >
                {h.symbol.slice(0, 2)}
              </Text>
            </View>
            <View style={{ flex: 1, minWidth: 0 }}>
              <View style={{ flexDirection: 'row', alignItems: 'center', gap: 6 }}>
                <Text style={[styles.hSym, { color: t.text }]}>{h.symbol}</Text>
                <Text style={{ color: t.textDim, fontFamily: FONT_MONO, fontSize: 11 }}>
                  ×{h.quantity}
                </Text>
              </View>
              <Text
                style={{ fontSize: 12, color: t.textMute, fontFamily: FONT_REGULAR }}
                numberOfLines={1}
              >
                {h.ticker.nameRu}
              </Text>
            </View>
            <View style={{ alignItems: 'flex-end' }}>
              <Text style={[styles.hVal, { color: t.text }]}>
                {fmtMoney(h.valueRub * ccyRate, currency, 0)}
              </Text>
              <Text
                style={[
                  styles.hPnl,
                  { color: h.pnlRub >= 0 ? t.up : t.down },
                ]}
              >
                {fmtSigned(h.pnlRub * ccyRate, 0)}{' '}
                <Text style={{ color: t.textMute }}>({fmtPct(h.pnlPct, 1)})</Text>
              </Text>
            </View>
          </Pressable>
        ))}
      </View>

      {brokerData.activeOrders.length > 0 ? (
        <>
          <View style={styles.secHead}>
            <Text style={[styles.secTxt, { color: t.textMute }]}>{T.activeOrders}</Text>
            <Text style={[styles.secCount, { color: t.textDim }]}>{brokerData.activeOrders.length}</Text>
          </View>
          <View style={{ paddingHorizontal: 16, gap: 4 }}>
            {brokerData.activeOrders.map((o) => {
              const tk = brokerData.findTicker(o.sym);
              return (
                <View
                  key={o.id}
                  style={[
                    styles.order,
                    { backgroundColor: t.surface, borderColor: t.hairline },
                  ]}
                >
                  <View
                    style={[
                      styles.orderSide,
                      {
                        backgroundColor: o.side === 'buy' ? t.upSoft : t.downSoft,
                      },
                    ]}
                  >
                    <Text
                      style={{
                        color: o.side === 'buy' ? t.up : t.down,
                        fontSize: 10,
                        fontFamily: FONT_BOLD,
                        letterSpacing: 0.4,
                        textTransform: 'uppercase',
                      }}
                    >
                      {o.side === 'buy' ? T.typeBuy : T.typeSell}
                    </Text>
                  </View>
                  <View style={{ flex: 1 }}>
                    <Text style={{ color: t.text, fontSize: 14, fontFamily: FONT_BOLD }}>
                      {o.sym}
                    </Text>
                    <Text
                      style={{
                        color: t.textMute,
                        fontSize: 12,
                        fontFamily: FONT_MONO,
                        marginTop: 1,
                      }}
                    >
                      {o.qty} × {fmtMoney(o.limit, tk.ccy)}
                    </Text>
                  </View>
                  <View style={[styles.orderClose, { backgroundColor: t.surface2 }]}>
                    <IconClose size={14} color={t.textMute} />
                  </View>
                </View>
              );
            })}
          </View>
        </>
      ) : null}

      <Pressable
        onPress={onOpenHistory}
        style={[
          styles.linkRow,
          { borderColor: t.hairline },
        ]}
      >
        <IconHistory size={20} color={t.textMute} />
        <Text style={{ flex: 1, color: t.text, fontSize: 14, fontFamily: FONT_REGULAR }}>
          {T.historyTitle}
        </Text>
        <IconRight size={18} color={t.textDim} />
      </Pressable>

      <Text style={[styles.risk, { color: t.textDim }]}>{T.riskWarning}</Text>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  authPrompt: {
    flex: 1,
    paddingHorizontal: 32,
    alignItems: 'center',
    justifyContent: 'center',
    paddingTop: 40,
  },
  authGlyph: {
    width: 96,
    height: 96,
    borderRadius: 28,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 24,
  },
  authTitle: { fontSize: 22, fontFamily: FONT_BOLD, marginBottom: 8 },
  authSub: { fontSize: 14, textAlign: 'center', marginBottom: 28, maxWidth: 280, lineHeight: 20 },
  btn: {
    paddingVertical: 14,
    paddingHorizontal: 18,
    borderRadius: 12,
    alignItems: 'center',
    minHeight: 52,
    justifyContent: 'center',
  },
  btnTxt: { fontSize: 15, fontFamily: FONT_BOLD },
  balance: {
    marginHorizontal: 16,
    marginBottom: 14,
    paddingTop: 18,
    paddingHorizontal: 20,
    paddingBottom: 8,
    borderRadius: 18,
    borderWidth: 1,
    overflow: 'hidden',
  },
  balanceHead: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  balanceLbl: { fontSize: 12, fontFamily: FONT_BOLD, letterSpacing: 0.4, textTransform: 'uppercase' },
  ccyPill: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    paddingVertical: 5,
    paddingHorizontal: 10,
    borderRadius: 100,
    borderWidth: 1,
  },
  balanceV: { fontSize: 38, fontFamily: FONT_BOLD, letterSpacing: -0.95, marginTop: 8 },
  balanceChg: { flexDirection: 'row', alignItems: 'center', gap: 8, marginTop: 8 },
  balanceChgTxt: { fontSize: 14, fontFamily: FONT_MONO },
  tag: { paddingHorizontal: 7, paddingVertical: 2, borderRadius: 100 },
  cash: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    marginHorizontal: 16,
    marginBottom: 18,
    padding: 16,
    borderRadius: 14,
    borderWidth: 1,
  },
  cashGlyph: {
    width: 40,
    height: 40,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
  },
  cashV: { fontSize: 17, fontFamily: FONT_MONO, marginTop: 2 },
  cashAdd: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 5,
    paddingVertical: 8,
    paddingHorizontal: 12,
    borderRadius: 100,
  },
  secHead: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingTop: 18,
    paddingBottom: 10,
  },
  secTxt: {
    fontSize: 12,
    fontFamily: FONT_BOLD,
    letterSpacing: 0.6,
    textTransform: 'uppercase',
  },
  secCount: { fontSize: 12, fontFamily: FONT_MONO },
  holding: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    padding: 14,
    borderRadius: 14,
    borderWidth: 1,
  },
  holdingIcon: {
    width: 36,
    height: 36,
    borderRadius: 10,
    borderWidth: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  hSym: { fontSize: 14, fontFamily: FONT_BOLD },
  hVal: { fontSize: 14, fontFamily: FONT_MONO },
  hPnl: { fontSize: 11, fontFamily: FONT_MONO, marginTop: 2 },
  order: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    padding: 14,
    borderRadius: 14,
    borderWidth: 1,
  },
  orderSide: {
    paddingHorizontal: 8,
    paddingVertical: 5,
    borderRadius: 6,
  },
  orderClose: {
    width: 32,
    height: 32,
    borderRadius: 16,
    alignItems: 'center',
    justifyContent: 'center',
  },
  linkRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    paddingHorizontal: 20,
    paddingVertical: 14,
    borderTopWidth: 1,
    borderBottomWidth: 1,
    marginTop: 18,
  },
  risk: { fontSize: 11, padding: 16, textAlign: 'center', lineHeight: 16 },
});
