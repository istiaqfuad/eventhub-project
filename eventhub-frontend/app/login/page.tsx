"use client";

import { Suspense, useState } from "react";
import Link from "next/link";
import { Ticket, Eye, EyeOff } from "lucide-react";
import { getProblemDetail, useLogin } from "../hooks/useAuth";

function LoginForm() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const { mutate: login, isPending, error } = useLogin();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    login({ email, password });
  };

  return (
    <div style={{
      minHeight: "100vh",
      background: "var(--bg-primary)",
      display: "flex",
      alignItems: "center",
      justifyContent: "center",
      padding: "1.5rem",
      position: "relative",
      overflow: "hidden",
    }}>
      {/* Background glow */}
      <div style={{
        position: "absolute",
        top: "30%",
        left: "50%",
        transform: "translate(-50%, -50%)",
        width: 600,
        height: 600,
        background: "radial-gradient(circle, rgba(108, 59, 255, 0.12) 0%, transparent 70%)",
        pointerEvents: "none",
      }} />

      <div style={{
        width: "100%",
        maxWidth: 420,
        background: "var(--bg-secondary)",
        borderRadius: "var(--radius-xl)",
        padding: "2.5rem 2rem",
        border: "1px solid var(--glass-border)",
        boxShadow: "0 24px 64px rgba(0,0,0,0.5)",
        position: "relative",
        zIndex: 1,
      }}>
        {/* Logo */}
        <div style={{ textAlign: "center", marginBottom: "2rem" }}>
          <Link href="/" style={{
            display: "inline-flex",
            alignItems: "center",
            gap: "0.5rem",
            color: "var(--accent-primary)",
            marginBottom: "1.25rem",
          }}>
            <Ticket size={22} />
            <span style={{ fontWeight: 800, fontSize: "1.2rem" }}>EventHub</span>
          </Link>
          <h1 style={{ fontSize: "1.75rem", marginBottom: "0.375rem" }}>Welcome back</h1>
          <p style={{ color: "var(--text-secondary)", fontSize: "0.9rem", margin: 0 }}>
            Sign in to access your tickets and events.
          </p>
        </div>

        {error && (
          <div className="errorBox" style={{ marginBottom: "1.5rem" }}>
            {getProblemDetail(error)}
          </div>
        )}

        <form onSubmit={handleSubmit} style={{ display: "flex", flexDirection: "column", gap: "1.125rem" }}>
          <div style={{ display: "flex", flexDirection: "column", gap: "0.375rem" }}>
            <label htmlFor="email" style={{ fontSize: "0.875rem", fontWeight: 600, color: "var(--text-primary)" }}>
              Email Address
            </label>
            <input
              type="email"
              id="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              autoComplete="email"
              placeholder="you@example.com"
            />
          </div>

          <div style={{ display: "flex", flexDirection: "column", gap: "0.375rem" }}>
            <label htmlFor="password" style={{ fontSize: "0.875rem", fontWeight: 600, color: "var(--text-primary)" }}>
              Password
            </label>
            <div style={{ position: "relative" }}>
              <input
                type={showPassword ? "text" : "password"}
                id="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                autoComplete="current-password"
                placeholder="••••••••"
                style={{ paddingRight: "3rem" }}
              />
              <button
                type="button"
                onClick={() => setShowPassword((v) => !v)}
                style={{
                  position: "absolute",
                  right: "0.875rem",
                  top: "50%",
                  transform: "translateY(-50%)",
                  background: "none",
                  border: "none",
                  color: "var(--text-muted)",
                  cursor: "pointer",
                  display: "flex",
                  alignItems: "center",
                  padding: 0,
                }}
              >
                {showPassword ? <EyeOff size={17} /> : <Eye size={17} />}
              </button>
            </div>
          </div>

          <button
            type="submit"
            className="btn btn-primary"
            disabled={isPending}
            style={{ width: "100%", padding: "0.8rem", marginTop: "0.25rem" }}
          >
            {isPending ? "Signing in..." : "Sign In"}
          </button>
        </form>

        <p style={{ textAlign: "center", marginTop: "1.75rem", fontSize: "0.875rem", color: "var(--text-secondary)" }}>
          Don&apos;t have an account?{" "}
          <Link href="/register" style={{ color: "var(--accent-primary)", fontWeight: 600 }}>
            Create one
          </Link>
        </p>
      </div>
    </div>
  );
}

export default function LoginPage() {
  return (
    <Suspense fallback={
      <div style={{ minHeight: "100vh", background: "var(--bg-primary)", display: "flex", alignItems: "center", justifyContent: "center" }}>
        <div style={{ color: "var(--text-secondary)" }}>Loading...</div>
      </div>
    }>
      <LoginForm />
    </Suspense>
  );
}
