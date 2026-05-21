import { brokerData } from '../data/repository';
import type {
  AuthResponse,
  LoginRequest,
  PortfolioResponse,
  RegisterRequest,
  TradeRequest,
  TradeResponse,
  UserMe,
} from './types';

const MOCK_JWT = 'mock-jwt';

export function mockRegister(body: RegisterRequest): AuthResponse {
  return {
    token: MOCK_JWT,
    accessToken: MOCK_JWT,
    email: body.email,
    username: body.username,
    name: body.name,
    clntId: 100001,
    roles: ['USER'],
    balance: brokerData.cashRub,
    user: {
      id: 'mock-user',
      userId: 1,
      clntId: 100001,
      email: body.email,
      username: body.username,
      name: body.name,
      roles: ['USER'],
      status: 'ACTIVE',
      balance: brokerData.cashRub,
    },
  };
}

export function mockLogin(body: LoginRequest): AuthResponse {
  return {
    token: MOCK_JWT,
    accessToken: MOCK_JWT,
    email: body.email,
    username: body.email.includes('@') ? body.email.split('@')[0] : body.email,
    name: 'Demo Trader',
    clntId: 100001,
    roles: ['USER'],
    balance: brokerData.cashRub,
    user: {
      id: 'mock-user',
      userId: 1,
      clntId: 100001,
      email: body.email,
      username: body.email.includes('@') ? body.email.split('@')[0] : body.email,
      name: 'Demo Trader',
      roles: ['USER'],
      status: 'ACTIVE',
      balance: brokerData.cashRub,
    },
  };
}

export function mockGetMe(
  email: string,
  username: string | null,
  name: string | null,
  balance: number,
  clntId: number | null
): UserMe {
  return {
    id: 'mock-user',
    userId: 1,
    clntId: clntId ?? 100001,
    email,
    username,
    name,
    roles: ['USER'],
    status: 'ACTIVE',
    balance,
  };
}

export function mockGetPortfolio(): PortfolioResponse {
  return {
    positions: brokerData.portfolioHoldings.map((h) => ({
      instrumentId: h.sym,
      symbol: h.sym,
      quantity: h.qty,
    })),
    cashRub: brokerData.cashRub,
  };
}

export function mockExecuteTrade(body: TradeRequest, side: 'buy' | 'sell'): TradeResponse {
  const tk = brokerData.findTicker(body.instrumentId);
  const stats = brokerData.dayStats(tk);
  return {
    id: `mock-${Date.now()}`,
    instrumentId: body.instrumentId,
    symbol: tk.sym,
    side,
    quantity: body.quantity,
    price: stats.price,
    currency: tk.ccy,
    executedAt: Date.now(),
  };
}
