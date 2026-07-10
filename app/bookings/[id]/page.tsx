"use client";

import { use, useEffect } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { ArrowLeft } from "lucide-react";
import Navbar from "../../components/Navbar";
import { useBooking } from "../../hooks/useBooking";
import { useAuthStore } from "../../providers/auth-store-provider";

export default function BookingDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  const hydrated = useAuthStore((s) => s.hydrated);
  const router = useRouter();
  const { data: booking, isLoading, error } = useBooking(isAuthenticated ? id : null);

  useEffect(() => {
    if (hydrated && !isAuthenticated) {
      router.replace(`/login?redirect=/bookings/${id}`);
    }
  }, [hydrated, isAuthenticated, router, id]);

  return (
    <main>
      <Navbar />
      <div className="container" style={{ paddingTop: "100px", minHeight: "100vh", paddingBottom: "3rem" }}>
        <Link
          href="/bookings"
          style={{
            color: "var(--text-secondary)",
            display: "inline-flex",
            alignItems: "center",
            gap: "0.5rem",
            marginBottom: "1rem",
          }}
        >
          <ArrowLeft size={16} /> All bookings
        </Link>

        {isLoading || !hydrated ? (
          <div style={{ color: "var(--text-secondary)" }}>Loading...</div>
        ) : error || !booking ? (
          <div className="errorBox">Booking not found or you do not have access.</div>
        ) : (
          <>
            <h1 style={{ fontSize: "2rem", marginBottom: "0.5rem" }}>Booking #{booking.id}</h1>
            <p style={{ color: "var(--text-secondary)", marginBottom: "2rem" }}>
              Status:{" "}
              <strong
                style={{
                  color:
                    booking.status === "CONFIRMED"
                      ? "var(--success)"
                      : booking.status === "PENDING"
                        ? "var(--warning)"
                        : "inherit",
                }}
              >
                {booking.status}
              </strong>
            </p>

            <div
              style={{
                background: "var(--bg-secondary)",
                border: "1px solid var(--glass-border)",
                borderRadius: "var(--radius-lg)",
                padding: "1.5rem",
                maxWidth: 560,
              }}
            >
              <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "1rem" }}>
                <span style={{ color: "var(--text-secondary)" }}>Event</span>
                <Link href={`/events/${booking.eventId}`} className="text-gradient">
                  #{booking.eventId}
                </Link>
              </div>
              <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "1rem" }}>
                <span style={{ color: "var(--text-secondary)" }}>Total</span>
                <span style={{ fontWeight: 700 }}>${Number(booking.totalAmount).toFixed(2)}</span>
              </div>
              <div style={{ marginTop: "1.5rem" }}>
                <h3 style={{ marginBottom: "0.75rem" }}>Line items</h3>
                <ul style={{ listStyle: "none", display: "flex", flexDirection: "column", gap: "0.5rem" }}>
                  {booking.items?.map((item) => (
                    <li
                      key={item.id}
                      style={{
                        display: "flex",
                        justifyContent: "space-between",
                        fontSize: "0.95rem",
                        padding: "0.5rem 0",
                        borderBottom: "1px solid var(--glass-border)",
                      }}
                    >
                      <span>
                        {item.seatId
                          ? `Seat #${item.seatId}`
                          : item.ticketTypeId
                            ? `Ticket type #${item.ticketTypeId}`
                            : "Item"}
                      </span>
                      <span>${Number(item.price).toFixed(2)}</span>
                    </li>
                  ))}
                </ul>
              </div>
            </div>
          </>
        )}
      </div>
    </main>
  );
}
