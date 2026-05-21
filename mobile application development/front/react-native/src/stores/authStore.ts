import AsyncStorage from '@react-native-async-storage/async-storage';
import { create } from 'zustand';
import { createJSONStorage, persist } from 'zustand/middleware';
import {
  loginApi,
  logoutApi,
  registerApi,
} from '../api/authApi';
import { isNetworkOrMockError, setTokenGetter } from '../api/client';
import { mockGetMe, mockLogin, mockRegister } from '../api/mockApi';
import { getMeApi } from '../api/userApi';
import type { AuthResponse, LoginRequest, RegisterRequest } from '../api/types';
import { brokerData } from '../data/repository';

type AuthState = {
  jwt: string | null;
  email: string | null;
  username: string | null;
  name: string | null;
  clntId: number | null;
  roles: string[];
  balance: number;
  hydrated: boolean;
  isAuthed: () => boolean;
  login: (body: LoginRequest) => Promise<void>;
  register: (body: RegisterRequest) => Promise<void>;
  logout: () => Promise<void>;
  refreshMe: () => Promise<void>;
  setBalance: (balance: number) => void;
  setHydrated: (v: boolean) => void;
};

function applyAuthResult(set: (partial: Partial<AuthState>) => void, res: AuthResponse) {
  const user = res.user;
  set({
    jwt: res.accessToken ?? res.token,
    email: user?.email ?? res.email,
    username: user?.username ?? res.username ?? null,
    name: user?.name ?? res.name ?? null,
    clntId: user?.clntId ?? res.clntId ?? null,
    roles: user?.roles ?? res.roles ?? [],
    balance: user?.balance ?? res.balance,
  });
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      jwt: null,
      email: null,
      username: null,
      name: null,
      clntId: null,
      roles: [],
      balance: brokerData.cashRub,
      hydrated: false,

      isAuthed: () => !!get().jwt,

      setHydrated: (v) => set({ hydrated: v }),

      setBalance: (balance) => set({ balance }),

      login: async (body) => {
        try {
          const res = await loginApi(body);
          applyAuthResult(set, res);
        } catch (e) {
          if (!isNetworkOrMockError(e)) throw e;
          const res = mockLogin(body);
          applyAuthResult(set, res);
        }
      },

      register: async (body) => {
        try {
          const res = await registerApi(body);
          applyAuthResult(set, res);
        } catch (e) {
          if (!isNetworkOrMockError(e)) throw e;
          const res = mockRegister(body);
          applyAuthResult(set, res);
        }
      },

      logout: async () => {
        const jwt = get().jwt;
        if (jwt) {
          try {
            await logoutApi(jwt);
          } catch {
            /* ignore */
          }
        }
        set({
          jwt: null,
          email: null,
          username: null,
          name: null,
          clntId: null,
          roles: [],
          balance: brokerData.cashRub,
        });
      },

      refreshMe: async () => {
        const { jwt, email, username, name, balance, clntId } = get();
        if (!jwt) return;
        try {
          const me = await getMeApi();
          set({
            email: me.email,
            username: me.username ?? null,
            name: me.name ?? null,
            clntId: me.clntId ?? null,
            roles: me.roles ?? [],
            balance: me.balance,
          });
        } catch (e) {
          if (!isNetworkOrMockError(e)) throw e;
          if (email) {
            const mockMe = mockGetMe(email, username, name, balance, clntId);
            set({
              email: mockMe.email,
              username: mockMe.username ?? null,
              name: mockMe.name ?? null,
              clntId: mockMe.clntId ?? null,
              roles: mockMe.roles ?? [],
              balance: mockMe.balance,
            });
          }
        }
      },
    }),
    {
      name: 'auth-store',
      storage: createJSONStorage(() => AsyncStorage),
      partialize: (s) => ({
        jwt: s.jwt,
        email: s.email,
        username: s.username,
        name: s.name,
        clntId: s.clntId,
        roles: s.roles,
        balance: s.balance,
      }),
      onRehydrateStorage: () => (state) => {
        state?.setHydrated(true);
      },
    }
  )
);

setTokenGetter(() => useAuthStore.getState().jwt);
