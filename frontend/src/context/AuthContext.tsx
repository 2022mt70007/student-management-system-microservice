import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import { login as loginApi, setPassword as setPasswordApi } from '../api/auth';
import { clearAuth, loadStoredAuth, saveAuth } from '../api/client';
import type { AuthUser, LoginRequest, SetPasswordRequest, UserRole } from '../types';

interface AuthContextValue {
  user: AuthUser | null;
  isAuthenticated: boolean;
  login: (request: LoginRequest) => Promise<UserRole>;
  completeRegistration: (request: SetPasswordRequest) => Promise<UserRole>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(() => loadStoredAuth());

  const login = useCallback(async (request: LoginRequest) => {
    const response = await loginApi(request);
    if (!response.success || !response.data) {
      throw new Error(response.message || 'Login failed');
    }
    const authUser: AuthUser = {
      token: response.data.token,
      email: response.data.email,
      role: response.data.role,
      profileId: response.data.profileId,
    };
    saveAuth(authUser);
    setUser(authUser);
    return authUser.role;
  }, []);

  const completeRegistration = useCallback(async (request: SetPasswordRequest) => {
    const response = await setPasswordApi(request);
    if (!response.success || !response.data) {
      throw new Error(response.message || 'Registration failed');
    }
    const authUser: AuthUser = {
      token: response.data.token,
      email: response.data.email,
      role: response.data.role,
      profileId: response.data.profileId,
    };
    saveAuth(authUser);
    setUser(authUser);
    return authUser.role;
  }, []);

  const logout = useCallback(() => {
    clearAuth();
    setUser(null);
  }, []);

  const value = useMemo(
    () => ({
      user,
      isAuthenticated: !!user,
      login,
      completeRegistration,
      logout,
    }),
    [user, login, completeRegistration, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
}

export function roleHomePath(role: UserRole): string {
  switch (role) {
    case 'ADMIN':
      return '/admin';
    case 'STUDENT':
      return '/student';
    case 'TEACHER':
      return '/teacher';
    default:
      return '/login';
  }
}
