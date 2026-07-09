import { createStore } from 'zustand/vanilla'

export type AuthState = {
  user: null | { id: string; name: string; email: string; role: string };
  isAuthenticated: boolean;
}

export type AuthActions = {
  setUser: (user: AuthState['user']) => void;
  logout: () => void;
}

export type AuthStore = AuthState & AuthActions

export const defaultInitState: AuthState = {
  user: null,
  isAuthenticated: false,
}

export const createAuthStore = (
  initState: AuthState = defaultInitState,
) => {
  return createStore<AuthStore>()((set) => ({
    ...initState,
    setUser: (user) => set(() => ({ user, isAuthenticated: !!user })),
    logout: () => set(() => ({ user: null, isAuthenticated: false })),
  }))
}
