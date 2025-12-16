import { configureStore } from '@reduxjs/toolkit';
import authReducer from './authSlice';
import goalsReducer from "./goalsSlice";

const store = configureStore({
  reducer: {
    auth: authReducer,
    goals: goalsReducer,
  },
});

export default store;
export type RootState = ReturnType<typeof store.getState>;