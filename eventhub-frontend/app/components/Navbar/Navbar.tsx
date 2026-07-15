"use client";

import Link from "next/link";
import { Ticket, User, LogOut } from "lucide-react";
import styles from "./Navbar.module.css";
import { useEffect, useState } from "react";
import { useAuthStore } from "../../providers/auth-store-provider";
import { useLogout } from "../../hooks/useAuth";

export default function Navbar() {
  const [scrolled, setScrolled] = useState(false);
  const { isAuthenticated, user } = useAuthStore((s) => s);
  const { mutate: logout } = useLogout();

  const isOrganizer =
    user?.roles?.includes("ORGANIZER") || user?.roles?.includes("ADMIN");

  useEffect(() => {
    const handleScroll = () => {
      setScrolled(window.scrollY > 20);
    };

    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  return (
    <header className={`${styles.navbar} ${scrolled ? styles.scrolled : ""}`}>
      <div className={`container ${styles.navContainer}`}>
        <Link href="/" className={styles.logo}>
          <Ticket className={styles.logoIcon} size={28} />
          <span>
            Event<span className="text-gradient">Hub</span>
          </span>
        </Link>

        <nav className={styles.navLinks}>
          <Link href="/events" className={styles.navLink}>
            Events
          </Link>
          <Link href="/venues" className={styles.navLink}>
            Venues
          </Link>
          {isAuthenticated && (
            <Link href="/bookings" className={styles.navLink}>
              My Tickets
            </Link>
          )}
          {isAuthenticated && isOrganizer && (
            <Link
              href="/dashboard"
              className={styles.navLink}
              style={{ color: "var(--accent-primary)" }}
            >
              Dashboard
            </Link>
          )}
        </nav>

        <div className={styles.navActions}>
          {!isAuthenticated ? (
            <>
              <Link href="/login" className={`btn ${styles.btnLogin}`}>
                Log in
              </Link>
              <Link href="/register" className={`btn btn-primary ${styles.btnRegister}`}>
                Sign up
              </Link>
            </>
          ) : (
            <div style={{ display: "flex", alignItems: "center", gap: "1rem" }}>
              <span
                style={{
                  display: "flex",
                  alignItems: "center",
                  gap: "0.5rem",
                  fontSize: "0.9rem",
                  color: "var(--text-secondary)",
                }}
              >
                <User size={16} /> {user?.email}
              </span>
              <button
                onClick={() => logout()}
                className={`btn btn-secondary`}
                style={{ padding: "0.4rem 0.8rem", fontSize: "0.85rem" }}
                title="Logout"
                type="button"
              >
                <LogOut size={16} />
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
