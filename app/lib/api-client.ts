import axios from "axios";
import { useAuthStore } from "../providers/auth-store-provider";

// We use the Next.js API rewrite we configured
const apiClient = axios.create({
  baseURL: "/api/backend",
  withCredentials: true, // Crucial for refresh cookies
  headers: {
    "Content-Type": "application/json",
  },
});

// Setup request interceptor to attach JWT access token
apiClient.interceptors.request.use(
  (config) => {
    // If we wanted to store the access token in memory or localStorage, we'd grab it here.
    // However, if the backend uses HTTP-only cookies for both, we don't need this.
    // Wait, the backend uses JWT for access (usually in Authorization header) and HTTP-only cookie for refresh.
    // For now, if we store the access token in localStorage, we can append it:
    const token = localStorage.getItem("accessToken");
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export default apiClient;
