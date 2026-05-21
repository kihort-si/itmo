import React, { useState } from 'react';
import {
  ActivityIndicator,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import { useTheme } from '../theme/useTheme';
import { useI18n } from '../i18n/I18nContext';
import { useAuth } from '../hooks/useAuth';
import { usePortfolioStore } from '../stores/portfolioStore';
import { checkEmailInUseApi, checkUsernameInUseApi } from '../api/authApi';
import { USE_MOCK_API } from '../config/api';
import { TopBar } from '../components/TopBar';
import { IconButton } from '../components/IconButton';
import { IconClose, IconEye } from '../icons/Icons';
import { FONT_BOLD, FONT_REGULAR } from '../theme/themes';

export function AuthScreen({
  onClose,
  onAuthed,
}: {
  onClose: () => void;
  onAuthed: () => void;
}) {
  const { tokens: t } = useTheme();
  const { T, lang } = useI18n();
  const [mode, setMode] = useState<'signin' | 'signup'>('signin');
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [username, setUsername] = useState('');
  const [showPwd, setShowPwd] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const { login, register } = useAuth();

  const valid =
    password.length >= 6 &&
    (mode === 'signin'
      ? email.trim().length > 0
      : name.trim().length >= 3 &&
        username.trim().length >= 2 &&
        email.includes('@') &&
        email.trim().length > 0);

  async function submit() {
    if (!valid || loading) return;
    setLoading(true);
    setError(null);
    try {
      const trimmedEmail = email.trim();
      const trimmedUsername = username.trim();
      const trimmedName = name.trim();

      if (mode === 'signin') {
        await login({ email: trimmedEmail, password });
      } else {
        if (!USE_MOCK_API) {
          const usernameBusy = await checkUsernameInUseApi(trimmedUsername);
          if (usernameBusy.inUse) {
            throw new Error(lang === 'ru' ? 'Username уже занят' : 'Username is already in use');
          }
          const emailBusy = await checkEmailInUseApi(trimmedEmail);
          if (emailBusy.inUse) {
            throw new Error(lang === 'ru' ? 'Email уже занят' : 'Email is already in use');
          }
        }
        await register({
          name: trimmedName,
          email: trimmedEmail,
          password,
          username: trimmedUsername,
        });
      }
      usePortfolioStore.getState().seedFromMockIfEmpty();
      onAuthed();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Auth failed');
    } finally {
      setLoading(false);
    }
  }

  return (
    <ScrollView
      style={{ flex: 1, backgroundColor: t.bg }}
      contentContainerStyle={{ paddingBottom: 40 }}
      keyboardShouldPersistTaps="handled"
    >
      <TopBar
        transparent
        left={<IconButton icon={<IconClose size={22} color={t.text} />} onPress={onClose} />}
        title=""
      />

      <View style={styles.hero}>
        <View style={[styles.logo, { backgroundColor: t.accent }]}>
          <Text style={{ color: t.accentFg, fontSize: 36, fontFamily: FONT_BOLD, letterSpacing: -1.4 }}>
            M
          </Text>
        </View>
        <Text style={[styles.brand, { color: t.text }]}>MyBroker</Text>
        <Text style={[styles.tagline, { color: t.textMute }]}>
          {mode === 'signin' ? T.signedInTagline : T.signedOutTagline}
        </Text>
      </View>

      <View style={[styles.tabs, { backgroundColor: t.surface, borderColor: t.hairline }]}>
        <Pressable
          onPress={() => setMode('signin')}
          style={[
            styles.tab,
            mode === 'signin' && { backgroundColor: t.surface3 },
          ]}
        >
          <Text style={{ color: mode === 'signin' ? t.text : t.textMute, fontFamily: FONT_BOLD, fontSize: 14 }}>
            {T.signIn}
          </Text>
        </Pressable>
        <Pressable
          onPress={() => setMode('signup')}
          style={[
            styles.tab,
            mode === 'signup' && { backgroundColor: t.surface3 },
          ]}
        >
          <Text style={{ color: mode === 'signup' ? t.text : t.textMute, fontFamily: FONT_BOLD, fontSize: 14 }}>
            {T.signUp}
          </Text>
        </Pressable>
      </View>

      <View style={styles.form}>
        {mode === 'signup' ? (
          <Field label={T.fullName} t={t}>
            <TextInput
              value={name}
              onChangeText={setName}
              placeholder={lang === 'ru' ? 'Иванов Иван Иванович' : 'Ivan Ivanov'}
              placeholderTextColor={t.textDim}
              style={[styles.input, { color: t.text, backgroundColor: t.surface, borderColor: t.hairline }]}
            />
          </Field>
        ) : null}
        {mode === 'signup' ? (
          <Field label={T.username} t={t}>
            <TextInput
              value={username}
              onChangeText={setUsername}
              placeholder={lang === 'ru' ? 'трейдер_2026' : 'trader_2026'}
              placeholderTextColor={t.textDim}
              style={[styles.input, { color: t.text, backgroundColor: t.surface, borderColor: t.hairline }]}
            />
          </Field>
        ) : null}
        <Field label={mode === 'signin' ? T.emailOrUsername : T.email} t={t}>
          <TextInput
            value={email}
            onChangeText={setEmail}
            placeholder="user@itmo.ru"
            placeholderTextColor={t.textDim}
            keyboardType={mode === 'signin' ? 'default' : 'email-address'}
            autoCapitalize="none"
            style={[styles.input, { color: t.text, backgroundColor: t.surface, borderColor: t.hairline }]}
          />
        </Field>
        <Field label={T.password} t={t}>
          <View style={{ position: 'relative' }}>
            <TextInput
              value={password}
              onChangeText={setPassword}
              placeholder="••••••••"
              placeholderTextColor={t.textDim}
              secureTextEntry={!showPwd}
              style={[
                styles.input,
                { color: t.text, backgroundColor: t.surface, borderColor: t.hairline, paddingRight: 44 },
              ]}
            />
            <Pressable
              onPress={() => setShowPwd((v) => !v)}
              style={styles.eye}
              hitSlop={6}
            >
              <IconEye size={18} color={t.textMute} />
            </Pressable>
          </View>
        </Field>

        {mode === 'signin' ? (
          <Text style={[styles.forgot, { color: t.accent }]}>{T.forgot}</Text>
        ) : null}

        {error ? (
          <Text style={{ color: t.down, fontSize: 13, marginBottom: 8, textAlign: 'center' }}>{error}</Text>
        ) : null}

        <Pressable
          onPress={() => void submit()}
          disabled={!valid || loading}
          style={[
            styles.btn,
            {
              backgroundColor: t.accent,
              opacity: valid && !loading ? 1 : 0.45,
            },
          ]}
        >
          {loading ? (
            <ActivityIndicator color={t.accentFg} />
          ) : (
            <Text style={{ color: t.accentFg, fontSize: 15, fontFamily: FONT_BOLD }}>
              {mode === 'signin' ? T.signIn : T.signUp}
            </Text>
          )}
        </Pressable>

        <View style={styles.switch}>
          <Text style={{ color: t.textMute, fontSize: 13 }}>
            {mode === 'signin' ? T.noAccount : T.hasAccount}
          </Text>
          <Pressable onPress={() => setMode(mode === 'signin' ? 'signup' : 'signin')}>
            <Text style={{ color: t.accent, fontSize: 13, fontFamily: FONT_BOLD, paddingHorizontal: 4 }}>
              {mode === 'signin' ? T.signUp : T.signIn}
            </Text>
          </Pressable>
        </View>
      </View>

      <Text style={[styles.risk, { color: t.textDim }]}>{T.riskWarning}</Text>
    </ScrollView>
  );
}

function Field({
  label,
  children,
  t,
}: {
  label: string;
  children: React.ReactNode;
  t: ReturnType<typeof useTheme>['tokens'];
}) {
  return (
    <View style={{ marginBottom: 14 }}>
      <Text
        style={{
          fontSize: 11,
          fontFamily: FONT_BOLD,
          letterSpacing: 0.6,
          color: t.textMute,
          textTransform: 'uppercase',
          marginBottom: 8,
        }}
      >
        {label}
      </Text>
      {children}
    </View>
  );
}

const styles = StyleSheet.create({
  hero: { paddingHorizontal: 32, paddingBottom: 28, alignItems: 'center' },
  logo: {
    width: 72,
    height: 72,
    borderRadius: 22,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 16,
  },
  brand: { fontSize: 22, fontFamily: FONT_BOLD, letterSpacing: -0.22 },
  tagline: { fontSize: 13, fontFamily: FONT_REGULAR, marginTop: 6, textAlign: 'center' },
  tabs: {
    flexDirection: 'row',
    marginHorizontal: 16,
    marginBottom: 20,
    padding: 4,
    borderRadius: 12,
    borderWidth: 1,
    gap: 2,
  },
  tab: {
    flex: 1,
    paddingVertical: 11,
    borderRadius: 8,
    alignItems: 'center',
  },
  form: { paddingHorizontal: 16 },
  input: {
    height: 52,
    borderRadius: 14,
    borderWidth: 1,
    paddingHorizontal: 16,
    fontSize: 15,
    fontFamily: FONT_REGULAR,
  },
  eye: {
    position: 'absolute',
    right: 12,
    top: 0,
    height: 52,
    width: 32,
    alignItems: 'center',
    justifyContent: 'center',
  },
  forgot: { textAlign: 'right', fontSize: 13, fontFamily: FONT_BOLD, marginBottom: 14, marginTop: -4 },
  btn: {
    height: 52,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
    marginTop: 6,
  },
  switch: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    paddingVertical: 18,
  },
  risk: { fontSize: 11, padding: 16, textAlign: 'center' },
});
