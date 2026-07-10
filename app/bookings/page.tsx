"use client";

import Link from "next/link";
import { ArrowLeft, Ticket } from "lucide-react";
import Navbar from "../components/Navbar/Navbar";
import { useMyBookings } from "../hooks/useBooking";
import { useAuthStore } from "../providers/auth-store-provider";

export default function MyBookingsPage() {
  const { isAuthenticated, hydrated } = useAuthStore((s) => s);
  const { data: bookings, isLoading, error } = useMyBookings(isAuthenticated);

  if (!hydrated || isLoading) {
    return (
      <main className="min-h-screen bg-[#0b0e14] text-white">
        <Navbar />
        <div className="flex items-center justify-center min-h-[calc(100vh-80px)] text-gray-400">
          Loading your tickets...
        </div>
      </main>
    );
  }

  if (!isAuthenticated) {
    return (
      <main className="min-h-screen bg-[#0b0e14] text-white">
        <Navbar />
        <div className="container mx-auto px-6 pt-[100px] text-center">
          <div className="bg-[#ff3366]/10 border border-[#ff3366]/30 text-[#ff8fab] p-4 rounded-lg inline-block">
            Please log in to view your tickets.
          </div>
        </div>
      </main>
    );
  }

  return (
    <main className="min-h-screen bg-[#0b0e14] text-white">
      <Navbar />
      <div className="container mx-auto px-6 pt-[100px] pb-12">
        <Link
          href="/"
          className="inline-flex items-center gap-2 mb-4 text-gray-400 hover:text-[#00f0ff] transition-colors"
        >
          <ArrowLeft size={16} /> Home
        </Link>
        <h1 className="text-4xl font-bold mb-2">My Tickets</h1>
        <p className="text-gray-400 text-lg mb-8">View and manage your upcoming events.</p>

        {error ? (
          <div className="bg-[#ff3366]/10 border border-[#ff3366]/30 text-[#ff8fab] p-4 rounded-lg">Failed to load bookings.</div>
        ) : bookings && bookings.length > 0 ? (
          <div className="flex flex-col gap-6 max-w-3xl">
            {bookings.map((b) => {
              const d = new Date(b.createdAt).toLocaleDateString("en-US", {
                month: "short",
                day: "numeric",
                year: "numeric",
              });
              const isConfirmed = b.status === "CONFIRMED";
              const isPending = b.status === "PENDING";
              const isCancelled = b.status === "CANCELLED";
              const isExpired = b.status === "EXPIRED";

              return (
                <Link
                  key={b.id}
                  href={`/bookings/${b.id}`}
                  className="block p-6 bg-[#151a23] border border-white/10 rounded-2xl hover:border-[#00f0ff]/50 hover:-translate-y-1 hover:shadow-[0_4px_20px_rgba(0,240,255,0.1)] transition-all group"
                >
                  <div className="flex justify-between items-start">
                    <div className="flex items-start gap-4">
                      <div className={`p-3 rounded-xl border ${isConfirmed ? 'bg-[#00f0ff]/10 border-[#00f0ff]/30 text-[#00f0ff]' : 'bg-white/5 border-white/10 text-gray-400'}`}>
                        <Ticket size={24} />
                      </div>
                      <div>
                        <h3 className="text-xl font-bold mb-1 group-hover:text-[#00f0ff] transition-colors">
                          Event #{b.eventId}
                        </h3>
                        <div className="text-sm text-gray-400">
                          Order #{b.id} • {d} • {b.items.length} ticket{b.items.length !== 1 && "s"}
                        </div>
                      </div>
                    </div>
                    <div className="text-right">
                      <div className="font-bold text-lg">${Number(b.total).toFixed(2)}</div>
                      <div
                        className={`text-xs font-semibold px-2 py-1 rounded-full mt-2 inline-block ${
                          isConfirmed ? "bg-[#00e676]/10 text-[#00e676] border border-[#00e676]/30" : 
                          isPending ? "bg-[#ffea00]/10 text-[#ffea00] border border-[#ffea00]/30" : 
                          isCancelled || isExpired ? "bg-[#ff3366]/10 text-[#ff3366] border border-[#ff3366]/30" : 
                          "bg-white/10 text-gray-400"
                        }`}
                      >
                        {b.status}
                      </div>
                    </div>
                  </div>
                </Link>
              );
            })}
          </div>
        ) : (
          <div className="p-12 text-center bg-[#151a23] border border-white/10 rounded-2xl">
            <Ticket size={48} className="mx-auto text-gray-500 mb-4 opacity-50" />
            <h3 className="text-xl font-bold mb-2">No tickets yet</h3>
            <p className="text-gray-400 mb-6">You haven&apos;t booked any events.</p>
            <Link
              href="/events"
              className="inline-block bg-gradient-to-r from-[#00f0ff] to-[#7000ff] text-white font-semibold py-3 px-6 rounded-lg hover:shadow-[0_4px_15px_rgba(112,0,255,0.4)] transition-all"
            >
              Browse Events
            </Link>
          </div>
        )}
      </div>
    </main>
  );
}
