"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { ArrowLeft, CalendarPlus, MapPin, Tag } from "lucide-react";
import Navbar from "../../../components/Navbar";
import apiClient, { getProblemDetail } from "../../../lib/api-client";
import { useVenues } from "../../../hooks/useVenues";

export default function NewEventPage() {
  const router = useRouter();
  const { data: venues } = useVenues();
  
  const [formData, setFormData] = useState({
    title: "",
    description: "",
    venueId: "",
    city: "",
    startsAt: "",
    endsAt: "",
    imageUrls: "", // comma separated
  });
  
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsSubmitting(true);
    
    try {
      // Format payload for backend
      const payload = {
        title: formData.title,
        description: formData.description || undefined,
        venueId: formData.venueId ? parseInt(formData.venueId) : undefined,
        city: formData.city || undefined,
        // Backend expects ISO 8601 offset datetime, inputs provide local datetime (e.g. 2024-05-12T14:30)
        // Quickest way for local test is to append Z or offset
        startsAt: new Date(formData.startsAt).toISOString(),
        endsAt: new Date(formData.endsAt).toISOString(),
        imageUrls: formData.imageUrls ? formData.imageUrls.split(",").map(s => s.trim()).filter(Boolean) : [],
        tagIds: [], // Tags/categories omitted for simple demo
      };

      await apiClient.post("/events", payload);
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
      <div className="container mx-auto px-6 pt-[100px] pb-12 max-w-3xl">
        <Link
          href="/organizer/dashboard"
          className="inline-flex items-center gap-2 mb-6 text-gray-400 hover:text-[#00f0ff] transition-colors"
        >
          <ArrowLeft size={16} /> Back to Dashboard
        </Link>
        <h1 className="text-4xl font-bold mb-2">Create New Event</h1>
        <p className="text-gray-400 text-lg mb-8">Draft a new event to sell tickets.</p>

        <form onSubmit={handleSubmit} className="bg-[#151a23] border border-white/10 p-8 rounded-2xl shadow-xl flex flex-col gap-6">
          <div>
            <label className="block text-sm font-semibold mb-2">Event Title</label>
            <input
              type="text"
              required
              className="w-full bg-[#0b0e14] border border-white/10 rounded-xl px-4 py-3 focus:outline-none focus:border-[#00f0ff] transition-colors"
              placeholder="e.g. Coldplay: Music of the Spheres"
              value={formData.title}
              onChange={(e) => setFormData({ ...formData, title: e.target.value })}
            />
          </div>

          <div>
            <label className="block text-sm font-semibold mb-2">Description</label>
            <textarea
              rows={4}
              className="w-full bg-[#0b0e14] border border-white/10 rounded-xl px-4 py-3 focus:outline-none focus:border-[#00f0ff] transition-colors resize-y"
              placeholder="Tell attendees what to expect..."
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 border-y border-white/5 py-6 my-2">
            <div>
              <label className="block text-sm font-semibold mb-2">Start Time</label>
              <input
                type="datetime-local"
                required
                className="w-full bg-[#0b0e14] border border-white/10 rounded-xl px-4 py-3 focus:outline-none focus:border-[#00f0ff] transition-colors text-white"
                value={formData.startsAt}
                onChange={(e) => setFormData({ ...formData, startsAt: e.target.value })}
              />
            </div>
            <div>
              <label className="block text-sm font-semibold mb-2">End Time</label>
              <input
                type="datetime-local"
                required
                className="w-full bg-[#0b0e14] border border-white/10 rounded-xl px-4 py-3 focus:outline-none focus:border-[#00f0ff] transition-colors text-white"
                value={formData.endsAt}
                onChange={(e) => setFormData({ ...formData, endsAt: e.target.value })}
              />
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label className="block text-sm font-semibold mb-2">Venue (Optional)</label>
              <select
                className="w-full bg-[#0b0e14] border border-white/10 rounded-xl px-4 py-3 focus:outline-none focus:border-[#00f0ff] transition-colors"
                value={formData.venueId}
                onChange={(e) => {
                  setFormData({ ...formData, venueId: e.target.value });
                  // Auto-fill city if venue selected
                  if (e.target.value && venues) {
                    const v = venues.find(x => String(x.id) === e.target.value);
                    if (v && v.city) {
                      setFormData(prev => ({ ...prev, city: v.city }));
                    }
                  }
                }}
              >
                <option value="">-- Select a predefined venue --</option>
                {venues?.map(v => (
                  <option key={v.id} value={v.id}>{v.name} ({v.city})</option>
                ))}
              </select>
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

          <div>
            <label className="block text-sm font-semibold mb-2">Image URLs (comma separated)</label>
            <input
              type="text"
              className="w-full bg-[#0b0e14] border border-white/10 rounded-xl px-4 py-3 focus:outline-none focus:border-[#00f0ff] transition-colors"
              placeholder="https://example.com/poster.jpg"
              value={formData.imageUrls}
              onChange={(e) => setFormData({ ...formData, imageUrls: e.target.value })}
            />
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
                  <CalendarPlus size={20} />
                  Save as Draft
                </>
              )}
            </button>
            <p className="text-center text-xs text-gray-500 mt-4">
              Events are saved as DRAFT by default. You can add ticket types later.
            </p>
          </div>
        </form>
      </div>
    </main>
  );
}
