"use client";

import Link from "next/link";
import { XCircle } from "lucide-react";
import Navbar from "../../components/Navbar/Navbar";

export default function BookingCancelPage() {
  return (
    <main className="min-h-screen bg-[#0b0e14] text-white">
      <Navbar />
      <div className="container mx-auto px-6 pt-[150px] pb-12 flex flex-col items-center justify-center text-center">
        <div className="bg-[#151a23] border border-white/10 rounded-3xl p-12 max-w-lg w-full flex flex-col items-center shadow-2xl relative overflow-hidden">
          <div className="absolute top-0 left-0 right-0 h-2 bg-[#ff3366]" />
          
          <div className="w-24 h-24 bg-[#ff3366]/10 rounded-full flex items-center justify-center mb-8">
            <XCircle size={48} className="text-[#ff3366]" />
          </div>

          <h1 className="text-4xl font-black mb-4 bg-clip-text text-transparent bg-gradient-to-r from-white to-gray-400">
            Checkout Cancelled
          </h1>
          <p className="text-gray-400 text-lg mb-8 leading-relaxed">
            Your payment was cancelled and your booking has not been confirmed. Any held seats will be released shortly.
          </p>

          <div className="flex flex-col sm:flex-row gap-4 w-full">
            <Link
              href="/"
              className="flex-1 bg-gradient-to-r from-[#00f0ff] to-[#7000ff] text-white font-bold py-4 px-6 rounded-xl hover:shadow-[0_4px_20px_rgba(112,0,255,0.4)] hover:-translate-y-1 transition-all"
            >
              Browse Events
            </Link>
            <Link
              href="/bookings"
              className="flex-1 bg-white/5 border border-white/10 text-white font-bold py-4 px-6 rounded-xl hover:bg-white/10 hover:-translate-y-1 transition-all"
            >
              My Bookings
            </Link>
          </div>
        </div>
      </div>
    </main>
  );
}
