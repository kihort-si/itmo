import { useState, useEffect } from 'react';
import { authService, type AccountRole } from '../services/api';

export function useUserRole(): {
  role: AccountRole | null;
  isClient: boolean;
  isStaff: boolean;
  loading: boolean;
} {
  const [role, setRole] = useState<AccountRole | null>(null);
  const [loading, setLoading] = useState(true);

  const loadRole = async () => {
    try {
      const userRole = await authService.getRole();
      setRole(userRole);
    } catch {
      setRole(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadRole();

    let lastPathname = window.location.pathname;
    const checkPathnameChange = () => {
      const currentPathname = window.location.pathname;
      if (currentPathname !== lastPathname) {
        lastPathname = currentPathname;
        loadRole();
      }
    };

    const interval = setInterval(checkPathnameChange, 500);

    const handleStorageChange = (e: StorageEvent) => {
      if (e.key === 'accessToken') {
        loadRole();
      }
    };

    window.addEventListener('popstate', checkPathnameChange);
    window.addEventListener('storage', handleStorageChange);

    return () => {
      clearInterval(interval);
      window.removeEventListener('popstate', checkPathnameChange);
      window.removeEventListener('storage', handleStorageChange);
    };
  }, []);

  const isClient = role === 'CLIENT';
  const isStaff = role !== null && role !== 'CLIENT';

  return { role, isClient, isStaff, loading };
}

