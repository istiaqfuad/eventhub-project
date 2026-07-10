import { createStore } from "zustand/vanilla";
import type { UserResponse } from "../lib/types";

export type AuthUser = {
  id: number;
  email: string;
  roles: string[];
};

export type AuthState = {
  user: AuthUser | null;
  isAuthenticated: boolean;
  hydrated: boolean;
};

export type AuthActions = {
  setUser: (user: AuthUser | null) => void;
  setHydrated: (hydrated: boolean) => void;
  logout: () => void;
};

export type AuthStore = AuthState & AuthActions;

export const defaultInitState: AuthState = {
  user: null,
  isAuthenticated: false,
  hydrated: false,
};

export function toAuthUser(user: UserResponse): AuthUser {
  return {
    id: user.id,
    email: user.email,
    roles: Array.isArray(user.roles) ? [...user.roles] : [],
  };
}

export const createAuthStore = (initState: AuthState = defaultInitState) => {
  return createStore<AuthStore>()((set) => ({
    ...initState,
    setUser: (user) => set(() => ({ user, isAuthenticated: !!user })),
    setHydrated: (hydrated) => set(() => ({ hydrated })),
    logout: () => set(() => ({ user: null, isAuthenticated: false })),
  }));
};
