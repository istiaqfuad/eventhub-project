import { useMutation, useQueryClient } from "@tanstack/react-query";
import apiClient from "../lib/api-client";
import { useAuthStore } from "../providers/auth-store-provider";
import { useRouter } from "next/navigation";

// The backend expects RegisterUserRequest { name, email, password, role }
// Login expects LoginRequest { email, password }
// Responses typically contain TokenResponse { accessToken, user }

export const useLogin = () => {
  const setUser = useAuthStore((s) => s.setUser);
  const router = useRouter();

  return useMutation({
    mutationFn: async (credentials: any) => {
      const { data } = await apiClient.post("/auth/login", credentials);
      return data;
    },
    onSuccess: (data) => {
      if (data.accessToken) {
        localStorage.setItem("accessToken", data.accessToken);
      }
      if (data.user) {
        setUser(data.user);
      }
      router.push("/");
    },
  });
};

export const useRegister = () => {
  const router = useRouter();
  
  return useMutation({
    mutationFn: async (userData: any) => {
      const { data } = await apiClient.post("/auth/register", userData);
      return data;
    },
    onSuccess: () => {
      // Typically, backend doesn't return token on register, just the user.
      // So redirect to login
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
      await apiClient.post("/auth/logout");
    },
    onSettled: () => {
      localStorage.removeItem("accessToken");
      logoutAction();
      queryClient.clear();
      router.push("/login");
    },
  });
};
