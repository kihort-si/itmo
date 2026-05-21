import { create } from 'zustand';
import { WS_QUOTES_URL, WS_RECONNECT_MS } from '../config/api';
import { brokerData } from '../data/repository';
import type { QuoteStreamMessage } from '../api/types';
import { useAuthStore } from './authStore';
import { useDevStore } from './devStore';

export type Quote = {
  symbol: string;
  price: number;
  change: number;
  changePct: number;
  spark: number[];
  updatedAt: number;
};

type QuoteState = {
  quotes: Record<string, Quote>;
  refCounts: Record<string, number>;
  wsConnected: boolean;
  subscribe: (symbol: string) => () => void;
  getQuote: (symbol: string) => Quote | undefined;
  setQuote: (q: Quote) => void;
  _incRef: (symbol: string) => void;
  _decRef: (symbol: string) => void;
};

function seedQuote(symbol: string): Quote {
  const tk = brokerData.findTicker(symbol);
  const s = brokerData.dayStats(tk);
  return {
    symbol: tk.sym,
    price: s.price,
    change: s.change,
    changePct: s.changePct,
    spark: s.spark,
    updatedAt: Date.now(),
  };
}

let ws: WebSocket | null = null;
let reconnectTimer: ReturnType<typeof setTimeout> | null = null;
let mockTimer: ReturnType<typeof setInterval> | null = null;
let useMockStream = false;
let lastJwt = useAuthStore.getState().jwt;

function shouldUseLocalMockQuotes() {
  return useDevStore.getState().mockMarketData;
}

function clearReconnect() {
  if (reconnectTimer) {
    clearTimeout(reconnectTimer);
    reconnectTimer = null;
  }
}

function clearMockTimer() {
  if (mockTimer) {
    clearInterval(mockTimer);
    mockTimer = null;
  }
}

function subscribedSymbols(): string[] {
  const { refCounts } = useQuoteStore.getState();
  return Object.keys(refCounts).filter((s) => refCounts[s] > 0);
}

function sendSubscribe(symbols: string[]) {
  if (!ws || ws.readyState !== WebSocket.OPEN || symbols.length === 0) return;
  const msg: QuoteStreamMessage = { type: 'subscribe', symbols };
  ws.send(JSON.stringify(msg));
}

function startMockStream() {
  if (!shouldUseLocalMockQuotes()) return;
  if (mockTimer) return;
  mockTimer = setInterval(() => {
    const syms = subscribedSymbols();
    if (syms.length === 0) return;
    const { setQuote } = useQuoteStore.getState();
    for (const sym of syms) {
      const prev = useQuoteStore.getState().quotes[sym] ?? seedQuote(sym);
      const jitter = (Math.random() - 0.5) * prev.price * 0.002;
      const price = Math.max(0.01, prev.price + jitter);
      const change = price - (prev.price - prev.change);
      const base = prev.price - prev.change;
      const changePct = base !== 0 ? (change / base) * 100 : 0;
      setQuote({
        ...prev,
        price,
        change,
        changePct,
        updatedAt: Date.now(),
      });
    }
  }, 1200);
}

function stopMockStream() {
  clearMockTimer();
}

function handleQuoteMessage(msg: QuoteStreamMessage) {
  if (msg.type !== 'quote') return;
  const prev = useQuoteStore.getState().quotes[msg.symbol];
  useQuoteStore.getState().setQuote({
    symbol: msg.symbol,
    price: msg.price,
    change: msg.change,
    changePct: msg.changePct,
    spark: msg.spark ?? prev?.spark ?? [],
    updatedAt: Date.now(),
  });
}

function openWebSocket() {
  if (ws && (ws.readyState === WebSocket.OPEN || ws.readyState === WebSocket.CONNECTING)) {
    return;
  }

  const jwt = useAuthStore.getState().jwt;
  const url = jwt ? `${WS_QUOTES_URL}?token=${encodeURIComponent(jwt)}` : WS_QUOTES_URL;

  try {
    ws = new WebSocket(url);
  } catch {
    useMockStream = shouldUseLocalMockQuotes();
    useQuoteStore.setState({ wsConnected: false });
    startMockStream();
    return;
  }

  ws.onopen = () => {
    useMockStream = false;
    useQuoteStore.setState({ wsConnected: true });
    clearReconnect();
    stopMockStream();
    sendSubscribe(subscribedSymbols());
  };

  ws.onmessage = (ev) => {
    try {
      const data = JSON.parse(String(ev.data)) as QuoteStreamMessage;
      handleQuoteMessage(data);
    } catch {
      /* ignore */
    }
  };

  ws.onerror = () => {
    useMockStream = shouldUseLocalMockQuotes();
  };

  ws.onclose = () => {
    ws = null;
    useQuoteStore.setState({ wsConnected: false });
    if (subscribedSymbols().length === 0) return;

    if (useMockStream && shouldUseLocalMockQuotes()) {
      startMockStream();
    }

    clearReconnect();
    reconnectTimer = setTimeout(() => {
      if (subscribedSymbols().length > 0) {
        openWebSocket();
      }
    }, WS_RECONNECT_MS);
  };
}

function closeWebSocket() {
  clearReconnect();
  stopMockStream();
  if (ws) {
    ws.onclose = null;
    ws.close();
    ws = null;
  }
  useQuoteStore.setState({ wsConnected: false });
}

function ensureConnection() {
  if (shouldUseLocalMockQuotes()) {
    useQuoteStore.setState({ wsConnected: false });
    startMockStream();
    return;
  }
  openWebSocket();
}

function releaseConnectionIfIdle() {
  if (subscribedSymbols().length === 0) {
    closeWebSocket();
  }
}

function onSessionChanged() {
  closeWebSocket();
  if (subscribedSymbols().length > 0) {
    ensureConnection();
  }
}

export const useQuoteStore = create<QuoteState>((set, get) => ({
  quotes: {},
  refCounts: {},
  wsConnected: false,

  getQuote: (symbol) => {
    const sym = symbol.toUpperCase();
    return get().quotes[sym] ?? get().quotes[symbol];
  },

  setQuote: (q) => {
    set((s) => ({
      quotes: { ...s.quotes, [q.symbol]: q },
    }));
  },

  _incRef: (symbol) => {
    const sym = symbol.toUpperCase();
    const prev = get().refCounts[sym] ?? 0;
    const next = { ...get().refCounts, [sym]: prev + 1 };
    set({ refCounts: next });
    if (!get().quotes[sym] && shouldUseLocalMockQuotes()) {
      get().setQuote(seedQuote(sym));
    }
    if (prev === 0) {
      ensureConnection();
      if (ws?.readyState === WebSocket.OPEN) {
        sendSubscribe([sym]);
      }
    }
  },

  _decRef: (symbol) => {
    const sym = symbol.toUpperCase();
    const prev = get().refCounts[sym] ?? 0;
    if (prev <= 0) return;
    const count = prev - 1;
    const next = { ...get().refCounts };
    if (count <= 0) {
      delete next[sym];
    } else {
      next[sym] = count;
    }
    set({ refCounts: next });
    releaseConnectionIfIdle();
  },

  subscribe: (symbol) => {
    get()._incRef(symbol);
    return () => get()._decRef(symbol);
  },
}));

useAuthStore.subscribe((state) => {
  if (state.jwt === lastJwt) return;
  lastJwt = state.jwt;
  onSessionChanged();
});

useDevStore.subscribe((state) => {
  if (state.mockMarketData) {
    useMockStream = true;
    const { refCounts, quotes } = useQuoteStore.getState();
    Object.keys(refCounts)
      .filter((sym) => refCounts[sym] > 0 && !quotes[sym])
      .forEach((sym) => useQuoteStore.getState().setQuote(seedQuote(sym)));
    ensureConnection();
    return;
  }

  useMockStream = false;
  stopMockStream();
  useQuoteStore.setState({ quotes: {} });
  if (subscribedSymbols().length > 0) {
    openWebSocket();
  }
});
