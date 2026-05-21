import { useAuthStore } from '../stores/authStore';

export function useAuth() {
  const jwt = useAuthStore((s) => s.jwt);
  const email = useAuthStore((s) => s.email);
  const username = useAuthStore((s) => s.username);
  const name = useAuthStore((s) => s.name);
  const clntId = useAuthStore((s) => s.clntId);
  const roles = useAuthStore((s) => s.roles);
  const balance = useAuthStore((s) => s.balance);
  const hydrated = useAuthStore((s) => s.hydrated);
  const login = useAuthStore((s) => s.login);
  const register = useAuthStore((s) => s.register);
  const logout = useAuthStore((s) => s.logout);
  const refreshMe = useAuthStore((s) => s.refreshMe);

  return {
    jwt,
    email,
    username,
    name,
    clntId,
    roles,
    balance,
    hydrated,
    isAuthed: !!jwt,
    login,
    register,
    logout,
    refreshMe,
  };
}
