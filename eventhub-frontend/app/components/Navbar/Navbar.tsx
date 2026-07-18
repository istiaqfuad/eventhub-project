"use client";

import Link from "next/link";
import { Ticket, User, LogOut, Menu, X, LayoutDashboard } from "lucide-react";
import styles from "./Navbar.module.css";
import { useEffect, useState } from "react";
import { usePathname } from "next/navigation";
import { useAuthStore } from "../../providers/auth-store-provider";
import { useLogout } from "../../hooks/useAuth";

export default function Navbar() {
  const [scrolled, setScrolled] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const { isAuthenticated, user } = useAuthStore((s) => s);
  const { mutate: logout } = useLogout();
  const pathname = usePathname();

  const isOrganizer =
    user?.roles?.includes("ORGANIZER") || user?.roles?.includes("ADMIN");

  useEffect(() => {
    const handleScroll = () => setScrolled(window.scrollY > 20);
    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  // Close mobile menu on route change
  useEffect(() => {
    setMobileOpen(false);
  }, [pathname]);

  const isActive = (href: string) => pathname === href;

  return (
    <header className={`${styles.navbar} ${scrolled ? styles.scrolled : ""}`}>
      <div className={`container ${styles.navContainer}`}>
        {/* Logo */}
        <Link href="/" className={styles.logo}>
          <Ticket className={styles.logoIcon} size={24} />
          <span>Event<span className="text-gradient">Hub</span></span>
        </Link>

        {/* Desktop Nav Links */}
        <nav className={styles.navLinks}>
          <Link href="/events" className={`${styles.navLink} ${isActive("/events") ? styles.navLinkActive : ""}`}>
            Events
          </Link>
          <Link href="/venues" className={`${styles.navLink} ${isActive("/venues") ? styles.navLinkActive : ""}`}>
            Venues
          </Link>
          {isAuthenticated && (
            <Link href="/bookings" className={`${styles.navLink} ${isActive("/bookings") ? styles.navLinkActive : ""}`}>
              My Tickets
            </Link>
          )}
          {isAuthenticated && isOrganizer && (
            <Link href="/organizer" className={`${styles.navLink} ${styles.navLinkOrganizer} ${isActive("/organizer") ? styles.navLinkActive : ""}`}>
              <LayoutDashboard size={15} />
              Dashboard
            </Link>
          )}
        </nav>

        {/* Desktop Actions */}
        <div className={styles.navActions}>
          {!isAuthenticated ? (
            <>
              <Link href="/login" className={`btn ${styles.btnLogin}`}>Log in</Link>
              <Link href="/register" className={`btn btn-primary ${styles.btnRegister}`}>Sign up</Link>
            </>
          ) : (
            <div className={styles.userMenu}>
              <div className={styles.userInfo}>
                <div className={styles.userAvatar}>
                  {user?.email?.[0]?.toUpperCase() ?? <User size={14} />}
                </div>
                <span className={styles.userEmail}>{user?.email}</span>
              </div>
              <button
                onClick={() => logout()}
                className={styles.logoutBtn}
                title="Sign out"
                type="button"
              >
                <LogOut size={16} />
              </button>
            </div>
          )}
        </div>

        {/* Mobile Hamburger */}
        <button
          className={styles.hamburger}
          onClick={() => setMobileOpen((v) => !v)}
          aria-label="Toggle menu"
          type="button"
        >
          {mobileOpen ? <X size={22} /> : <Menu size={22} />}
        </button>
      </div>

      {/* Mobile Menu Dropdown */}
      <div className={`${styles.mobileMenu} ${mobileOpen ? styles.mobileMenuOpen : ""}`}>
        <nav className={styles.mobileNav}>
          <Link href="/events" className={`${styles.mobileNavLink} ${isActive("/events") ? styles.mobileNavLinkActive : ""}`}>Events</Link>
          <Link href="/venues" className={`${styles.mobileNavLink} ${isActive("/venues") ? styles.mobileNavLinkActive : ""}`}>Venues</Link>
          {isAuthenticated && (
            <Link href="/bookings" className={`${styles.mobileNavLink} ${isActive("/bookings") ? styles.mobileNavLinkActive : ""}`}>My Tickets</Link>
          )}
          {isAuthenticated && isOrganizer && (
            <Link href="/organizer" className={`${styles.mobileNavLink} ${isActive("/organizer") ? styles.mobileNavLinkActive : ""}`}>Dashboard</Link>
          )}
          <div className={styles.mobileDivider} />
          {!isAuthenticated ? (
            <div className={styles.mobileAuthBtns}>
              <Link href="/login" className="btn btn-secondary" style={{ flex: 1 }}>Log in</Link>
              <Link href="/register" className="btn btn-primary" style={{ flex: 1 }}>Sign up</Link>
            </div>
          ) : (
            <div className={styles.mobileUserRow}>
              <div className={styles.userAvatar}>{user?.email?.[0]?.toUpperCase() ?? "U"}</div>
              <span className={styles.mobileUserEmail}>{user?.email}</span>
              <button onClick={() => logout()} className={`btn btn-secondary`} style={{ padding: "0.4rem 0.75rem" }} type="button">
                <LogOut size={15} /> Sign out
              </button>
            </div>
          )}
        </nav>
      </div>
    </header>
  );
}
