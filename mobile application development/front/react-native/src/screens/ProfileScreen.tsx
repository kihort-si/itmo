import React, { ReactNode } from 'react';
import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { useTheme } from '../theme/useTheme';
import { useI18n } from '../i18n/I18nContext';
import { TopBar } from '../components/TopBar';
import { IconButton } from '../components/IconButton';
import { Switch } from '../components/Switch';
import {
  IconBell,
  IconBriefcase,
  IconLogout,
  IconRight,
  IconShield,
  IconSignal,
  IconUser,
} from '../icons/Icons';
import { FONT_BOLD, FONT_MONO, FONT_REGULAR } from '../theme/themes';
import { brokerData } from '../data/repository';
import { useAuth } from '../hooks/useAuth';

export function ProfileScreen({
  isAuthed,
  onSignIn,
  onSignOut,
}: {
  isAuthed: boolean;
  onSignIn: () => void;
  onSignOut: () => void;
}) {
  const { tokens: t } = useTheme();
  const { T, lang } = useI18n();
  const user = brokerData.user;
  const { email: authEmail, username: authUsername, name: authName, clntId } = useAuth();

  if (!isAuthed) {
    return (
      <View style={{ flex: 1, backgroundColor: t.bg }}>
        <TopBar large title={T.profile} />
        <View style={styles.prompt}>
          <View style={[styles.glyph, { backgroundColor: t.accentSoft }]}>
            <IconUser size={48} color={t.accent} />
          </View>
          <Text style={[styles.promptTitle, { color: t.text }]}>{T.signInTitle}</Text>
          <Text style={[styles.promptSub, { color: t.textMute }]}>{T.signInSub}</Text>
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

  const displayName = authName?.trim() || (lang === 'ru' ? user.nameRu : user.nameEn);
  const displayUsername = authUsername?.trim() || 'trader';
  const displayEmail = authEmail ?? user.email;
  const displayInitials = displayName
    .split(' ')
    .filter((part) => part.length > 0)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase() ?? '')
    .join('') || displayUsername.slice(0, 1).toUpperCase();

  return (
    <ScrollView
      style={{ flex: 1, backgroundColor: t.bg }}
      contentContainerStyle={{ paddingBottom: 100 }}
    >
      <TopBar
        large
        title={T.profile}
        right={<IconButton icon={<IconBell size={22} color={t.text} />} />}
      />

      <View style={[styles.card, { backgroundColor: t.surface, borderColor: t.hairline }]}>
        <View style={[styles.avatar, { backgroundColor: t.accent }]}>
          <Text style={{ color: t.accentFg, fontSize: 26, fontFamily: FONT_BOLD, letterSpacing: 0.5 }}>
            {displayInitials}
          </Text>
        </View>
        <Text style={[styles.name, { color: t.text }]}>{displayName}</Text>
        <Text style={[styles.handle, { color: t.textMute }]}>@{displayUsername}</Text>
        <Text style={[styles.email, { color: t.textMute }]}>{displayEmail}</Text>
        <View style={[styles.stats, { borderTopColor: t.hairline }]}>
          <Stat label={T.memberSince} value={lang === 'ru' ? user.memberSinceRu : user.memberSinceEn} t={t} />
          <View style={[styles.statDiv, { backgroundColor: t.hairline }]} />
          <Stat label="ID" value={clntId != null ? String(clntId) : '-'} t={t} />
          <View style={[styles.statDiv, { backgroundColor: t.hairline }]} />
          <Stat label={T.streak} value={user.streak} t={t} />
        </View>
      </View>

      <View style={styles.secHead}>
        <Text style={[styles.secTxt, { color: t.textMute }]}>{T.settings}</Text>
      </View>
      <View style={{ paddingHorizontal: 16 }}>
        <SettingsRow
          icon={<IconBell size={20} color={t.textMute} />}
          label={T.notifications}
          trail={<Switch defaultOn />}
          t={t}
          first
        />
        <SettingsRow
          icon={<IconShield size={20} color={t.textMute} />}
          label={T.biometry}
          trail={<Switch defaultOn />}
          t={t}
        />
        <SettingsRow
          icon={<IconSignal size={20} color={t.textMute} />}
          label={T.tradeSounds}
          trail={<Switch />}
          t={t}
          last
        />
      </View>

      <View style={styles.secHead}>
        <Text style={[styles.secTxt, { color: t.textMute }]}>{T.support}</Text>
      </View>
      <View style={{ paddingHorizontal: 16 }}>
        <SettingsRow
          icon={<IconBriefcase size={20} color={t.textMute} />}
          label={T.termsFees}
          trail={<IconRight size={18} color={t.textDim} />}
          t={t}
          first
        />
        <SettingsRow
          icon={<IconShield size={20} color={t.textMute} />}
          label={T.privacy}
          trail={<IconRight size={18} color={t.textDim} />}
          t={t}
          last
        />
      </View>

      <Pressable
        onPress={onSignOut}
        style={[styles.signOut, { borderColor: t.down }]}
      >
        <IconLogout size={18} color={t.down} />
        <Text style={{ color: t.down, fontFamily: FONT_BOLD, fontSize: 14 }}>{T.signOut}</Text>
      </Pressable>

      <Text style={[styles.risk, { color: t.textDim }]}>
        {T.riskWarning} v1.0.4 · build 2026.05
      </Text>
    </ScrollView>
  );
}

function Stat({
  label,
  value,
  t,
}: {
  label: string;
  value: string;
  t: ReturnType<typeof useTheme>['tokens'];
}) {
  return (
    <View style={{ flex: 1, alignItems: 'center' }}>
      <Text style={{ fontSize: 10, fontFamily: FONT_BOLD, letterSpacing: 0.6, color: t.textMute, textTransform: 'uppercase' }}>
        {label}
      </Text>
      <Text style={{ fontSize: 14, fontFamily: FONT_MONO, color: t.text, marginTop: 4 }}>
        {value}
      </Text>
    </View>
  );
}

function SettingsRow({
  icon,
  label,
  trail,
  t,
  first,
  last,
}: {
  icon: ReactNode;
  label: string;
  trail: ReactNode;
  t: ReturnType<typeof useTheme>['tokens'];
  first?: boolean;
  last?: boolean;
}) {
  return (
    <View
      style={[
        styles.sRow,
        {
          backgroundColor: t.surface,
          borderColor: t.hairline,
          borderTopLeftRadius: first ? 14 : 0,
          borderTopRightRadius: first ? 14 : 0,
          borderBottomLeftRadius: last ? 14 : 0,
          borderBottomRightRadius: last ? 14 : 0,
          borderTopWidth: first ? 1 : 0,
        },
      ]}
    >
      <View style={[styles.sGlyph, { backgroundColor: t.surface2 }]}>{icon}</View>
      <Text style={{ flex: 1, fontSize: 14, color: t.text, fontFamily: FONT_REGULAR }}>
        {label}
      </Text>
      <View>{trail}</View>
    </View>
  );
}

const styles = StyleSheet.create({
  prompt: { flex: 1, paddingHorizontal: 32, alignItems: 'center', justifyContent: 'center' },
  glyph: {
    width: 96,
    height: 96,
    borderRadius: 28,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 24,
  },
  promptTitle: { fontSize: 22, fontFamily: FONT_BOLD, marginBottom: 8 },
  promptSub: { fontSize: 14, textAlign: 'center', marginBottom: 28, maxWidth: 280, lineHeight: 20 },
  btn: {
    paddingVertical: 14,
    paddingHorizontal: 18,
    borderRadius: 12,
    alignItems: 'center',
    minHeight: 52,
    justifyContent: 'center',
  },
  btnTxt: { fontSize: 15, fontFamily: FONT_BOLD },
  card: {
    marginHorizontal: 16,
    marginBottom: 8,
    padding: 20,
    borderRadius: 18,
    borderWidth: 1,
    alignItems: 'center',
  },
  avatar: {
    width: 72,
    height: 72,
    borderRadius: 36,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 12,
  },
  name: { fontSize: 18, fontFamily: FONT_BOLD },
  handle: { fontSize: 12, fontFamily: FONT_MONO, marginTop: 2 },
  email: { fontSize: 12, fontFamily: FONT_MONO, marginTop: 2 },
  stats: {
    flexDirection: 'row',
    width: '100%',
    paddingTop: 16,
    marginTop: 18,
    borderTopWidth: 1,
  },
  statDiv: { width: 1 },
  secHead: { paddingHorizontal: 20, paddingTop: 18, paddingBottom: 10 },
  secTxt: {
    fontSize: 12,
    fontFamily: FONT_BOLD,
    letterSpacing: 0.6,
    textTransform: 'uppercase',
  },
  sRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 14,
    paddingHorizontal: 14,
    paddingVertical: 12,
    borderLeftWidth: 1,
    borderRightWidth: 1,
    borderBottomWidth: 1,
  },
  sGlyph: {
    width: 32,
    height: 32,
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
  },
  signOut: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    marginHorizontal: 16,
    marginTop: 18,
    marginBottom: 8,
    padding: 14,
    borderRadius: 14,
    borderWidth: 1,
  },
  risk: { fontSize: 11, padding: 16, textAlign: 'center', lineHeight: 16 },
});
