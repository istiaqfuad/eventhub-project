"use client";

import Link from "next/link";
import { ArrowLeft, MapPin, LayoutDashboard } from "lucide-react";
import Navbar from "../components/Navbar/Navbar";
import { useVenues } from "../hooks/useVenues";

export default function VenuesPage() {
  const { data: venues, isLoading, error } = useVenues();

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
        <h1 className="text-4xl font-bold mb-2">Venues</h1>
        <p className="text-gray-400 text-lg mb-8">
          Explore stadiums, theaters, and halls hosting EventHub shows.
        </p>

        {isLoading ? (
          <div className="text-center text-gray-400 py-12">Loading venues...</div>
        ) : error ? (
          <div className="bg-[#ff3366]/10 border border-[#ff3366]/30 text-[#ff8fab] p-4 rounded-lg">
            Failed to load venues.
          </div>
        ) : venues && venues.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {venues.map((v) => (
              <Link
                key={v.id}
                href={`/venues/${v.id}`}
                className="block p-6 bg-[#151a23] border border-white/10 rounded-2xl hover:border-[#00f0ff]/50 hover:-translate-y-1 hover:shadow-[0_4px_20px_rgba(0,240,255,0.1)] transition-all group relative overflow-hidden"
              >
                <div className="absolute top-0 right-0 p-4 opacity-10 group-hover:opacity-20 transition-opacity">
                  <LayoutDashboard size={80} className="text-[#00f0ff]" />
                </div>
                <h3 className="text-2xl font-bold mb-2 group-hover:text-[#00f0ff] transition-colors relative z-10">
                  {v.name}
                </h3>
                <div className="text-sm font-semibold text-transparent bg-clip-text bg-gradient-to-r from-[#00f0ff] to-[#7000ff] mb-4 uppercase tracking-wider relative z-10">
                  {v.layoutType.replaceAll("_", " ")}
                </div>
                <div className="flex items-center gap-2 text-gray-400 text-sm relative z-10">
                  <MapPin size={14} className="text-gray-500" />
                  {[v.address, v.city].filter(Boolean).join(", ") || "Location TBD"}
                </div>
              </Link>
            ))}
          </div>
        ) : (
          <div className="text-center text-gray-400 py-12 bg-[#151a23] border border-white/10 rounded-2xl">
            <LayoutDashboard size={48} className="mx-auto text-gray-500 mb-4 opacity-50" />
            <h3 className="text-xl font-bold mb-2">No venues listed yet</h3>
            <p className="text-gray-400">Check back later for exciting new locations.</p>
          </div>
        )}
      </div>
    </main>
  );
}
