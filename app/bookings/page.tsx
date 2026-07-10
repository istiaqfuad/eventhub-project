"use client";

import Link from "next/link";
import { ArrowLeft, Ticket } from "lucide-react";
import Navbar from "../components/Navbar";
import { useMyBookings } from "../hooks/useBooking";
import { useAuthStore } from "../providers/auth-store-provider";
import { useRouter } from "next/navigation";
import { useEffect } from "react";

export default function MyBookingsPage() {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  const hydrated = useAuthStore((s) => s.hydrated);
  const router = useRouter();
  const { data: bookings, isLoading, error } = useMyBookings(isAuthenticated);

  useEffect(() => {
    if (hydrated && !isAuthenticated) {
      router.replace("/login?redirect=/bookings");
    }
  }, [hydrated, isAuthenticated, router]);

  return (
    <main>
      <Navbar />
      <div className="container" style={{ paddingTop: "100px", minHeight: "100vh", paddingBottom: "3rem" }}>
        <Link
          href="/events"
          style={{
            color: "var(--text-secondary)",
            display: "inline-flex",
            alignItems: "center",
            gap: "0.5rem",
            marginBottom: "1rem",
          }}
        >
          <ArrowLeft size={16} /> Events
        </Link>
        <h1 style={{ fontSize: "2.5rem", marginBottom: "0.5rem" }}>My Tickets</h1>
        <p style={{ color: "var(--text-secondary)", marginBottom: "2rem" }}>
          Bookings and payment status for your account.
        </p>

        {!hydrated || isLoading ? (
          <div style={{ color: "var(--text-secondary)" }}>Loading bookings...</div>
        ) : error ? (
          <div className="errorBox">Failed to load bookings.</div>
        ) : bookings && bookings.length > 0 ? (
          <div style={{ display: "flex", flexDirection: "column", gap: "1rem" }}>
            {bookings.map((b) => (
              <Link
                key={b.id}
                href={`/bookings/${b.id}`}
                style={{
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                  padding: "1.25rem",
                  background: "var(--bg-secondary)",
                  border: "1px solid var(--glass-border)",
                  borderRadius: "var(--radius-lg)",
                  textDecoration: "none",
                  color: "inherit",
                }}
              >
                <div style={{ display: "flex", alignItems: "center", gap: "1rem" }}>
                  <Ticket size={22} color="var(--accent-primary)" />
                  <div>
                    <div style={{ fontWeight: 600 }}>Booking #{b.id}</div>
                    <div style={{ fontSize: "0.85rem", color: "var(--text-secondary)" }}>
                      Event #{b.eventId} · {b.items?.length ?? 0} item(s)
                    </div>
                  </div>
                </div>
                <div style={{ textAlign: "right" }}>
                  <div style={{ fontWeight: 700 }}>${Number(b.totalAmount).toFixed(2)}</div>
                  <div
                    style={{
                      fontSize: "0.85rem",
                      color:
                        b.status === "CONFIRMED"
                          ? "var(--success)"
                          : b.status === "PENDING"
                            ? "var(--warning)"
                            : "var(--text-secondary)",
                    }}
                  >
                    {b.status}
                  </div>
                </div>
              </Link>
            ))}
          </div>
        ) : (
          <div style={{ color: "var(--text-secondary)", textAlign: "center", padding: "3rem 0" }}>
            No bookings yet.{" "}
            <Link href="/events" className="text-gradient">
              Browse events
            </Link>
          </div>
        )}
      </div>
    </main>
  );
}
