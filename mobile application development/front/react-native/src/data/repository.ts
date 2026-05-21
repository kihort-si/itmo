import {
  ACTIVE_ORDERS,
  CASH_RUB,
  FX,
  HISTORY,
  PORTFOLIO_HOLDINGS,
  TICKERS,
  ActiveOrder,
  Ccy,
  DayStats,
  HistoryItem,
  Holding,
  MarketIndex,
  Orderbook,
  Ticker,
  Tf,
  UserProfile,
  dayStats,
  findTicker,
  genCandles,
  genOrderbook,
} from './market';

export type {
  ActiveOrder,
  Ccy,
  DayStats,
  HistoryItem,
  HistoryStatus,
  Holding,
  MarketIndex,
  Orderbook,
  Ticker,
  Tf,
  UserProfile,
} from './market';

export interface BrokerRepository {
  readonly tickers: Ticker[];
  readonly portfolioHoldings: Holding[];
  readonly cashRub: number;
  readonly history: HistoryItem[];
  readonly activeOrders: ActiveOrder[];
  readonly user: UserProfile;
  readonly indices: MarketIndex[];
  readonly fx: typeof FX;
  findTicker(sym: string): Ticker;
  dayStats(t: Ticker): DayStats;
  genCandles(t: Ticker, tf?: Tf, n?: number): ReturnType<typeof genCandles>;
  genOrderbook(t: Ticker, levels?: number): Orderbook;
}

const mockRepository: BrokerRepository = {
  tickers: TICKERS,
  portfolioHoldings: PORTFOLIO_HOLDINGS,
  cashRub: CASH_RUB,
  history: HISTORY,
  activeOrders: ACTIVE_ORDERS,
  user: {
    initials: 'МА',
    nameRu: 'Михаил Ахметов',
    nameEn: 'Mikhail Akhmetov',
    email: 'm.akhmetov@itmo.ru',
    memberSinceRu: '12 окт 2024',
    memberSinceEn: 'Oct 12, 2024',
    ordersCount: '183',
    streak: '14d',
  },
  indices: [
    { label: 'MOEX', value: '3 248,17' },
    { label: 'NASDAQ', value: '21 184,3' },
  ],
  fx: FX,
  findTicker,
  dayStats,
  genCandles,
  genOrderbook,
};

export const brokerData: BrokerRepository = mockRepository;
