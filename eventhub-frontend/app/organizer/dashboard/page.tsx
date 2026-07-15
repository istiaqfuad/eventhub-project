"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { BarChart3, CalendarPlus, MapPin, Ticket, TrendingUp, Users } from "lucide-react";
import Navbar from "../../components/Navbar";
import { useAuthStore } from "../../providers/auth-store-provider";

export default function OrganizerDashboard() {
  const { isAuthenticated, hydrated, user } = useAuthStore((s) => s);
  const router = useRouter();

  useEffect(() => {
    if (hydrated && !isAuthenticated) {
      router.push("/login?redirect=/organizer/dashboard");
    }
    // Ideally we would also check if user has ORGANIZER role
  }, [hydrated, isAuthenticated, router]);

  if (!hydrated) {
    return (
      <main className="min-h-screen bg-[#0b0e14] text-white">
        <Navbar />
      </main>
    );
  }

  // Placeholder stats for the dashboard (would come from an API)
  const stats = [
    { label: "Total Revenue", value: "$12,450", icon: TrendingUp, color: "text-[#00e676]" },
    { label: "Tickets Sold", value: "342", icon: Ticket, color: "text-[#00f0ff]" },
    { label: "Active Events", value: "3", icon: BarChart3, color: "text-[#ffea00]" },
    { label: "Attendees", value: "890", icon: Users, color: "text-[#7000ff]" },
  ];

  return (
    <main className="min-h-screen bg-[#0b0e14] text-white">
      <Navbar />
      <div className="container mx-auto px-6 pt-[100px] pb-12">
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 gap-4">
          <div>
            <h1 className="text-4xl font-bold mb-2">Organizer Dashboard</h1>
            <p className="text-gray-400">Welcome back, {user?.email || "Organizer"}. Here's your overview.</p>
          </div>
          <div className="flex gap-3">
            <Link
              href="/organizer/venues/new"
              className="bg-[#202632] border border-white/10 text-white font-semibold py-2.5 px-5 rounded-lg hover:bg-white/10 transition-all flex items-center gap-2"
            >
              <MapPin size={18} />
              New Venue
            </Link>
            <Link
              href="/organizer/events/new"
              className="bg-gradient-to-r from-[#00f0ff] to-[#7000ff] text-white font-semibold py-2.5 px-5 rounded-lg hover:shadow-[0_4px_15px_rgba(112,0,255,0.4)] transition-all flex items-center gap-2"
            >
              <CalendarPlus size={18} />
              Create Event
            </Link>
          </div>
        </div>

        {/* Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-12">
          {stats.map((stat, i) => (
            <div key={i} className="bg-[#151a23] border border-white/10 p-6 rounded-2xl flex items-center gap-4">
              <div className={`p-4 rounded-xl bg-white/5 ${stat.color}`}>
                <stat.icon size={28} />
              </div>
              <div>
                <div className="text-gray-400 text-sm font-medium mb-1">{stat.label}</div>
                <div className="text-3xl font-bold">{stat.value}</div>
              </div>
            </div>
          ))}
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Recent Events */}
          <div className="lg:col-span-2 bg-[#151a23] border border-white/10 rounded-2xl p-8">
            <h2 className="text-2xl font-bold mb-6">Your Events</h2>
            <div className="text-center text-gray-500 py-12 border-2 border-dashed border-white/10 rounded-xl">
              <CalendarPlus size={48} className="mx-auto mb-4 opacity-50" />
              <h3 className="text-xl font-medium text-gray-300 mb-2">No events created yet</h3>
              <p className="mb-6 max-w-sm mx-auto">Create your first event to start selling tickets and tracking revenue.</p>
              <Link
                href="/organizer/events/new"
                className="inline-block bg-white/10 text-white font-semibold py-2 px-6 rounded-lg hover:bg-white/20 transition-all"
              >
                Create Event
              </Link>
            </div>
          </div>

          {/* Quick Actions / Venues */}
          <div className="bg-[#151a23] border border-white/10 rounded-2xl p-8">
            <h2 className="text-xl font-bold mb-6">Managed Venues</h2>
            <div className="flex flex-col gap-3">
              <div className="p-4 rounded-xl border border-white/5 bg-[#202632] flex justify-between items-center">
                <div>
                  <div className="font-semibold text-white">Madison Square Garden</div>
                  <div className="text-xs text-gray-400">New York, NY</div>
                </div>
                <div className="text-[#00f0ff] text-sm">Active</div>
              </div>
              <div className="p-4 rounded-xl border border-white/5 bg-[#202632] flex justify-between items-center">
                <div>
                  <div className="font-semibold text-white">Staples Center</div>
                  <div className="text-xs text-gray-400">Los Angeles, CA</div>
                </div>
                <div className="text-[#00f0ff] text-sm">Active</div>
              </div>
              <Link
                href="/organizer/venues/new"
                className="mt-2 text-center block w-full py-3 rounded-xl border border-dashed border-white/20 text-gray-400 hover:text-white hover:border-white/50 transition-all text-sm font-medium"
              >
                + Add Venue
              </Link>
            </div>
          </div>
        </div>
      </div>
    </main>
  );
}
