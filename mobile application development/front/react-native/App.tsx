import React, { useEffect } from 'react';
import { StyleSheet, View } from 'react-native';
import { StatusBar } from 'expo-status-bar';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { ThemeProvider, useTheme } from './src/theme/useTheme';
import { AppNavigator } from './src/navigation/AppNavigator';
import { TweaksPanel } from './src/components/TweaksPanel';
import { useAppFonts } from './src/theme/fonts';

import { usePortfolioStore } from './src/stores/portfolioStore';
import { useAuthStore } from './src/stores/authStore';

function Shell() {
  const { tokens: t, tweaks } = useTheme();
  return (
    <View style={[styles.root, { backgroundColor: t.bg }]}>
      <StatusBar style={tweaks.theme === 'dark' ? 'light' : 'dark'} />
      <AppNavigator />
      <TweaksPanel />
    </View>
  );
}

export default function App() {
  useAppFonts();

  useEffect(() => {
    const unsub = useAuthStore.persist.onFinishHydration(() => {
      if (useAuthStore.getState().jwt) {
        usePortfolioStore.getState().seedFromMockIfEmpty();
      }
    });
    return unsub;
  }, []);

  return (
    <SafeAreaProvider>
      <ThemeProvider>
        <Shell />
      </ThemeProvider>
    </SafeAreaProvider>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1 },
});
