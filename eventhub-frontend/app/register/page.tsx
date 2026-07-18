"use client";

import { useState } from "react";
import Link from "next/link";
import { Ticket, Eye, EyeOff } from "lucide-react";
import { getProblemDetail, useRegister } from "../hooks/useAuth";

export default function RegisterPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const { mutate: register, isPending, error } = useRegister();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    register({ email, password });
  };

  const passwordStrength = password.length === 0 ? 0 : password.length < 8 ? 1 : password.length < 12 ? 2 : 3;
  const strengthColors = ["", "#f04060", "#f5c842", "#00c896"];
  const strengthLabels = ["", "Too short", "Fair", "Strong"];

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
        background: "radial-gradient(circle, rgba(0, 212, 255, 0.1) 0%, transparent 70%)",
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
          <h1 style={{ fontSize: "1.75rem", marginBottom: "0.375rem" }}>Create an account</h1>
          <p style={{ color: "var(--text-secondary)", fontSize: "0.9rem", margin: 0 }}>
            Join to discover and book live events.
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
                autoComplete="new-password"
                placeholder="At least 8 characters"
                minLength={8}
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

            {/* Password strength meter */}
            {password.length > 0 && (
              <div>
                <div style={{ display: "flex", gap: "0.25rem", marginBottom: "0.25rem" }}>
                  {[1, 2, 3].map((level) => (
                    <div key={level} style={{
                      flex: 1,
                      height: 3,
                      borderRadius: 99,
                      background: level <= passwordStrength ? strengthColors[passwordStrength] : "var(--bg-tertiary)",
                      transition: "background 0.2s ease",
                    }} />
                  ))}
                </div>
                <p style={{ fontSize: "0.75rem", color: strengthColors[passwordStrength], margin: 0 }}>
                  {strengthLabels[passwordStrength]}
                </p>
              </div>
            )}
          </div>

          <button
            type="submit"
            className="btn btn-primary"
            disabled={isPending}
            style={{ width: "100%", padding: "0.8rem", marginTop: "0.25rem" }}
          >
            {isPending ? "Creating account..." : "Create Account"}
          </button>
        </form>

        <p style={{ textAlign: "center", marginTop: "1.75rem", fontSize: "0.875rem", color: "var(--text-secondary)" }}>
          Already have an account?{" "}
          <Link href="/login" style={{ color: "var(--accent-primary)", fontWeight: 600 }}>
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
}
