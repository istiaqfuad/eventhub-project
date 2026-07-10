"use client";

import { use } from "react";
import Link from "next/link";
import { ArrowLeft, MapPin } from "lucide-react";
import Navbar from "../../components/Navbar";
import { useVenue, useVenueLayout } from "../../hooks/useVenues";
import InteractiveSeatMap from "../../components/SeatMap/InteractiveSeatMap";

export default function VenueDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const { data: venue, isLoading, error } = useVenue(id);
  const { data: layout, isLoading: layoutLoading } = useVenueLayout(id);

  return (
    <main>
      <Navbar />
      <div className="container" style={{ paddingTop: "100px", minHeight: "100vh", paddingBottom: "3rem" }}>
        <Link
          href="/venues"
          style={{
            color: "var(--text-secondary)",
            display: "inline-flex",
            alignItems: "center",
            gap: "0.5rem",
            marginBottom: "1rem",
          }}
        >
          <ArrowLeft size={16} /> All venues
        </Link>

        {isLoading ? (
          <div style={{ color: "var(--text-secondary)" }}>Loading...</div>
        ) : error || !venue ? (
          <div className="errorBox">Venue not found.</div>
        ) : (
          <>
            <h1 style={{ fontSize: "2.5rem", marginBottom: "0.5rem" }}>{venue.name}</h1>
            <p style={{ color: "var(--accent-primary)", marginBottom: "0.5rem" }}>
              {venue.layoutType.replaceAll("_", " ")}
            </p>
            <p
              style={{
                display: "flex",
                alignItems: "center",
                gap: "0.35rem",
                color: "var(--text-secondary)",
                marginBottom: "2rem",
              }}
            >
              <MapPin size={16} />
              {[venue.address, venue.city].filter(Boolean).join(", ") || "Location TBD"}
            </p>

            <h2 style={{ marginBottom: "1rem" }}>Layout</h2>
            {layoutLoading ? (
              <div style={{ color: "var(--text-secondary)" }}>Loading layout...</div>
            ) : layout ? (
              <>
                <p style={{ color: "var(--text-secondary)", marginBottom: "1rem" }}>
                  {layout.sections.length} section(s) ·{" "}
                  {layout.sections.reduce((n, s) => n + (s.seats?.length ?? 0), 0)} seats
                </p>
                <InteractiveSeatMap
                  sections={layout.sections}
                  selectedSeats={new Set()}
                  onSeatToggle={() => {}}
                />
              </>
            ) : (
              <div style={{ color: "var(--text-secondary)" }}>Layout unavailable.</div>
            )}
          </>
        )}
      </div>
    </main>
  );
}
