import { create } from 'zustand';

type DevState = {
  mockMarketData: boolean;
  setMockMarketData: (value: boolean) => void;
};

export const useDevStore = create<DevState>((set) => ({
  mockMarketData: false,
  setMockMarketData: (value) => set({ mockMarketData: value }),
}));
