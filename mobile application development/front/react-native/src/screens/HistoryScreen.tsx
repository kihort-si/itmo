import React, { useMemo } from 'react';
import { ScrollView, StyleSheet, Text, View } from 'react-native';
import { useTheme } from '../theme/useTheme';
import { useI18n } from '../i18n/I18nContext';
import { brokerData, HistoryItem, HistoryStatus } from '../data/repository';
import { fmtMoney } from '../format';
import { TopBar } from '../components/TopBar';
import { IconButton } from '../components/IconButton';
import { IconBack, IconClose, IconDown, IconUp } from '../icons/Icons';
import { FONT_BOLD, FONT_MONO, FONT_REGULAR } from '../theme/themes';

export function HistoryScreen({ onBack }: { onBack: () => void }) {
  const { tokens: t } = useTheme();
  const { T, lang } = useI18n();

  const groups = useMemo(() => {
    const map = new Map<string, HistoryItem[]>();
    brokerData.history.forEach((h) => {
      const d = new Date(h.t);
      const key = d.toISOString().slice(0, 10);
      if (!map.has(key)) map.set(key, []);
      map.get(key)!.push(h);
    });
    return Array.from(map.entries());
  }, []);

  function dayLabel(key: string) {
    const todayKey = new Date(Date.UTC(2026, 4, 8)).toISOString().slice(0, 10);
    const yestKey = new Date(Date.UTC(2026, 4, 7)).toISOString().slice(0, 10);
    if (key === todayKey) return lang === 'ru' ? 'Сегодня' : 'Today';
    if (key === yestKey) return lang === 'ru' ? 'Вчера' : 'Yesterday';
    const d = new Date(key);
    const months = lang === 'ru'
      ? ['января','февраля','марта','апреля','мая','июня','июля','августа','сентября','октября','ноября','декабря']
      : ['January','February','March','April','May','June','July','August','September','October','November','December'];
    return `${d.getUTCDate()} ${months[d.getUTCMonth()]}`;
  }
  function timeLabel(ts: number) {
    const d = new Date(ts);
    const hh = String(d.getUTCHours()).padStart(2, '0');
    const mm = String(d.getUTCMinutes()).padStart(2, '0');
    return `${hh}:${mm}`;
  }
  function statusLabel(s: HistoryStatus) {
    return {
      filled: T.statusFilled,
      partial: T.statusPartial,
      cancelled: T.statusCancelled,
      rejected: T.statusRejected,
      pending: T.statusPending,
    }[s];
  }
  function statusColors(s: HistoryStatus): { bg: string; fg: string } {
    switch (s) {
      case 'filled': return { bg: t.upSoft, fg: t.up };
      case 'partial': return { bg: t.accentSoft, fg: t.accent };
      case 'cancelled': return { bg: t.surface2, fg: t.textMute };
      case 'rejected': return { bg: t.downSoft, fg: t.down };
      case 'pending': return { bg: t.accentSoft, fg: t.accent };
    }
  }

  return (
    <ScrollView
      style={{ flex: 1, backgroundColor: t.bg }}
      contentContainerStyle={{ paddingBottom: 24 }}
    >
      <TopBar
        left={<IconButton icon={<IconBack size={22} color={t.text} />} onPress={onBack} />}
        title={T.historyTitle}
      />

      {groups.map(([key, items]) => (
        <View key={key} style={{ marginBottom: 8 }}>
          <View style={[styles.dayHead, { backgroundColor: t.bg, borderBottomColor: t.hairline }]}>
            <Text style={[styles.dayTxt, { color: t.textMute }]}>{dayLabel(key)}</Text>
          </View>
          {items.map((op) => {
            const isBuy = op.type === 'buy';
            const isSell = op.type === 'sell';
            const isFail = op.status === 'cancelled' || op.status === 'rejected';
            const iconBg = isBuy ? t.upSoft : isSell ? t.downSoft : t.surface2;
            const iconFg = isBuy ? t.up : isSell ? t.down : t.textMute;
            const statusColor = statusColors(op.status);
            return (
              <View
                key={op.id}
                style={[styles.row, { borderBottomColor: t.hairline }]}
              >
                <View style={[styles.icon, { backgroundColor: iconBg }]}>
                  {isBuy ? (
                    <IconUp size={16} color={iconFg} />
                  ) : isSell ? (
                    <IconDown size={16} color={iconFg} />
                  ) : (
                    <IconClose size={16} color={iconFg} />
                  )}
                </View>
                <View style={{ flex: 1, minWidth: 0 }}>
                  <View style={styles.line1}>
                    <Text style={{ color: t.text, fontSize: 14, fontFamily: FONT_BOLD }}>
                      {op.type === 'buy'
                        ? T.typeBuy
                        : op.type === 'sell'
                        ? T.typeSell
                        : op.type === 'cancel'
                        ? T.typeCancel
                        : T.typeReject}
                    </Text>
                    <View style={[styles.symBadge, { backgroundColor: t.surface2 }]}>
                      <Text style={{ color: t.textMute, fontSize: 12, fontFamily: FONT_BOLD }}>
                        {op.sym}
                      </Text>
                    </View>
                  </View>
                  <View style={styles.line2}>
                    <Text style={{ color: t.text, fontSize: 13, fontFamily: FONT_MONO }}>
                      {op.qty}
                    </Text>
                    <Text style={{ color: t.textMute, fontSize: 13 }}>×</Text>
                    <Text style={{ color: t.text, fontSize: 13, fontFamily: FONT_MONO }}>
                      {fmtMoney(op.price, op.currency)}
                    </Text>
                    <Text
                      style={{
                        color: t.textDim,
                        fontSize: 11,
                        fontFamily: FONT_MONO,
                        marginLeft: 'auto',
                      }}
                    >
                      {timeLabel(op.t)}
                    </Text>
                  </View>
                  {op.reason ? (
                    <Text style={{ color: t.textMute, fontSize: 11, marginTop: 4 }}>
                      {op.reason}
                    </Text>
                  ) : null}
                  {op.status === 'partial' && op.filledQty != null ? (
                    <Text style={{ color: t.accent, fontSize: 11, marginTop: 4 }}>
                      {T.statusPartial}: {op.filledQty}/{op.qty}
                    </Text>
                  ) : null}
                </View>
                <View style={[styles.status, { backgroundColor: statusColor.bg }]}>
                  <Text
                    style={{
                      color: statusColor.fg,
                      fontSize: 10,
                      fontFamily: FONT_BOLD,
                      letterSpacing: 0.4,
                      textTransform: 'uppercase',
                    }}
                  >
                    {statusLabel(op.status)}
                  </Text>
                </View>
              </View>
            );
          })}
        </View>
      ))}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  dayHead: {
    paddingVertical: 8,
    paddingHorizontal: 20,
    borderBottomWidth: 1,
  },
  dayTxt: { fontSize: 11, fontFamily: FONT_BOLD, letterSpacing: 0.6, textTransform: 'uppercase' },
  row: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    gap: 12,
    paddingVertical: 14,
    paddingHorizontal: 20,
    borderBottomWidth: 1,
  },
  icon: {
    width: 32,
    height: 32,
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
    marginTop: 1,
  },
  line1: { flexDirection: 'row', alignItems: 'baseline', gap: 8 },
  symBadge: { paddingHorizontal: 6, paddingVertical: 1, borderRadius: 4 },
  line2: { flexDirection: 'row', alignItems: 'baseline', gap: 6, marginTop: 4 },
  status: {
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 5,
    marginTop: 2,
  },
});
