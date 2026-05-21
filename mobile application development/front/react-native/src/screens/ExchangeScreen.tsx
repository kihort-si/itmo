import React, { useEffect, useMemo, useState } from 'react';
import {
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
import { useQuotesSubscription } from '../hooks/useQuoteSubscription';
import { getQuote } from '../services/quotesService';
import { useQuoteStore } from '../stores/quoteStore';
import { fmtNum, fmtPct, fmtSigned } from '../format';
import { TopBar } from '../components/TopBar';
import { IconButton } from '../components/IconButton';
import { Sparkline } from '../components/Sparkline';
import { BottomSheet } from '../components/BottomSheet';
import { DualRange } from '../components/DualRange';
import {
  IconClose,
  IconDown,
  IconFilter,
  IconSearch,
  IconStar,
  IconStarSolid,
  IconUp,
} from '../icons/Icons';
import { FONT_BOLD, FONT_MONO, FONT_REGULAR } from '../theme/themes';
import { getMarketStocks, getSparkline } from '../services/marketService';
import type { MarketStockRow } from '../api/types';
import { useDevStore } from '../stores/devStore';

type SortKey = 'price' | 'name' | 'change';
type FilterKey = 'all' | 'moex' | 'nasdaq' | 'favs';
const MARKET_REFRESH_MS = 5000;
type ExchangeRow = {
  sym: string;
  market: 'MOEX' | 'NASDAQ';
  nameRu: string;
  nameEn: string;
  ccy: 'RUB' | 'USD' | 'EUR';
  price: number;
  change: number;
  changePct: number;
  spark: number[];
};

export function ExchangeScreen({
  onOpenStock,
  favorites,
  toggleFavorite,
}: {
  onOpenStock: (sym: string) => void;
  favorites: Record<string, boolean>;
  toggleFavorite: (sym: string) => void;
}) {
  const { tokens: t, tweaks } = useTheme();
  const { T, lang } = useI18n();
  const [search, setSearch] = useState('');
  const [sort, setSort] = useState<SortKey>('change');
  const [sortDir, setSortDir] = useState<'asc' | 'desc'>('desc');
  const [filter, setFilter] = useState<FilterKey>('all');
  const [filterOpen, setFilterOpen] = useState(false);
  const [priceRange, setPriceRange] = useState<[number, number]>([0, 10000]);
  const [chgRange, setChgRange] = useState<[number, number]>([-10, 10]);
  const [marketRows, setMarketRows] = useState<MarketStockRow[] | null>(null);
  const [sparks, setSparks] = useState<Record<string, number[]>>({});
  const mockMarketData = useDevStore((s) => s.mockMarketData);

  const symbols = useMemo(
    () => (marketRows?.map((row) => row.ticker) ?? brokerData.tickers.map((tk) => tk.sym)),
    [marketRows]
  );
  useQuotesSubscription(symbols);
  const quotes = useQuoteStore((s) => s.quotes);

  useEffect(() => {
    let cancelled = false;
    let inFlight = false;
    const sortByMap: Record<SortKey, 'PRICE' | 'NAME' | 'DAY_CHANGE_PCT'> = {
      price: 'PRICE',
      name: 'NAME',
      change: 'DAY_CHANGE_PCT',
    };
    const refresh = () => {
      if (inFlight) return;
      inFlight = true;
      void getMarketStocks({
        search: search.trim() || undefined,
        sortBy: sortByMap[sort],
        sortOrder: sortDir.toUpperCase() as 'ASC' | 'DESC',
        minPrice: priceRange[0] > 0 ? priceRange[0] : undefined,
        maxPrice: priceRange[1] < 10000 ? priceRange[1] : undefined,
        minDayChangePct: chgRange[0] > -10 ? chgRange[0] : undefined,
        maxDayChangePct: chgRange[1] < 10 ? chgRange[1] : undefined,
      }).then((rows) => {
        if (cancelled) return;
        if (!rows) {
          setMarketRows(null);
          return;
        }
        setMarketRows(rows);
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
  }, [search, sort, sortDir, priceRange, chgRange, mockMarketData]);

  useEffect(() => {
    let cancelled = false;
    let inFlight = false;
    const refresh = () => {
      if (inFlight) return;
      inFlight = true;
      void Promise.all(
        brokerData.tickers.map(async (ticker) => {
          const result = await getSparkline(ticker, '1m');
          return [ticker.sym, result.spark] as const;
        })
      ).then((entries) => {
        if (cancelled) return;
        setSparks(Object.fromEntries(entries));
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
  }, [mockMarketData]);

  function deriveChange(price: number, changePct: number) {
    const ratio = 1 + changePct / 100;
    if (ratio === 0) return 0;
    return price - price / ratio;
  }

  function inferMarket(row: MarketStockRow): 'MOEX' | 'NASDAQ' {
    const local = brokerData.tickers.find((ticker) => ticker.sym === row.ticker);
    if (local) return local.market;
    if (row.currency === 'USD') return 'NASDAQ';
    return 'MOEX';
  }

  function localizeName(row: MarketStockRow) {
    const local = brokerData.tickers.find((ticker) => ticker.sym === row.ticker);
    if (local) {
      return { nameRu: local.nameRu, nameEn: local.nameEn, ccy: local.ccy };
    }
    return {
      nameRu: row.name,
      nameEn: row.name,
      ccy: (row.currency as 'RUB' | 'USD' | 'EUR') || 'RUB',
    };
  }

  const rows = useMemo<ExchangeRow[]>(
    () => {
      if (marketRows && marketRows.length > 0) {
        return marketRows.map((row) => {
          const fallbackQuote = getQuote(row.ticker);
          const q = quotes[row.ticker];
          const price = mockMarketData
            ? q?.price ?? row.lastPrice ?? fallbackQuote?.price ?? 0
            : row.lastPrice ?? q?.price ?? 0;
          const changePct = mockMarketData
            ? q?.changePct ?? row.dayChangePct ?? fallbackQuote?.changePct ?? 0
            : row.dayChangePct ?? q?.changePct ?? 0;
          const change = q?.change ?? deriveChange(price, changePct);
          const meta = localizeName(row);
          return {
            sym: row.ticker,
            market: inferMarket(row),
            nameRu: meta.nameRu,
            nameEn: meta.nameEn,
            ccy: meta.ccy,
            price,
            change,
            changePct,
            spark: mockMarketData
              ? q?.spark ?? sparks[row.ticker] ?? fallbackQuote?.spark ?? []
              : sparks[row.ticker] ?? [],
          };
        });
      }

      return brokerData.tickers.map((tk) => {
        const fallbackQuote = getQuote(tk.sym);
        const q = quotes[tk.sym];
        const price = q?.price ?? fallbackQuote?.price ?? 0;
        const changePct = q?.changePct ?? fallbackQuote?.changePct ?? 0;
        const change = q?.change ?? deriveChange(price, changePct);
        return {
          sym: tk.sym,
          market: tk.market,
          nameRu: tk.nameRu,
          nameEn: tk.nameEn,
          ccy: tk.ccy,
          price,
          change,
          changePct,
          spark: mockMarketData
            ? q?.spark ?? sparks[tk.sym] ?? fallbackQuote?.spark ?? []
            : sparks[tk.sym] ?? [],
        };
      });
    },
    [marketRows, mockMarketData, quotes, sparks]
  );

  const filtered = useMemo(() => {
    let r = rows;
    if (search.trim()) {
      const q = search.toLowerCase();
      r = r.filter(
        (x) =>
          x.sym.toLowerCase().includes(q) ||
          x.nameRu.toLowerCase().includes(q) ||
          x.nameEn.toLowerCase().includes(q)
      );
    }
    if (filter === 'moex') r = r.filter((x) => x.market === 'MOEX');
    if (filter === 'nasdaq') r = r.filter((x) => x.market === 'NASDAQ');
    if (filter === 'favs') r = r.filter((x) => favorites[x.sym]);
    if (priceRange[0] > 0 || priceRange[1] < 10000) {
      r = r.filter((x) => {
        const p = x.ccy === 'USD' ? x.price * brokerData.fx.USD_RUB : x.price;
        return p >= priceRange[0] && p <= priceRange[1];
      });
    }
    if (chgRange[0] > -10 || chgRange[1] < 10) {
      r = r.filter((x) => x.changePct >= chgRange[0] && x.changePct <= chgRange[1]);
    }
    const dir = sortDir === 'asc' ? 1 : -1;
    r = [...r].sort((a, b) => {
      if (sort === 'price') {
        const pa = a.ccy === 'USD' ? a.price * brokerData.fx.USD_RUB : a.price;
        const pb = b.ccy === 'USD' ? b.price * brokerData.fx.USD_RUB : b.price;
        return (pa - pb) * dir;
      }
      if (sort === 'name') return a.sym.localeCompare(b.sym) * dir;
      return (a.changePct - b.changePct) * dir;
    });
    return r;
  }, [rows, search, filter, sort, sortDir, priceRange, chgRange, favorites]);

  const gainers = rows.filter((r) => r.changePct > 0).length;
  const losers = rows.length - gainers;

  function clickSort(k: SortKey) {
    if (sort === k) setSortDir((d) => (d === 'asc' ? 'desc' : 'asc'));
    else {
      setSort(k);
      setSortDir(k === 'name' ? 'asc' : 'desc');
    }
  }

  const isCompact = tweaks.density === 'compact';

  return (
    <ScrollView
      style={{ flex: 1, backgroundColor: t.bg }}
      contentContainerStyle={{ paddingBottom: 100 }}
    >
      <TopBar
        large
        title="MyBroker"
        subtitle={`${brokerData.tickers.length} ${T.instruments}`}
        right={<IconButton icon={<IconSearch size={22} color={t.text} />} />}
      />

      {}
      <View style={[styles.searchRow, { backgroundColor: t.surface, borderColor: t.hairline }]}>
        <IconSearch size={18} color={t.textMute} />
        <TextInput
          style={[styles.searchInput, { color: t.text }]}
          placeholder={T.searchPlaceholder}
          placeholderTextColor={t.textDim}
          value={search}
          onChangeText={setSearch}
        />
        {search ? (
          <Pressable onPress={() => setSearch('')} hitSlop={6}>
            <IconClose size={16} color={t.textMute} />
          </Pressable>
        ) : null}
      </View>

      {}
      <View style={[styles.pulse, { backgroundColor: t.surface, borderColor: t.hairline }]}>
        <PulseCell label={T.gainers} value={String(gainers)} valColor={t.up} t={t} />
        <View style={[styles.pulseDiv, { backgroundColor: t.hairline }]} />
        <PulseCell label={T.losers} value={String(losers)} valColor={t.down} t={t} />
        <View style={[styles.pulseDiv, { backgroundColor: t.hairline }]} />
        <PulseCell label={brokerData.indices[0].label} value={brokerData.indices[0].value} mono t={t} />
        <View style={[styles.pulseDiv, { backgroundColor: t.hairline }]} />
        <PulseCell label={brokerData.indices[1].label} value={brokerData.indices[1].value} mono t={t} />
      </View>

      {}
      <ScrollView
        horizontal
        showsHorizontalScrollIndicator={false}
        contentContainerStyle={styles.chipRow}
      >
        <Chip on={filter === 'all'} label={T.filterAll} onPress={() => setFilter('all')} />
        <Chip on={filter === 'moex'} label="MOEX" onPress={() => setFilter('moex')} />
        <Chip on={filter === 'nasdaq'} label="NASDAQ" onPress={() => setFilter('nasdaq')} />
        <Chip
          on={filter === 'favs'}
          label={T.filterFavs}
          onPress={() => setFilter('favs')}
          icon={<IconStar size={12} color={filter === 'favs' ? t.textInv : t.textMute} />}
        />
        <View style={{ flex: 1, minWidth: 8 }} />
        <Chip
          ghost
          on={false}
          label={T.filters}
          onPress={() => setFilterOpen(true)}
          icon={<IconFilter size={12} color={t.textMute} />}
        />
      </ScrollView>

      {}
      <View style={[styles.listHead, { borderBottomColor: t.hairline, paddingVertical: isCompact ? 4 : 6 }]}>
        <Pressable onPress={() => clickSort('name')} style={styles.lhSym}>
          <Text style={[styles.lhTxt, { color: t.textDim }]}>{T.sortName}</Text>
          {sort === 'name' ? (sortDir === 'asc' ? <IconUp size={10} color={t.textDim} /> : <IconDown size={10} color={t.textDim} />) : null}
        </Pressable>
        <View style={styles.lhSpark} />
        <Pressable onPress={() => clickSort('price')} style={styles.lhPrice}>
          <Text style={[styles.lhTxt, { color: t.textDim }]}>{T.sortPrice}</Text>
          {sort === 'price' ? (sortDir === 'asc' ? <IconUp size={10} color={t.textDim} /> : <IconDown size={10} color={t.textDim} />) : null}
        </Pressable>
        <Pressable onPress={() => clickSort('change')} style={styles.lhChg}>
          <Text style={[styles.lhTxt, { color: t.textDim }]}>{T.sortChange}</Text>
          {sort === 'change' ? (sortDir === 'asc' ? <IconUp size={10} color={t.textDim} /> : <IconDown size={10} color={t.textDim} />) : null}
        </Pressable>
      </View>

      {}
      <View>
        {filtered.map((tk) => {
          const up = tk.changePct >= 0;
          const isFav = !!favorites[tk.sym];
          const isBold = tweaks.variant === 'bold';
          return (
            <Pressable
              key={tk.sym}
              onPress={() => onOpenStock(tk.sym)}
              style={({ pressed }) => [
                styles.row,
                {
                  borderBottomColor: isBold ? 'transparent' : t.hairline,
                  paddingVertical: t.rowPadY,
                  paddingHorizontal: t.rowPadX,
                  minHeight: t.rowMinH,
                  backgroundColor: pressed ? t.surface2 : isBold ? t.surface : 'transparent',
                  borderRadius: isBold ? 14 : 0,
                  borderWidth: isBold ? 1 : 0,
                  borderColor: isBold ? t.hairline : 'transparent',
                  marginHorizontal: isBold ? 12 : 0,
                  marginBottom: isBold ? 4 : 0,
                },
              ]}
            >
              <View
                style={[
                  styles.srIcon,
                  {
                    backgroundColor:
                      tk.market === 'MOEX'
                        ? 'rgba(199,161,78,0.05)'
                        : 'rgba(107,182,255,0.05)',
                    borderColor:
                      tk.market === 'MOEX'
                        ? 'rgba(199,161,78,0.3)'
                        : 'rgba(107,182,255,0.3)',
                  },
                ]}
              >
                <Text
                  style={[
                    styles.srIconTxt,
                    { color: tk.market === 'MOEX' ? '#C7A14E' : '#6BB6FF' },
                  ]}
                >
                  {tk.sym.slice(0, 2)}
                </Text>
              </View>
              <View style={styles.srMeta}>
                <View style={{ flexDirection: 'row', alignItems: 'center', gap: 6 }}>
                  <Text style={[styles.srSym, { color: t.text }]}>{tk.sym}</Text>
                  <View style={[styles.srMkt, { backgroundColor: t.surface2 }]}>
                    <Text style={[styles.srMktTxt, { color: t.textDim }]}>{tk.market}</Text>
                  </View>
                  {isFav ? <IconStarSolid size={11} color={t.textDim} /> : null}
                </View>
                <Text style={[styles.srName, { color: t.textMute }]} numberOfLines={1}>
                  {lang === 'ru' ? tk.nameRu : tk.nameEn}
                </Text>
              </View>
              <View style={styles.srSpark}>
                <Sparkline data={tk.spark} w={56} h={28} color={up ? t.up : t.down} fill />
              </View>
              <View style={styles.srPrice}>
                <Text
                  style={[styles.srPriceV, { color: t.text }]}
                  numberOfLines={1}
                  adjustsFontSizeToFit
                  minimumFontScale={0.82}
                >
                  {fmtNum(tk.price, tk.price > 1000 ? 1 : 2)}
                  <Text style={[styles.srPriceC, { color: t.textDim }]}>
                    {' '}{tk.ccy === 'RUB' ? '₽' : '$'}
                  </Text>
                </Text>
              </View>
              <View
                style={[
                  styles.srChg,
                  {
                    backgroundColor: up ? t.upSoft : t.downSoft,
                  },
                ]}
              >
                <Text style={[styles.srChgPct, { color: up ? t.up : t.down }]}>
                  {fmtPct(tk.changePct)}
                </Text>
                <Text style={[styles.srChgAbs, { color: up ? t.up : t.down }]}>
                  {fmtSigned(tk.change, Math.abs(tk.change) > 100 ? 1 : 2)}
                </Text>
              </View>
            </Pressable>
          );
        })}
        {filtered.length === 0 ? (
          <View style={styles.empty}>
            <Text style={[styles.emptyGlyph, { color: t.textDim }]}>∅</Text>
            <Text style={{ color: t.textMute }}>{T.nothingFound}</Text>
          </View>
        ) : null}
      </View>

      {}
      <BottomSheet open={filterOpen} onClose={() => setFilterOpen(false)} title={T.filters}>
        <View style={{ paddingHorizontal: 4 }}>
          <View style={{ marginBottom: 18 }}>
            <Text style={[styles.fpLbl, { color: t.textMute }]}>{T.priceRange}, ₽</Text>
            <DualRange
              min={0}
              max={10000}
              step={50}
              value={priceRange}
              onChange={setPriceRange}
              fmt={(v) => v.toLocaleString('en-US').replace(/,/g, ' ')}
            />
          </View>
          <View style={{ marginBottom: 18 }}>
            <Text style={[styles.fpLbl, { color: t.textMute }]}>{T.dayChange}, %</Text>
            <DualRange
              min={-10}
              max={10}
              step={0.5}
              value={chgRange}
              onChange={setChgRange}
              fmt={(v) => (v > 0 ? '+' : '') + v.toFixed(1)}
            />
          </View>
          <View style={{ flexDirection: 'row', gap: 10, paddingTop: 8 }}>
            <Pressable
              onPress={() => {
                setPriceRange([0, 10000]);
                setChgRange([-10, 10]);
              }}
              style={[
                styles.btn,
                { flex: 1, borderColor: t.hairline, borderWidth: 1, backgroundColor: 'transparent' },
              ]}
            >
              <Text style={[styles.btnTxt, { color: t.text }]}>{T.reset}</Text>
            </Pressable>
            <Pressable
              onPress={() => setFilterOpen(false)}
              style={[styles.btn, { flex: 1, backgroundColor: t.accent }]}
            >
              <Text style={[styles.btnTxt, { color: t.accentFg }]}>{T.apply}</Text>
            </Pressable>
          </View>
        </View>
      </BottomSheet>
    </ScrollView>
  );
}

function Chip({
  on,
  label,
  onPress,
  ghost,
  icon,
}: {
  on: boolean;
  label: string;
  onPress: () => void;
  ghost?: boolean;
  icon?: React.ReactNode;
}) {
  const { tokens: t } = useTheme();
  return (
    <Pressable
      onPress={onPress}
      style={[
        styles.chip,
        {
          backgroundColor: on ? t.text : ghost ? 'transparent' : t.surface,
          borderColor: on ? t.text : t.hairline,
        },
      ]}
    >
      {icon ? <View style={{ marginRight: 4 }}>{icon}</View> : null}
      <Text
        style={[
          styles.chipTxt,
          { color: on ? t.textInv : t.textMute },
        ]}
      >
        {label}
      </Text>
    </Pressable>
  );
}

function PulseCell({
  label,
  value,
  valColor,
  mono,
  t,
}: {
  label: string;
  value: string;
  valColor?: string;
  mono?: boolean;
  t: ReturnType<typeof useTheme>['tokens'];
}) {
  return (
    <View style={styles.pulseCell}>
      <Text style={[styles.pulseLbl, { color: t.textMute }]}>{label}</Text>
      <Text
        style={[
          styles.pulseVal,
          {
            color: valColor || t.text,
            fontFamily: mono ? FONT_MONO : FONT_BOLD,
          },
        ]}
      >
        {value}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  searchRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
    paddingVertical: 8,
    paddingHorizontal: 14,
    marginHorizontal: 16,
    marginBottom: 14,
    borderRadius: 14,
    borderWidth: 1,
  },
  searchInput: { flex: 1, fontSize: 15, padding: 0, fontFamily: FONT_REGULAR },
  pulse: {
    flexDirection: 'row',
    alignItems: 'stretch',
    marginHorizontal: 16,
    marginBottom: 12,
    paddingVertical: 12,
    borderRadius: 14,
    borderWidth: 1,
    overflow: 'hidden',
  },
  pulseCell: { flex: 1, alignItems: 'center', gap: 2 },
  pulseDiv: { width: 1, marginVertical: 4 },
  pulseLbl: {
    fontSize: 10,
    fontFamily: FONT_BOLD,
    letterSpacing: 0.6,
    textTransform: 'uppercase',
  },
  pulseVal: { fontSize: 16, letterSpacing: -0.16 },
  chipRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    paddingHorizontal: 16,
    paddingBottom: 14,
  },
  chip: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 7,
    paddingHorizontal: 14,
    borderRadius: 100,
    borderWidth: 1,
  },
  chipTxt: { fontSize: 13, fontFamily: FONT_BOLD },
  listHead: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingBottom: 8,
    borderBottomWidth: 1,
  },
  lhSym: { width: 90, flexDirection: 'row', alignItems: 'center', gap: 2 },
  lhSpark: { flex: 1 },
  lhPrice: { width: 86, flexDirection: 'row', justifyContent: 'flex-end', alignItems: 'center', gap: 2 },
  lhChg: { width: 92, flexDirection: 'row', justifyContent: 'flex-end', alignItems: 'center', gap: 2 },
  lhTxt: {
    fontSize: 10,
    fontFamily: FONT_BOLD,
    letterSpacing: 0.2,
    textTransform: 'uppercase',
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
    borderBottomWidth: 1,
  },
  srIcon: {
    width: 36,
    height: 36,
    borderRadius: 10,
    borderWidth: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  srIconTxt: { fontSize: 12, fontFamily: FONT_MONO },
  srMeta: { flex: 1, minWidth: 0 },
  srSym: { fontSize: 14, fontFamily: FONT_BOLD, letterSpacing: -0.07 },
  srMkt: { paddingHorizontal: 5, paddingVertical: 2, borderRadius: 4 },
  srMktTxt: { fontSize: 9, fontFamily: FONT_BOLD, letterSpacing: 0.5 },
  srName: { fontSize: 12, marginTop: 1, fontFamily: FONT_REGULAR },
  srSpark: { width: 56, alignItems: 'center', justifyContent: 'center' },
  srPrice: { width: 86, alignItems: 'flex-end', justifyContent: 'center' },
  srPriceV: { fontSize: 14, fontFamily: FONT_MONO, textAlign: 'right' },
  srPriceC: { fontSize: 11, fontFamily: FONT_REGULAR },
  srChg: {
    width: 92,
    paddingVertical: 4,
    paddingHorizontal: 6,
    borderRadius: 6,
    alignItems: 'flex-end',
  },
  srChgPct: { fontSize: 13, fontFamily: FONT_MONO },
  srChgAbs: { fontSize: 10, opacity: 0.75, fontFamily: FONT_MONO },
  empty: { padding: 60, alignItems: 'center' },
  emptyGlyph: { fontSize: 32, marginBottom: 8 },
  fpLbl: {
    fontSize: 11,
    fontFamily: FONT_BOLD,
    letterSpacing: 0.6,
    textTransform: 'uppercase',
    marginBottom: 8,
  },
  btn: { paddingVertical: 14, paddingHorizontal: 18, borderRadius: 12, alignItems: 'center' },
  btnTxt: { fontSize: 14, fontFamily: FONT_BOLD },
});
