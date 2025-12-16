import {createSlice, type PayloadAction} from '@reduxjs/toolkit';

interface GoalsState {
  dailyGoal: number;
  dailyRead: number;
  yearlyGoal: number;
  yearlyRead: number;
}

const initialState: GoalsState = {
  dailyGoal: 100,
  dailyRead: 56,
  yearlyGoal: 35,
  yearlyRead: 12,
};

const goalsSlice = createSlice({
  name: 'goals',
  initialState,
  reducers: {
    setDailyGoal: (state, action: PayloadAction<number>) => {
      state.dailyGoal = action.payload;
    },
    setDailyRead: (state, action: PayloadAction<number>) => {
      state.dailyRead = action.payload;
    },
    setYearlyGoal: (state, action: PayloadAction<number>) => {
      state.yearlyGoal = action.payload;
    },
    setYearlyRead: (state, action: PayloadAction<number>) => {
      state.yearlyRead = action.payload;
    },
  },
});

export const { setDailyGoal, setDailyRead, setYearlyGoal, setYearlyRead } = goalsSlice.actions;

export default goalsSlice.reducer;