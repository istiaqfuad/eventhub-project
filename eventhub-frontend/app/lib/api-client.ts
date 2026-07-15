import axios, { AxiosError, InternalAxiosRequestConfig } from "axios";

const ACCESS_TOKEN_KEY = "accessToken";

/**
 * Proxied to Spring at /api/* via next.config rewrites.
 * Auth cookie-bearing calls (refresh/logout) use /api/auth/* so the refresh
 * cookie Path=/api/auth is sent by the browser.
 */
const apiClient = axios.create({
  baseURL: "/api/backend",
  withCredentials: true,
  xsrfCookieName: "XSRF-TOKEN",
  xsrfHeaderName: "X-XSRF-TOKEN",
  headers: {
    "Content-Type": "application/json",
  },
});

export function getAccessToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem(ACCESS_TOKEN_KEY);
}

export function setAccessToken(token: string | null) {
  if (typeof window === "undefined") return;
  if (token) {
    localStorage.setItem(ACCESS_TOKEN_KEY, token);
  } else {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
  }
}

apiClient.interceptors.request.use(
  (config) => {
    const token = getAccessToken();
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

let refreshPromise: Promise<string | null> | null = null;

/**
 * Refresh cookie lives under Path=/api/auth — call the matching rewrite, not /api/backend.
 */
async function refreshAccessToken(): Promise<string | null> {
  if (!refreshPromise) {
    refreshPromise = axios
      .post<{ accessToken: string }>(
        "/api/auth/refresh",
        {},
        {
          withCredentials: true,
          xsrfCookieName: "XSRF-TOKEN",
          xsrfHeaderName: "X-XSRF-TOKEN",
          headers: { "Content-Type": "application/json" },
        }
      )
      .then((res) => {
        const token = res.data?.accessToken ?? null;
        setAccessToken(token);
        return token;
      })
      .catch(() => {
        setAccessToken(null);
        return null;
      })
      .finally(() => {
        refreshPromise = null;
      });
  }
  return refreshPromise;
}

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const original = error.config as InternalAxiosRequestConfig & { _retry?: boolean };
    if (!original || error.response?.status !== 401 || original._retry) {
      return Promise.reject(error);
    }
    // Do not refresh-loop on auth endpoints themselves
    const url = original.url ?? "";
    if (url.includes("/auth/login") || url.includes("/auth/register") || url.includes("/auth/refresh")) {
      return Promise.reject(error);
    }
    original._retry = true;
    const token = await refreshAccessToken();
    if (!token) {
      return Promise.reject(error);
    }
    original.headers.Authorization = `Bearer ${token}`;
    return apiClient(original);
  }
);

export async function logoutRequest(): Promise<void> {
  try {
    await axios.post(
      "/api/auth/logout",
      {},
      {
        withCredentials: true,
        xsrfCookieName: "XSRF-TOKEN",
        xsrfHeaderName: "X-XSRF-TOKEN",
        headers: { "Content-Type": "application/json" },
      }
    );
  } finally {
    setAccessToken(null);
  }
}

export function getProblemDetail(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as { detail?: string; title?: string } | undefined;
    return data?.detail || data?.title || error.message || "Request failed";
  }
  if (error instanceof Error) return error.message;
  return "Request failed";
}

export default apiClient;
