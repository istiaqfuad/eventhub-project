"use client";

import { useState } from "react";
import Link from "next/link";
import { Ticket } from "lucide-react";
import styles from "../auth.module.css";
import { useRegister } from "../hooks/useAuth";

export default function RegisterPage() {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState("CUSTOMER");
  const { mutate: register, isPending, error } = useRegister();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    register({ name, email, password, role });
  };

  return (
    <div className={styles.authContainer}>
      <div className={styles.authCard}>
        <div className={styles.authHeader}>
          <Link href="/" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem', color: 'var(--accent-primary)' }}>
            <Ticket size={24} />
            <span style={{ fontWeight: 800, fontSize: '1.25rem' }}>EventHub</span>
          </Link>
          <h1>Create Account</h1>
          <p>Join the best platform for live entertainment.</p>
        </div>

        {error && (
          <div className={styles.errorBox}>
            Registration failed. Email might be in use or data is invalid.
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className={styles.formGroup}>
            <label htmlFor="name">Full Name</label>
            <input
              type="text"
              id="name"
              className={styles.inputField}
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
              placeholder="John Doe"
            />
          </div>

          <div className={styles.formGroup}>
            <label htmlFor="email">Email Address</label>
            <input
              type="email"
              id="email"
              className={styles.inputField}
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              placeholder="you@example.com"
            />
          </div>

          <div className={styles.formGroup}>
            <label htmlFor="password">Password</label>
            <input
              type="password"
              id="password"
              className={styles.inputField}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              placeholder="••••••••"
              minLength={8}
            />
          </div>

          <div className={styles.formGroup}>
            <label htmlFor="role">I am a...</label>
            <select 
              id="role"
              className={styles.inputField}
              value={role}
              onChange={(e) => setRole(e.target.value)}
              style={{ appearance: 'none', backgroundColor: 'rgba(0,0,0,0.4)' }}
            >
              <option value="CUSTOMER">Fan / Customer</option>
              <option value="ORGANIZER">Event Organizer</option>
            </select>
          </div>

          <button
            type="submit"
            className={`btn btn-primary ${styles.submitBtn}`}
            disabled={isPending}
          >
            {isPending ? "Creating Account..." : "Create Account"}
          </button>
        </form>

        <div className={styles.authFooter}>
          Already have an account? <Link href="/login">Sign in</Link>
        </div>
      </div>
    </div>
  );
}
