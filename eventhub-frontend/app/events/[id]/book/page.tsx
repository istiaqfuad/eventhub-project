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
    <main className="min-h-screen bg-[#0b0e14] text-white">
      <Navbar />

      <div className="container mx-auto px-6 pt-[100px] pb-12">
        <div className="mb-8">
          <Link
            href={`/events/${event.id}`}
            className="inline-flex items-center gap-2 mb-4 text-gray-400 hover:text-[#00f0ff] transition-colors"
          >
            <ArrowLeft size={16} /> Back to Event Details
          </Link>
          <h1 className="text-4xl font-bold mb-2">Select Tickets</h1>
          <p className="text-gray-400 text-lg">{event.title}</p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-[1fr_350px] gap-12">
          <div className="flex flex-col gap-8">
            <div className="bg-[#151a23] border border-white/10 rounded-2xl p-8">
              <h2 className="text-2xl font-bold mb-4">General Admission Tickets</h2>
              {ticketTypes && ticketTypes.length > 0 ? (
                <div className="flex flex-col gap-4">
                  {ticketTypes.map((ticket) => (
                    <div
                      key={ticket.id}
                      className="flex justify-between items-center p-4 border border-white/10 rounded-xl bg-[#202632]"
                    >
                      <div>
                        <div className="font-semibold text-lg">{ticket.name}</div>
                        <div className="text-gray-400 text-sm mt-1">
                          ${Number(ticket.price).toFixed(2)}
                          {ticket.quota != null && (
                            <span className="ml-2">· {ticket.quota} available</span>
                          )}
                        </div>
                      </div>
                      <select
                        className="w-20 p-2 bg-[#151a23] text-white border border-white/10 rounded-lg focus:border-[#00f0ff] focus:outline-none"
                        value={gaQuantities.get(ticket.id) || 0}
                        onChange={(e) =>
                          handleGaQuantityChange(ticket.id, parseInt(e.target.value, 10))
                        }
                      >
                        {[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10].map((num) => (
                          <option key={num} value={num}>
                            {num}
                          </option>
                        ))}
                      </select>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="text-gray-400">No ticket types available.</div>
              )}
            </div>

            {event.venueId && (
              <div className="bg-[#151a23] border border-white/10 rounded-2xl p-8">
                <div className="flex justify-between items-center mb-6">
                  <h2 className="text-2xl font-bold">Venue Layout</h2>
                  <span className="text-sm text-gray-400 flex items-center gap-2">
                    <MapPin size={14} /> {event.city || "Venue"}
                  </span>
                </div>

                {isLoadingLayout ? (
                  <div className="p-12 text-center text-gray-400">
                    Loading Seat Map...
                  </div>
                ) : layout ? (
                  <InteractiveSeatMap
                    sections={layout.sections}
                    selectedSeats={new Set(selectedSeatsMap.keys())}
                    onSeatToggle={handleSeatToggle}
                  />
                ) : (
                  <div className="text-gray-400">Venue layout unavailable.</div>
                )}
              </div>
            )}
          </div>

          <div>
            <div className="bg-[#151a23] border border-white/10 rounded-2xl p-8 sticky top-[100px] shadow-xl">
              <h2 className="text-2xl font-bold mb-6">Order Summary</h2>

              {isCartEmpty ? (
                <div className="text-gray-400 text-center py-8 border-b border-white/10 mb-6">
                  No tickets selected yet.
                </div>
              ) : (
                <div className="flex flex-col gap-3 mb-6 pb-6 border-b border-white/10">
                  {Array.from(gaQuantities.entries()).map(([ticketId, quantity]) => {
                    const ticket = ticketTypes?.find((t) => t.id === ticketId);
                    if (!ticket) return null;
                    return (
                      <div
                        key={`ga-${ticketId}`}
                        className="flex justify-between text-sm"
                      >
                        <span>
                          {quantity}x {ticket.name}
                        </span>
                        <span className="font-medium">${(Number(ticket.price) * quantity).toFixed(2)}</span>
                      </div>
                    );
                  })}

                  {Array.from(selectedSeatsMap.values()).map(({ seat, sectionName, price }) => (
                    <div
                      key={`seat-${seat.id}`}
                      className="flex justify-between text-sm"
                    >
                      <span className="flex items-center gap-1.5 text-gray-300">
                        <Ticket size={12} className="text-[#00f0ff]" />
                        {sectionName} · Row {seat.rowLabel} Seat {seat.colNumber}
                      </span>
                      <span className="font-medium text-white">${price.toFixed(2)}</span>
                    </div>
                  ))}
                </div>
              )}

              <div className="flex justify-between mb-6 text-xl font-bold">
                <span>Total</span>
                <span className="text-[#00f0ff]">${totalAmount.toFixed(2)}</span>
              </div>

              {checkoutError && (
                <div className="bg-[#ff3366]/10 border border-[#ff3366]/30 text-[#ff8fab] p-4 rounded-lg mb-4 text-sm">
                  {checkoutError}
                </div>
              )}

              <button
                type="button"
                className={`w-full py-4 px-6 rounded-xl font-bold text-lg transition-all ${
                  isCartEmpty 
                    ? "bg-[#202632] text-gray-500 cursor-not-allowed" 
                    : "bg-gradient-to-r from-[#00f0ff] to-[#7000ff] text-white hover:shadow-[0_4px_20px_rgba(112,0,255,0.4)] hover:-translate-y-1"
                }`}
                disabled={isCartEmpty || isCheckingOut}
                onClick={handleCheckout}
              >
                {isCheckingOut
                  ? "Starting checkout..."
                  : !isAuthenticated
                    ? "Log in to Checkout"
                    : "Proceed to Checkout"}
              </button>
              <p className="text-center text-xs text-gray-500 mt-4 font-medium">
                Seats are held while you pay. Secure checkout via Stripe.
              </p>
            </div>
          </div>
        </div>
      </div>
    </main>
  );
}
