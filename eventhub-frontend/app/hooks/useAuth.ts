"use client";

import { useEffect } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useRouter, useSearchParams } from "next/navigation";
import apiClient, {
  getAccessToken,
  getProblemDetail,
  logoutRequest,
  setAccessToken,
} from "../lib/api-client";
import { useAuthStore } from "../providers/auth-store-provider";
import { toAuthUser } from "../store/auth-store";
import type { LoginRequest, RegisterRequest, TokenResponse, UserResponse } from "../lib/types";

async function fetchMe(): Promise<UserResponse> {
  const { data } = await apiClient.get<UserResponse>("/users/me");
  return data;
}

export function useSessionRestore() {
  const setUser = useAuthStore((s) => s.setUser);
  const setHydrated = useAuthStore((s) => s.setHydrated);
  const logout = useAuthStore((s) => s.logout);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      const token = getAccessToken();
      if (!token) {
        if (!cancelled) {
          setUser(null);
          setHydrated(true);
        }
        return;
      }
      try {
        const me = await fetchMe();
        if (!cancelled) setUser(toAuthUser(me));
      } catch {
        setAccessToken(null);
        if (!cancelled) logout();
      } finally {
        if (!cancelled) setHydrated(true);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [setUser, setHydrated, logout]);
}

export const useLogin = () => {
  const setUser = useAuthStore((s) => s.setUser);
  const router = useRouter();
  const searchParams = useSearchParams();

  return useMutation({
    mutationFn: async (credentials: LoginRequest) => {
      const { data } = await apiClient.post<TokenResponse>("/auth/login", credentials);
      if (!data.accessToken) {
        throw new Error("Login response missing access token");
      }
      setAccessToken(data.accessToken);
      const me = await fetchMe();
      return me;
    },
    onSuccess: (me) => {
      setUser(toAuthUser(me));
      const redirect = searchParams.get("redirect");
      router.push(redirect && redirect.startsWith("/") ? redirect : "/");
    },
  });
};

export const useRegister = () => {
  const router = useRouter();

  return useMutation({
    mutationFn: async (userData: RegisterRequest) => {
      const { data } = await apiClient.post<UserResponse>("/auth/register", userData);
      return data;
    },
    onSuccess: () => {
      router.push("/login");
    },
  });
};

export const useLogout = () => {
  const logoutAction = useAuthStore((s) => s.logout);
  const queryClient = useQueryClient();
  const router = useRouter();

  return useMutation({
    mutationFn: async () => {
      await logoutRequest();
    },
    onSettled: () => {
      logoutAction();
      queryClient.clear();
      router.push("/login");
    },
  });
};

export { getProblemDetail };
