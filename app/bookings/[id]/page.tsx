"use client";

import { use } from "react";
import Link from "next/link";
import { ArrowLeft, Ticket } from "lucide-react";
import Navbar from "../../components/Navbar/Navbar";
import { useBooking } from "../../hooks/useBooking";
import { useAuthStore } from "../../providers/auth-store-provider";
import { useRouter } from "next/navigation";
import { useEffect } from "react";

export default function BookingDetailsPage({ params }: { params: Promise<{ id: string }> }) {
  const resolvedParams = use(params);
  const { data: booking, isLoading, error } = useBooking(resolvedParams.id);
  const { isAuthenticated, hydrated } = useAuthStore((s) => s);
  const router = useRouter();

  useEffect(() => {
    if (hydrated && !isAuthenticated) {
      router.push("/login?redirect=/bookings");
    }
  }, [hydrated, isAuthenticated, router]);

  if (!hydrated || isLoading) {
    return (
      <main className="min-h-screen bg-[#0b0e14] text-white">
        <Navbar />
        <div className="flex items-center justify-center min-h-[calc(100vh-80px)] text-gray-400">
          Loading ticket details...
        </div>
      </main>
    );
  }

  if (error || !booking) {
    return (
      <main className="min-h-screen bg-[#0b0e14] text-white">
        <Navbar />
        <div className="container mx-auto px-6 pt-[100px] text-center">
          <div className="bg-[#ff3366]/10 border border-[#ff3366]/30 text-[#ff8fab] p-4 rounded-lg inline-block">
            Failed to load booking. It may not exist or you don't have access.
          </div>
        </div>
      </main>
    );
  }

  const isConfirmed = booking.status === "CONFIRMED";
  const isPending = booking.status === "PENDING";
  const isCancelled = booking.status === "CANCELLED";
  const isExpired = booking.status === "EXPIRED";

  const statusColorClass = isConfirmed 
    ? "text-[#00e676]" 
    : isPending 
      ? "text-[#ffea00]" 
      : isCancelled || isExpired 
        ? "text-[#ff3366]" 
        : "text-gray-400";

  return (
    <main className="min-h-screen bg-[#0b0e14] text-white">
      <Navbar />
      <div className="container mx-auto px-6 pt-[100px] pb-12">
        <Link
          href="/bookings"
          className="inline-flex items-center gap-2 mb-6 text-gray-400 hover:text-[#00f0ff] transition-colors"
        >
          <ArrowLeft size={16} /> Back to My Tickets
        </Link>
        <h1 className="text-3xl font-bold mb-6">Booking #{booking.id}</h1>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          <div className="bg-[#151a23] border border-white/10 rounded-2xl p-8 shadow-xl">
            <h2 className="text-xl font-semibold mb-6 pb-4 border-b border-white/10">Order Details</h2>
            <div className="flex flex-col gap-4">
              <div className="flex justify-between items-center">
                <span className="text-gray-400">Status</span>
                <span className={`font-bold ${statusColorClass}`}>{booking.status}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-gray-400">Event ID</span>
                <span className="font-semibold text-white">#{booking.eventId}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-gray-400">Date Ordered</span>
                <span className="font-semibold text-white">
                  {new Date(booking.createdAt).toLocaleString()}
                </span>
              </div>
              <div className="flex justify-between items-center pt-4 border-t border-white/10 mt-2">
                <span className="text-gray-300 font-semibold">Total</span>
                <span className="font-bold text-2xl text-[#00f0ff]">${Number(booking.total).toFixed(2)}</span>
              </div>
            </div>

            <div className="mt-8 pt-8 border-t border-white/10">
              <h3 className="font-semibold mb-4 text-gray-300">Line Items</h3>
              {booking.items.length === 0 ? (
                <div className="text-gray-500">No items in this booking.</div>
              ) : (
                <div className="flex flex-col gap-3">
                  {booking.items.map((item, idx) => (
                    <div
                      key={item.id}
                      className="flex justify-between items-center bg-[#202632] p-4 rounded-xl border border-white/5"
                    >
                      <div className="flex items-center gap-3">
                        <Ticket size={16} className="text-gray-400" />
                        <div>
                          <div className="font-medium text-sm">
                            {item.seatId ? "Reserved Seat" : "General Admission"}
                          </div>
                          <div className="text-xs text-gray-500 font-mono">
                            ID: {item.id}
                          </div>
                        </div>
                      </div>
                      <div className="font-semibold text-white">${Number(item.price).toFixed(2)}</div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>

          <div>
            <div className="bg-gradient-to-br from-[#1a2130] to-[#151a23] border border-[#00f0ff]/20 rounded-2xl p-8 relative overflow-hidden h-full flex flex-col items-center justify-center min-h-[300px]">
              {isConfirmed ? (
                <>
                  <div className="absolute top-0 left-0 right-0 h-2 bg-gradient-to-r from-[#00f0ff] to-[#7000ff]" />
                  <div className="w-48 h-48 bg-white p-4 rounded-xl shadow-[0_10px_30px_rgba(0,240,255,0.2)] mb-6 flex items-center justify-center border-4 border-dashed border-gray-200">
                    <div className="text-center">
                      <Ticket size={64} className="mx-auto text-black mb-2" />
                      <div className="text-black font-black uppercase text-xl">VALID TICKET</div>
                    </div>
                  </div>
                  <p className="text-gray-300 text-center font-medium">
                    Present this QR code (simulated) at the venue for entry.
                  </p>
                </>
              ) : (
                <div className="text-center text-gray-400 max-w-sm">
                  <Ticket size={48} className="mx-auto mb-4 opacity-50" />
                  <h3 className="text-xl font-semibold mb-2 text-white">Tickets Unavailable</h3>
                  <p>
                    {isPending
                      ? "Your booking is pending payment."
                      : "This booking was cancelled or expired. No tickets were issued."}
                  </p>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </main>
  );
}
