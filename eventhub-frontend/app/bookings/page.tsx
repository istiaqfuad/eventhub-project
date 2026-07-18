"use client";

import Link from "next/link";
import { ArrowLeft, Calendar, ExternalLink, Ticket } from "lucide-react";
import Navbar from "../components/Navbar/Navbar";
import { useMyBookings } from "../hooks/useBooking";
import { useAuthStore } from "../providers/auth-store-provider";
import { useRouter } from "next/navigation";
import { useEffect } from "react";

function statusBadgeClass(status: string) {
  switch (status) {
    case "CONFIRMED": return "badge badge-success";
    case "PENDING": return "badge badge-warning";
    case "CANCELLED":
    case "EXPIRED": return "badge badge-danger";
    default: return "badge badge-neutral";
  }
}

export default function MyBookingsPage() {
  const { isAuthenticated, hydrated } = useAuthStore((s) => s);
  const { data: bookings, isLoading, error } = useMyBookings(isAuthenticated);
  const router = useRouter();

  useEffect(() => {
    if (hydrated && !isAuthenticated) {
      router.push("/login");
    }
  }, [hydrated, isAuthenticated, router]);

  if (!hydrated || isLoading) {
    return (
      <main style={{ minHeight: "100vh", background: "var(--bg-primary)" }}>
        <Navbar />
        <div className="container" style={{ paddingTop: 96, paddingBottom: "4rem" }}>
          <div style={{ height: 36, width: 200, marginBottom: "1.5rem" }} className="skeleton" />
          <div style={{ height: 40, width: 240, marginBottom: "0.5rem" }} className="skeleton" />
          <div style={{ height: 20, width: 320, marginBottom: "2rem" }} className="skeleton" />
          {[...Array(3)].map((_, i) => (
            <div key={i} className="skeleton" style={{ height: 100, borderRadius: "var(--radius-lg)", marginBottom: "0.875rem" }} />
          ))}
        </div>
      </main>
    );
  }

  if (!isAuthenticated) return null;

  return (
    <main style={{ minHeight: "100vh", background: "var(--bg-primary)" }}>
      <Navbar />
      <div className="container" style={{ paddingTop: 96, paddingBottom: "4rem" }}>

        {/* ── Header ── */}
        <Link href="/" style={{
          display: "inline-flex",
          alignItems: "center",
          gap: "0.4rem",
          color: "var(--text-muted)",
          fontSize: "0.875rem",
          marginBottom: "1.5rem",
          transition: "color 0.15s ease",
        }}>
          <ArrowLeft size={15} /> Back to Home
        </Link>

        <h1 style={{ fontSize: "2.25rem", marginBottom: "0.375rem" }}>My Tickets</h1>
        <p style={{ color: "var(--text-secondary)", marginBottom: "2rem" }}>
          Your bookings and upcoming events.
        </p>

        {/* ── Content ── */}
        {error ? (
          <div className="errorBox">Failed to load bookings. Please try again.</div>
        ) : bookings && bookings.length > 0 ? (
          <div style={{ display: "flex", flexDirection: "column", gap: "0.875rem", maxWidth: 720 }}>
            {bookings.map((b) => {
              const d = new Date(b.createdAt).toLocaleDateString("en-US", {
                month: "short", day: "numeric", year: "numeric",
              });
              const isConfirmed = b.status === "CONFIRMED";

              return (
                <Link
                  key={b.id}
                  href={`/bookings/${b.id}`}
                  style={{
                    display: "block",
                    background: "var(--bg-secondary)",
                    border: `1px solid ${isConfirmed ? "rgba(0,200,150,0.2)" : "var(--glass-border)"}`,
                    borderRadius: "var(--radius-lg)",
                    padding: "1.25rem 1.5rem",
                    transition: "all 0.2s ease",
                    textDecoration: "none",
                    color: "inherit",
                  }}
                >
                  <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", gap: "1rem" }}>
                    {/* Left */}
                    <div style={{ display: "flex", alignItems: "flex-start", gap: "1rem", flex: 1, minWidth: 0 }}>
                      <div style={{
                        width: 44,
                        height: 44,
                        borderRadius: "var(--radius-md)",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                        flexShrink: 0,
                        background: isConfirmed ? "rgba(0,200,150,0.12)" : "var(--bg-tertiary)",
                        border: `1px solid ${isConfirmed ? "rgba(0,200,150,0.3)" : "var(--glass-border)"}`,
                        color: isConfirmed ? "var(--success)" : "var(--text-muted)",
                      }}>
                        <Ticket size={20} />
                      </div>
                      <div style={{ minWidth: 0 }}>
                        <div style={{ fontWeight: 700, fontSize: "1rem", marginBottom: "0.25rem" }}>
                          Event #{b.eventId}
                        </div>
                        <div style={{ display: "flex", alignItems: "center", gap: "0.5rem", flexWrap: "wrap" }}>
                          <span style={{ fontSize: "0.8rem", color: "var(--text-muted)", display: "flex", alignItems: "center", gap: "0.3rem" }}>
                            <Calendar size={12} /> {d}
                          </span>
                          <span style={{ fontSize: "0.8rem", color: "var(--text-muted)" }}>
                            {b.items.length} ticket{b.items.length !== 1 && "s"}
                          </span>
                        </div>
                      </div>
                    </div>

                    {/* Right */}
                    <div style={{ display: "flex", flexDirection: "column", alignItems: "flex-end", gap: "0.5rem", flexShrink: 0 }}>
                      <div style={{ fontWeight: 700, fontSize: "1.1rem" }}>
                        ${Number(b.total).toFixed(2)}
                      </div>
                      <div style={{ display: "flex", alignItems: "center", gap: "0.5rem" }}>
                        <span className={statusBadgeClass(b.status)}>{b.status}</span>
                        <ExternalLink size={14} color="var(--text-muted)" />
                      </div>
                    </div>
                  </div>
                </Link>
              );
            })}
          </div>
        ) : (
          <div style={{
            textAlign: "center",
            padding: "4rem 2rem",
            background: "var(--bg-secondary)",
            border: "1px solid var(--glass-border)",
            borderRadius: "var(--radius-xl)",
            maxWidth: 480,
          }}>
            <Ticket size={48} style={{ opacity: 0.2, marginBottom: "1rem", display: "block", margin: "0 auto 1rem" }} />
            <h3 style={{ marginBottom: "0.5rem" }}>No tickets yet</h3>
            <p style={{ marginBottom: "1.5rem" }}>You haven&apos;t booked any events yet.</p>
            <Link href="/events" className="btn btn-primary">
              Browse Events
            </Link>
          </div>
        )}
      </div>
    </main>
  );
}
