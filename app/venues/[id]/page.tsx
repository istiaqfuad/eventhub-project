"use client";

import { use } from "react";
import Link from "next/link";
import { ArrowLeft, MapPin, LayoutDashboard } from "lucide-react";
import Navbar from "../../components/Navbar";
import { useVenue, useVenueLayout } from "../../hooks/useVenues";
import InteractiveSeatMap from "../../components/SeatMap/InteractiveSeatMap";

export default function VenueDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const { data: venue, isLoading, error } = useVenue(id);
  const { data: layout, isLoading: layoutLoading } = useVenueLayout(id);

  return (
    <main className="min-h-screen bg-[#0b0e14] text-white">
      <Navbar />
      <div className="container mx-auto px-6 pt-[100px] pb-12">
        <Link
          href="/venues"
          className="inline-flex items-center gap-2 mb-6 text-gray-400 hover:text-[#00f0ff] transition-colors"
        >
          <ArrowLeft size={16} /> All venues
        </Link>

        {isLoading ? (
          <div className="flex items-center justify-center min-h-[50vh] text-gray-400">Loading...</div>
        ) : error || !venue ? (
          <div className="bg-[#ff3366]/10 border border-[#ff3366]/30 text-[#ff8fab] p-4 rounded-lg inline-block">
            Venue not found.
          </div>
        ) : (
          <>
            <div className="bg-gradient-to-br from-[#1a2130] to-[#151a23] border border-white/10 rounded-3xl p-8 md:p-12 mb-8 relative overflow-hidden shadow-xl">
              <div className="absolute top-0 right-0 p-8 opacity-5">
                <LayoutDashboard size={200} />
              </div>
              <h1 className="text-4xl md:text-5xl font-black mb-4 relative z-10">{venue.name}</h1>
              <p className="text-lg font-bold text-transparent bg-clip-text bg-gradient-to-r from-[#00f0ff] to-[#7000ff] mb-4 uppercase tracking-wider relative z-10">
                {venue.layoutType.replaceAll("_", " ")}
              </p>
              <p className="flex items-center gap-2 text-gray-300 text-lg relative z-10">
                <MapPin size={18} className="text-[#00f0ff]" />
                {[venue.address, venue.city].filter(Boolean).join(", ") || "Location TBD"}
              </p>
            </div>

            <div className="bg-[#151a23] border border-white/10 rounded-2xl p-8 shadow-lg">
              <h2 className="text-2xl font-bold mb-6">Interactive Layout</h2>
              {layoutLoading ? (
                <div className="text-center text-gray-400 py-12">Loading layout...</div>
              ) : layout ? (
                <>
                  <p className="text-gray-400 mb-8 font-medium">
                    <span className="text-white font-bold">{layout.sections.length}</span> section(s) ·{" "}
                    <span className="text-white font-bold">{layout.sections.reduce((n, s) => n + (s.seats?.length ?? 0), 0)}</span> total seats
                  </p>
                  <div className="bg-[#0b0e14] border border-white/5 rounded-xl p-6">
                    <InteractiveSeatMap
                      sections={layout.sections}
                      selectedSeats={new Set()}
                      onSeatToggle={() => {}}
                    />
                  </div>
                </>
              ) : (
                <div className="text-center text-gray-400 py-12 bg-white/5 rounded-xl">Layout unavailable for this venue.</div>
              )}
            </div>
          </>
        )}
      </div>
    </main>
  );
}
