
import React, { useCallback, useEffect, useState } from 'react';
import {
  NavigationContainer,
  NavigatorScreenParams,
  useNavigation,
} from '@react-navigation/native';
import {
  createBottomTabNavigator,
  BottomTabBarProps,
} from '@react-navigation/bottom-tabs';
import {
  createNativeStackNavigator,
  NativeStackScreenProps,
} from '@react-navigation/native-stack';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import {
  ExchangeScreen,
  StockScreen,
  TradeScreen,
  PortfolioScreen,
  HistoryScreen,
  ProfileScreen,
  AuthScreen,
} from '../screens';
import { useTheme } from '../theme/useTheme';
import { useAuth } from '../hooks/useAuth';
import { useI18n } from '../i18n/I18nContext';
import { IconBriefcase, IconChartLine, IconUser } from '../icons/Icons';
import { FONT_BOLD } from '../theme/themes';

type AppState = {
  favorites: Record<string, boolean>;
  toggleFavorite: (sym: string) => void;
};

const AppCtx = React.createContext<AppState | null>(null);

function useAppState(): AppState {
  const v = React.useContext(AppCtx);
  if (!v) throw new Error('AppState missing');
  return v;
}

type TabParamList = {
  Exchange: undefined;
  Portfolio: undefined;
  Profile: undefined;
};

export type RootStackParamList = {
  Tabs: NavigatorScreenParams<TabParamList>;
  Stock: { sym: string };
  Trade: { sym: string; side: 'buy' | 'sell' };
  History: undefined;
  Auth: undefined;
};

const Tab = createBottomTabNavigator<TabParamList>();
const Stack = createNativeStackNavigator<RootStackParamList>();

function BottomTabBar({ state, navigation }: BottomTabBarProps) {
  const { tokens: t } = useTheme();
  const { T } = useI18n();

  const tabs = [
    { name: 'Exchange', label: T.tabExchange, icon: IconChartLine },
    { name: 'Portfolio', label: T.tabPortfolio, icon: IconBriefcase },
    { name: 'Profile', label: T.tabProfile, icon: IconUser },
  ];

  return (
    <View style={[styles.bar, { backgroundColor: t.surface, borderTopColor: t.hairline }]}>
      {tabs.map((tab, i) => {
        const focused = state.index === i;
        const Ico = tab.icon;
        return (
          <Pressable
            key={tab.name}
            onPress={() => {
              const ev = navigation.emit({
                type: 'tabPress',
                target: state.routes[i].key,
                canPreventDefault: true,
              });
              if (!focused && !ev.defaultPrevented) {
                navigation.navigate(state.routes[i].name as never);
              }
            }}
            style={styles.tab}
          >
            <View
              style={[
                styles.pill,
                focused && { backgroundColor: t.accentSoft },
              ]}
            >
              <Ico size={22} color={focused ? t.accent : t.textMute} />
            </View>
            <Text
              style={[
                styles.label,
                {
                  color: focused ? t.text : t.textMute,
                  fontFamily: FONT_BOLD,
                },
              ]}
            >
              {tab.label}
            </Text>
          </Pressable>
        );
      })}
    </View>
  );
}

function ExchangeTab() {
  const navigation = useNavigation<any>();
  const { favorites, toggleFavorite } = useAppState();
  return (
    <ExchangeScreen
      onOpenStock={(sym) => navigation.navigate('Stock', { sym })}
      favorites={favorites}
      toggleFavorite={toggleFavorite}
    />
  );
}

function PortfolioTab() {
  const navigation = useNavigation<any>();
  const { isAuthed } = useAuth();
  return (
    <PortfolioScreen
      isAuthed={isAuthed}
      onSignIn={() => navigation.navigate('Auth')}
      onOpenStock={(sym) => navigation.navigate('Stock', { sym })}
      onOpenHistory={() => navigation.navigate('History')}
    />
  );
}

function ProfileTab() {
  const navigation = useNavigation<any>();
  const { isAuthed, logout } = useAuth();
  return (
    <ProfileScreen
      isAuthed={isAuthed}
      onSignIn={() => navigation.navigate('Auth')}
      onSignOut={() => void logout()}
    />
  );
}

function MainTabs() {
  return (
    <Tab.Navigator
      tabBar={(props) => <BottomTabBar {...props} />}
      screenOptions={{ headerShown: false }}
    >
      <Tab.Screen name="Exchange" component={ExchangeTab} />
      <Tab.Screen name="Portfolio" component={PortfolioTab} />
      <Tab.Screen name="Profile" component={ProfileTab} />
    </Tab.Navigator>
  );
}

function StockRoute({ route, navigation }: NativeStackScreenProps<RootStackParamList, 'Stock'>) {
  const { favorites, toggleFavorite } = useAppState();
  return (
    <StockScreen
      sym={route.params.sym}
      onBack={() => navigation.goBack()}
      onTrade={(sym, side) => navigation.navigate('Trade', { sym, side })}
      favorites={favorites}
      toggleFavorite={toggleFavorite}
    />
  );
}

function TradeRoute({ route, navigation }: NativeStackScreenProps<RootStackParamList, 'Trade'>) {
  const { isAuthed } = useAuth();
  return (
    <TradeScreen
      sym={route.params.sym}
      initialSide={route.params.side}
      onBack={() => navigation.goBack()}
      onClose={() => navigation.popToTop()}
      onPlaced={() => navigation.popToTop()}
      isAuthed={isAuthed}
      onSignIn={() => navigation.navigate('Auth')}
    />
  );
}

function HistoryRoute({ navigation }: NativeStackScreenProps<RootStackParamList, 'History'>) {
  return <HistoryScreen onBack={() => navigation.goBack()} />;
}

function AuthRoute({ navigation }: NativeStackScreenProps<RootStackParamList, 'Auth'>) {
  return (
    <AuthScreen
      onClose={() => navigation.goBack()}
      onAuthed={() => navigation.popToTop()}
    />
  );
}

export function AppNavigator() {
  const { tokens: t } = useTheme();
  const { hydrated, isAuthed, refreshMe } = useAuth();
  const [favorites, setFavorites] = useState<Record<string, boolean>>({
    SBER: true,
    AAPL: true,
    GAZP: true,
    NVDA: true,
  });

  const toggleFavorite = useCallback((sym: string) => {
    setFavorites((f) => ({ ...f, [sym]: !f[sym] }));
  }, []);

  useEffect(() => {
    if (!hydrated || !isAuthed) return;
    void refreshMe();
  }, [hydrated, isAuthed, refreshMe]);

  return (
    <AppCtx.Provider value={{ favorites, toggleFavorite }}>
      <NavigationContainer
        theme={{
          dark: t.bg === '#07090C' || t.bg === '#04080F',
          colors: {
            primary: t.accent,
            background: t.bg,
            card: t.surface,
            text: t.text,
            border: t.hairline,
            notification: t.accent,
          },
        }}
      >
        <Stack.Navigator
          screenOptions={{
            headerShown: false,
            contentStyle: { backgroundColor: t.bg },
          }}
        >
          <Stack.Screen name="Tabs" component={MainTabs} />
          <Stack.Screen name="Stock" component={StockRoute} />
          <Stack.Screen name="Trade" component={TradeRoute} />
          <Stack.Screen name="History" component={HistoryRoute} />
          <Stack.Screen
            name="Auth"
            component={AuthRoute}
            options={{ presentation: 'modal' }}
          />
        </Stack.Navigator>
      </NavigationContainer>
    </AppCtx.Provider>
  );
}

const styles = StyleSheet.create({
  bar: {
    flexDirection: 'row',
    height: 78,
    paddingTop: 8,
    paddingBottom: 16,
    paddingHorizontal: 4,
    gap: 2,
    borderTopWidth: 1,
  },
  tab: { flex: 1, alignItems: 'center', justifyContent: 'center', gap: 4 },
  pill: {
    width: 56,
    height: 32,
    borderRadius: 18,
    alignItems: 'center',
    justifyContent: 'center',
  },
  label: { fontSize: 11, letterSpacing: 0.2 },
});
