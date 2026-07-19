"use client";

import { use, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { ArrowLeft, MapPin, Ticket } from "lucide-react";
import Navbar from "../../../components/Navbar";
import { useEvent } from "../../../hooks/useEvents";
import {
  buildBookingItems,
  useCreateBooking,
  useCreatePayment,
  useTicketTypes,
  useVenueLayout,
} from "../../../hooks/useBooking";
import type { SeatResponse } from "../../../lib/types";
import InteractiveSeatMap from "../../../components/SeatMap/InteractiveSeatMap";
import { useAuthStore } from "../../../providers/auth-store-provider";
import { getProblemDetail } from "../../../lib/api-client";

export default function BookingPage({ params }: { params: Promise<{ id: string }> }) {
  const resolvedParams = use(params);
  const eventId = resolvedParams.id;
  const router = useRouter();
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  const hydrated = useAuthStore((s) => s.hydrated);

  const { data: event, isLoading: isLoadingEvent } = useEvent(eventId);
  const { data: ticketTypes, isLoading: isLoadingTickets } = useTicketTypes(eventId);
  const {
    data: layout,
    isLoading: isLoadingLayout,
    refetch: refetchLayout,
  } = useVenueLayout(event?.venueId);

  const createBooking = useCreateBooking();
  const createPayment = useCreatePayment();

  const [selectedSeatsMap, setSelectedSeatsMap] = useState<
    Map<number, { seat: SeatResponse; sectionName: string; price: number }>
  >(new Map());
  const [gaQuantities, setGaQuantities] = useState<Map<number, number>>(new Map());
  const [checkoutError, setCheckoutError] = useState<string | null>(null);

  if (isLoadingEvent || isLoadingTickets || !hydrated) {
    return (
      <main className="min-h-screen bg-[#0b0e14]">
        <Navbar />
        <div className="flex items-center justify-center min-h-[calc(100vh-80px)] text-gray-400">
          Loading booking engine...
        </div>
      </main>
    );
  }

  if (!event) {
    return (
      <main className="min-h-screen bg-[#0b0e14]">
        <Navbar />
        <div className="container mx-auto px-6 pt-[100px]">
          <div className="bg-[#ff3366]/10 border border-[#ff3366]/30 text-[#ff8fab] p-4 rounded-lg">Event not found.</div>
          <Link href="/events" className="inline-block mt-4 bg-gradient-to-r from-[#00f0ff] to-[#7000ff] text-white font-semibold py-2 px-6 rounded-lg hover:shadow-[0_4px_15px_rgba(112,0,255,0.4)] hover:-translate-y-0.5 transition-all">
            Back to Events
          </Link>
        </div>
      </main>
    );
  }

  const handleSeatToggle = (seat: SeatResponse, sectionName: string, price: number) => {
    setSelectedSeatsMap((prev) => {
      const next = new Map(prev);
      if (next.has(seat.id)) {
        next.delete(seat.id);
      } else {
        next.set(seat.id, { seat, sectionName, price });
      }
      return next;
    });
  };

  const handleGaQuantityChange = (ticketId: number, quantity: number) => {
    setGaQuantities((prev) => {
      const next = new Map(prev);
      if (quantity === 0) next.delete(ticketId);
      else next.set(ticketId, quantity);
      return next;
    });
  };

  let totalAmount = 0;
  gaQuantities.forEach((quantity, ticketId) => {
    const ticket = ticketTypes?.find((t) => t.id === ticketId);
    if (ticket) totalAmount += Number(ticket.price) * quantity;
  });
  selectedSeatsMap.forEach((item) => {
    totalAmount += item.price;
  });

  const isCartEmpty = totalAmount === 0 && selectedSeatsMap.size === 0 && gaQuantities.size === 0;
  const isCheckingOut = createBooking.isPending || createPayment.isPending;

  const handleCheckout = async () => {
    setCheckoutError(null);

    if (!isAuthenticated) {
      router.push(`/login?redirect=/events/${event.id}/book`);
      return;
    }

    const items = buildBookingItems([...selectedSeatsMap.keys()], gaQuantities);
    if (items.length === 0) {
      setCheckoutError("Select at least one ticket or seat.");
      return;
    }

    try {
      const booking = await createBooking.mutateAsync({
        eventId: event.id,
        items,
      });

      sessionStorage.setItem("lastBookingId", String(booking.id));

      const payment = await createPayment.mutateAsync({
        bookingId: booking.id,
        idempotencyKey: crypto.randomUUID(),
      });

      if (payment.checkoutUrl) {
        window.location.href = payment.checkoutUrl;
        return;
      }
      setCheckoutError("Payment created but no checkout URL was returned.");
    } catch (err) {
      setCheckoutError(getProblemDetail(err));
      await refetchLayout();
    }
  };

  return (
    <main style={{ minHeight: "100vh", background: "var(--bg-primary)", color: "var(--text-primary)" }}>
      <Navbar />

      <div className="container" style={{ paddingTop: "140px", paddingBottom: "4rem", maxWidth: 1200 }}>
        {/* ── Page Header ── */}
        <div style={{ marginBottom: "3rem" }}>
          <Link
            href={`/events/${event.id}`}
            style={{
              display: "inline-flex",
              alignItems: "center",
              gap: "0.5rem",
              color: "var(--text-muted)",
              fontSize: "0.9rem",
              marginBottom: "1.5rem",
              transition: "color 0.2s ease",
              textDecoration: "none"
            }}
            className="hover:text-[#00f0ff]"
          >
            <ArrowLeft size={16} /> Back to Event Details
          </Link>
          <h1 style={{ fontSize: "3rem", fontWeight: 800, letterSpacing: "-0.02em", marginBottom: "0.5rem", lineHeight: 1.1 }}>
            Select Tickets
          </h1>
          <p style={{ color: "var(--accent-primary)", fontSize: "1.25rem", fontWeight: 600 }}>
            {event.title}
          </p>
        </div>

        <div style={{ display: "grid", gridTemplateColumns: "1fr 380px", gap: "2.5rem", alignItems: "start" }} className="lg:grid-cols-[1fr_380px] grid-cols-1">
          
          {/* ── Left Column ── */}
          <div style={{ display: "flex", flexDirection: "column", gap: "2.5rem" }}>
            
            {/* General Admission Tickets */}
            <div style={{
              background: "var(--bg-secondary)",
              border: "1px solid var(--glass-border)",
              borderRadius: "var(--radius-xl)",
              padding: "2.5rem",
              boxShadow: "var(--shadow-md)"
            }}>
              <h2 style={{ fontSize: "1.5rem", fontWeight: 700, marginBottom: "1.5rem", display: "flex", alignItems: "center", gap: "0.5rem" }}>
                <Ticket size={24} className="text-[#00f0ff]" /> General Admission
              </h2>
              
              {ticketTypes && ticketTypes.length > 0 ? (
                <div style={{ display: "flex", flexDirection: "column", gap: "1rem" }}>
                  {ticketTypes.map((ticket) => (
                    <div
                      key={ticket.id}
                      style={{
                        display: "flex",
                        justifyContent: "space-between",
                        alignItems: "center",
                        padding: "1.25rem 1.5rem",
                        background: "var(--bg-tertiary)",
                        border: "1px solid var(--glass-border)",
                        borderRadius: "var(--radius-lg)",
                        transition: "all 0.2s ease"
                      }}
                      className="hover:border-[#00f0ff]/30 hover:shadow-[0_4px_20px_rgba(0,240,255,0.05)]"
                    >
                      <div>
                        <div style={{ fontSize: "1.1rem", fontWeight: 600, color: "var(--text-primary)" }}>
                          {ticket.name}
                        </div>
                        <div style={{ color: "var(--text-secondary)", fontSize: "0.9rem", marginTop: "0.25rem", display: "flex", alignItems: "center", gap: "0.5rem" }}>
                          <span style={{ fontWeight: 700, color: "white" }}>${Number(ticket.price).toFixed(2)}</span>
                          {ticket.quota != null && (
                            <>
                              <span style={{ opacity: 0.5 }}>•</span>
                              <span>{ticket.quota} available</span>
                            </>
                          )}
                        </div>
                      </div>
                      
                      <div style={{ display: "flex", alignItems: "center", gap: "1rem" }}>
                        <select
                          style={{
                            width: "70px",
                            padding: "0.5rem",
                            background: "var(--bg-primary)",
                            color: "white",
                            border: "1px solid var(--glass-border)",
                            borderRadius: "var(--radius-md)",
                            fontSize: "1rem",
                            fontWeight: 600,
                            textAlign: "center",
                            cursor: "pointer",
                            outline: "none"
                          }}
                          className="focus:border-[#00f0ff]"
                          value={gaQuantities.get(ticket.id) || 0}
                          onChange={(e) => handleGaQuantityChange(ticket.id, parseInt(e.target.value, 10))}
                        >
                          {[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10].map((num) => (
                            <option key={num} value={num}>{num}</option>
                          ))}
                        </select>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div style={{ padding: "2rem", textAlign: "center", color: "var(--text-muted)", background: "var(--bg-tertiary)", borderRadius: "var(--radius-lg)", border: "1px dashed var(--glass-border)" }}>
                  No ticket types available for this event.
                </div>
              )}
            </div>

            {/* Venue Layout */}
            {event.venueId && (
              <div style={{
                background: "var(--bg-secondary)",
                border: "1px solid var(--glass-border)",
                borderRadius: "var(--radius-xl)",
                padding: "2.5rem",
                boxShadow: "var(--shadow-md)"
              }}>
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1.5rem" }}>
                  <h2 style={{ fontSize: "1.5rem", fontWeight: 700 }}>Venue Layout</h2>
                  <span style={{ display: "flex", alignItems: "center", gap: "0.4rem", color: "var(--text-secondary)", fontSize: "0.9rem" }}>
                    <MapPin size={14} /> {event.city || "Venue"}
                  </span>
                </div>

                {isLoadingLayout ? (
                  <div style={{ padding: "4rem", display: "flex", justifyContent: "center" }}>
                    <div className="skeleton" style={{ height: 300, width: "100%", borderRadius: "var(--radius-lg)" }} />
                  </div>
                ) : layout ? (
                  <div style={{ background: "var(--bg-primary)", border: "1px solid var(--glass-border)", borderRadius: "var(--radius-lg)", padding: "1.5rem", overflow: "hidden" }}>
                    <InteractiveSeatMap
                      sections={layout.sections}
                      selectedSeats={new Set(selectedSeatsMap.keys())}
                      onSeatToggle={handleSeatToggle}
                    />
                  </div>
                ) : (
                  <div style={{ padding: "3rem", textAlign: "center", color: "var(--text-muted)", background: "var(--bg-tertiary)", borderRadius: "var(--radius-lg)", border: "1px dashed var(--glass-border)" }}>
                    Venue layout unavailable.
                  </div>
                )}
              </div>
            )}
          </div>

          {/* ── Right Column (Order Summary) ── */}
          <div style={{ position: "sticky", top: "120px" }}>
            <div style={{
              background: "var(--bg-secondary)",
              border: "1px solid var(--glass-border)",
              borderRadius: "var(--radius-xl)",
              padding: "2rem",
              boxShadow: "0 20px 40px rgba(0,0,0,0.4)"
            }}>
              <h2 style={{ fontSize: "1.25rem", fontWeight: 700, marginBottom: "1.5rem", paddingBottom: "1rem", borderBottom: "1px solid var(--glass-border)" }}>
                Order Summary
              </h2>

              {isCartEmpty ? (
                <div style={{ padding: "2rem 0", textAlign: "center", color: "var(--text-muted)", fontSize: "0.95rem" }}>
                  <Ticket size={32} style={{ opacity: 0.2, margin: "0 auto 1rem" }} />
                  No tickets selected yet.
                </div>
              ) : (
                <div style={{ display: "flex", flexDirection: "column", gap: "0.75rem", marginBottom: "1.5rem", paddingBottom: "1.5rem", borderBottom: "1px solid var(--glass-border)" }}>
                  {Array.from(gaQuantities.entries()).map(([ticketId, quantity]) => {
                    const ticket = ticketTypes?.find((t) => t.id === ticketId);
                    if (!ticket) return null;
                    return (
                      <div key={`ga-${ticketId}`} style={{ display: "flex", justifyContent: "space-between", alignItems: "center", fontSize: "0.95rem" }}>
                        <span style={{ color: "var(--text-secondary)" }}>
                          <span style={{ color: "white", fontWeight: 600, marginRight: "0.5rem" }}>{quantity}x</span> 
                          {ticket.name}
                        </span>
                        <span style={{ fontWeight: 600 }}>${(Number(ticket.price) * quantity).toFixed(2)}</span>
                      </div>
                    );
                  })}

                  {Array.from(selectedSeatsMap.values()).map(({ seat, sectionName, price }) => (
                    <div key={`seat-${seat.id}`} style={{ display: "flex", justifyContent: "space-between", alignItems: "center", fontSize: "0.95rem" }}>
                      <span style={{ display: "flex", alignItems: "center", gap: "0.4rem", color: "var(--text-secondary)" }}>
                        <Ticket size={12} className="text-[#00f0ff]" />
                        {sectionName} <span style={{ opacity: 0.5 }}>·</span> Row {seat.rowLabel} Seat {seat.colNumber}
                      </span>
                      <span style={{ fontWeight: 600 }}>${price.toFixed(2)}</span>
                    </div>
                  ))}
                </div>
              )}

              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "2rem" }}>
                <span style={{ fontSize: "1.1rem", color: "var(--text-secondary)" }}>Total</span>
                <span style={{ fontSize: "2rem", fontWeight: 800, color: "var(--accent-primary)", lineHeight: 1 }}>
                  ${totalAmount.toFixed(2)}
                </span>
              </div>

              {checkoutError && (
                <div className="errorBox" style={{ marginBottom: "1.5rem", fontSize: "0.85rem", padding: "1rem" }}>
                  {checkoutError}
                </div>
              )}

              <button
                type="button"
                className={`btn ${isCartEmpty ? "btn-secondary" : "btn-primary"}`}
                style={{
                  width: "100%",
                  padding: "1rem",
                  fontSize: "1.05rem",
                  opacity: isCartEmpty || isCheckingOut ? 0.5 : 1,
                  cursor: isCartEmpty || isCheckingOut ? "not-allowed" : "pointer"
                }}
                disabled={isCartEmpty || isCheckingOut}
                onClick={handleCheckout}
              >
                {isCheckingOut
                  ? "Starting checkout..."
                  : !isAuthenticated
                    ? "Log in to Checkout"
                    : "Proceed to Checkout"}
              </button>
              
              <p style={{ textAlign: "center", fontSize: "0.8rem", color: "var(--text-muted)", marginTop: "1rem", fontWeight: 500 }}>
                Seats are held while you pay. Secure checkout via Stripe.
              </p>
            </div>
          </div>
        </div>
      </div>
    </main>
  );
}
