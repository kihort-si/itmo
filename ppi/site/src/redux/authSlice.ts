import {createSlice, type PayloadAction} from '@reduxjs/toolkit';

export interface User {
  username: string;
}

interface AuthState {
  isAuthenticated: boolean;
  user: User | null;
}

const initialState: AuthState = {
  isAuthenticated: false,
  user: null
};

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    signUp: (state, action): void => {
      state.isAuthenticated = true;
      state.user = action.payload;
    },
    login: (state, action): void => {
      state.isAuthenticated = true;
      state.user = action.payload;
    },
    logout: (state): void => {
      state.isAuthenticated = false;
      state.user = null;
    },
    setUsername: (state, action: PayloadAction<string>): void => {
      if (state.user) {
        state.user.username = action.payload;
      }
    },
  },
});

export const { signUp, login, logout, setUsername } = authSlice.actions;

export default authSlice.reducer;