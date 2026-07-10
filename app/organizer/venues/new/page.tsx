"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { ArrowLeft, MapPin } from "lucide-react";
import Navbar from "../../../components/Navbar";
import apiClient, { getProblemDetail } from "../../../lib/api-client";

export default function NewVenuePage() {
  const router = useRouter();
  const [formData, setFormData] = useState({
    name: "",
    layoutType: "STADIUM",
    address: "",
    city: "",
  });
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsSubmitting(true);
    try {
      await apiClient.post("/venues", formData);
      router.push("/organizer/dashboard");
    } catch (err) {
      setError(getProblemDetail(err));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <main className="min-h-screen bg-[#0b0e14] text-white">
      <Navbar />
      <div className="container mx-auto px-6 pt-[100px] pb-12 max-w-2xl">
        <Link
          href="/organizer/dashboard"
          className="inline-flex items-center gap-2 mb-6 text-gray-400 hover:text-[#00f0ff] transition-colors"
        >
          <ArrowLeft size={16} /> Back to Dashboard
        </Link>
        <h1 className="text-4xl font-bold mb-2">Create New Venue</h1>
        <p className="text-gray-400 text-lg mb-8">Add a new location to host events.</p>

        <form onSubmit={handleSubmit} className="bg-[#151a23] border border-white/10 p-8 rounded-2xl shadow-xl flex flex-col gap-6">
          <div>
            <label className="block text-sm font-semibold mb-2">Venue Name</label>
            <input
              type="text"
              required
              className="w-full bg-[#0b0e14] border border-white/10 rounded-xl px-4 py-3 focus:outline-none focus:border-[#00f0ff] transition-colors"
              placeholder="e.g. Madison Square Garden"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            />
          </div>

          <div>
            <label className="block text-sm font-semibold mb-2">Layout Type</label>
            <select
              className="w-full bg-[#0b0e14] border border-white/10 rounded-xl px-4 py-3 focus:outline-none focus:border-[#00f0ff] transition-colors"
              value={formData.layoutType}
              onChange={(e) => setFormData({ ...formData, layoutType: e.target.value })}
            >
              <option value="STADIUM">Stadium</option>
              <option value="THEATER">Theater</option>
              <option value="CONFERENCE_HALL">Conference Hall</option>
              <option value="OPEN_GROUND">Open Ground (GA only)</option>
            </select>
            <p className="text-xs text-gray-500 mt-2">The layout dictates whether you can add reserved seats or just GA tickets.</p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label className="block text-sm font-semibold mb-2">Address</label>
              <input
                type="text"
                className="w-full bg-[#0b0e14] border border-white/10 rounded-xl px-4 py-3 focus:outline-none focus:border-[#00f0ff] transition-colors"
                placeholder="e.g. 4 Pennsylvania Plaza"
                value={formData.address}
                onChange={(e) => setFormData({ ...formData, address: e.target.value })}
              />
            </div>
            <div>
              <label className="block text-sm font-semibold mb-2">City</label>
              <input
                type="text"
                className="w-full bg-[#0b0e14] border border-white/10 rounded-xl px-4 py-3 focus:outline-none focus:border-[#00f0ff] transition-colors"
                placeholder="e.g. New York, NY"
                value={formData.city}
                onChange={(e) => setFormData({ ...formData, city: e.target.value })}
              />
            </div>
          </div>

          {error && (
            <div className="bg-[#ff3366]/10 border border-[#ff3366]/30 text-[#ff8fab] p-4 rounded-lg text-sm">
              {error}
            </div>
          )}

          <div className="pt-4 mt-2 border-t border-white/10">
            <button
              type="submit"
              disabled={isSubmitting}
              className="w-full bg-gradient-to-r from-[#00f0ff] to-[#7000ff] text-white font-bold py-4 px-6 rounded-xl hover:shadow-[0_4px_20px_rgba(112,0,255,0.4)] hover:-translate-y-1 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
            >
              {isSubmitting ? "Creating..." : (
                <>
                  <MapPin size={20} />
                  Save Venue
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </main>
  );
}
