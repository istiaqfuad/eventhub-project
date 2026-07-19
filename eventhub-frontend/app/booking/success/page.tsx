"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { CheckCircle2, Ticket } from "lucide-react";
import Navbar from "../../components/Navbar/Navbar";

export default function BookingSuccessPage() {
  const [bookingId, setBookingId] = useState<string | null>(null);

  useEffect(() => {
    // Attempt to get bookingId from search params or session storage
    const params = new URLSearchParams(window.location.search);
    const id = params.get("bookingId") || sessionStorage.getItem("lastBookingId");
    if (id) {
      setBookingId(id);
    }
  }, []);

  return (
    <main className="min-h-screen bg-[#0b0e14] text-white">
      <Navbar />
      <div className="container mx-auto px-6 pt-40 pb-12 flex flex-col items-center justify-center text-center">
        <div className="bg-[#151a23] border border-white/10 rounded-3xl p-12 max-w-lg w-full flex flex-col items-center shadow-2xl relative overflow-hidden">
          <div className="absolute top-0 left-0 right-0 h-2 bg-gradient-to-r from-[#00f0ff] to-[#7000ff]" />
          
          <div className="w-24 h-24 bg-[#00f0ff]/10 rounded-full flex items-center justify-center mb-8 relative">
            <div className="absolute inset-0 bg-[#00f0ff]/20 rounded-full animate-ping" style={{ animationDuration: '3s' }} />
            <CheckCircle2 size={48} className="text-[#00f0ff]" />
          </div>

          <h1 className="text-4xl font-black mb-4 bg-clip-text text-transparent bg-gradient-to-r from-white to-gray-400">
            Payment Successful!
          </h1>
          <p className="text-gray-400 text-lg mb-8 leading-relaxed">
            Your booking has been confirmed. You will receive an email shortly with your digital tickets.
          </p>

          {bookingId && (
            <div className="bg-[#202632] border border-white/5 rounded-xl p-4 w-full mb-8 flex justify-between items-center">
              <span className="text-gray-400 text-sm font-medium uppercase tracking-wider">Order ID</span>
              <span className="font-mono text-lg font-bold text-[#00f0ff]">#{bookingId}</span>
            </div>
          )}

          <div className="flex flex-col sm:flex-row gap-4 w-full">
            <Link
              href="/bookings"
              className="flex-1 bg-gradient-to-r from-[#00f0ff] to-[#7000ff] text-white font-bold py-4 px-6 rounded-xl hover:shadow-[0_4px_20px_rgba(112,0,255,0.4)] hover:-translate-y-1 transition-all flex items-center justify-center gap-2"
            >
              <Ticket size={20} />
              View My Tickets
            </Link>
            <Link
              href="/"
              className="flex-1 bg-white/5 border border-white/10 text-white font-bold py-4 px-6 rounded-xl hover:bg-white/10 hover:-translate-y-1 transition-all"
            >
              Browse Events
            </Link>
          </div>
        </div>
      </div>
    </main>
  );
}
