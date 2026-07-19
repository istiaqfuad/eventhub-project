"use client";

import { use } from "react";
import Link from "next/link";
import { ArrowLeft, MapPin, Building2, Music, Theater, Users } from "lucide-react";
import Navbar from "../../components/Navbar/Navbar";
import { useVenue, useVenueLayout } from "../../hooks/useVenues";
import InteractiveSeatMap from "../../components/SeatMap/InteractiveSeatMap";
import type { LayoutType } from "../../lib/types";

// Reuse the same style function
function getVenueStyle(layoutType: LayoutType) {
  switch (layoutType) {
    case "STADIUM":
      return {
        icon: <Users size={32} />,
        gradient: "linear-gradient(135deg, rgba(0, 200, 150, 0.15), rgba(0, 212, 255, 0.08))",
        bannerGradient: "linear-gradient(135deg, rgba(0, 200, 150, 0.25), rgba(0, 212, 255, 0.08))",
        border: "rgba(0, 200, 150, 0.3)",
        iconBg: "rgba(0, 200, 150, 0.15)",
        iconColor: "#00c896",
        accentColor: "#00c896",
        label: "Stadium",
      };
    case "THEATER":
      return {
        icon: <Theater size={32} />,
        gradient: "linear-gradient(135deg, rgba(245, 200, 66, 0.12), rgba(240, 160, 60, 0.06))",
        bannerGradient: "linear-gradient(135deg, rgba(245, 200, 66, 0.2), rgba(240, 160, 60, 0.08))",
        border: "rgba(245, 200, 66, 0.3)",
        iconBg: "rgba(245, 200, 66, 0.15)",
        iconColor: "#f5c842",
        accentColor: "#f5c842",
        label: "Theater",
      };
    case "CONFERENCE_HALL":
      return {
        icon: <Music size={32} />,
        gradient: "linear-gradient(135deg, rgba(108, 59, 255, 0.15), rgba(0, 212, 255, 0.08))",
        bannerGradient: "linear-gradient(135deg, rgba(108, 59, 255, 0.25), rgba(0, 212, 255, 0.08))",
        border: "rgba(108, 59, 255, 0.3)",
        iconBg: "rgba(108, 59, 255, 0.15)",
        iconColor: "#6c3bff",
        accentColor: "#6c3bff",
        label: "Conference Hall",
      };
    case "OPEN_GROUND":
      return {
        icon: <Building2 size={32} />,
        gradient: "linear-gradient(135deg, rgba(240, 64, 96, 0.12), rgba(245, 200, 66, 0.06))",
        bannerGradient: "linear-gradient(135deg, rgba(240, 64, 96, 0.25), rgba(245, 200, 66, 0.08))",
        border: "rgba(240, 64, 96, 0.3)",
        iconBg: "rgba(240, 64, 96, 0.12)",
        iconColor: "#f04060",
        accentColor: "#f04060",
        label: "Open Ground",
      };
    default:
      return {
        icon: <Building2 size={32} />,
        gradient: "linear-gradient(135deg, rgba(0, 212, 255, 0.1), rgba(108, 59, 255, 0.06))",
        bannerGradient: "linear-gradient(135deg, rgba(0, 212, 255, 0.2), rgba(108, 59, 255, 0.08))",
        border: "rgba(0, 212, 255, 0.2)",
        iconBg: "rgba(0, 212, 255, 0.1)",
        iconColor: "var(--accent-primary)",
        accentColor: "var(--accent-primary)",
        label: (layoutType as string)?.replace(/_/g, " ") || "Venue",
      };
  }
}

export default function VenueDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const { data: venue, isLoading, error } = useVenue(id);
  const { data: layout, isLoading: layoutLoading } = useVenueLayout(id);

  const style = venue ? getVenueStyle(venue.layoutType) : null;

  return (
    <main style={{ minHeight: "100vh", background: "var(--bg-primary)" }}>
      <Navbar />

      <div className="container" style={{ paddingTop: 96, paddingBottom: "4rem" }}>
        
        {/* ── Back Link ── */}
        <Link href="/venues" style={{
          display: "inline-flex",
          alignItems: "center",
          gap: "0.4rem",
          color: "var(--text-muted)",
          fontSize: "0.875rem",
          marginBottom: "1.5rem",
          transition: "color 0.15s ease",
        }}>
          <ArrowLeft size={15} /> All venues
        </Link>

        {isLoading ? (
          <div>
            <div className="skeleton" style={{ height: 220, borderRadius: "var(--radius-xl)", marginBottom: "2rem" }} />
            <div className="skeleton" style={{ height: 400, borderRadius: "var(--radius-xl)" }} />
          </div>
        ) : error || !venue || !style ? (
          <div className="errorBox">Venue not found or failed to load.</div>
        ) : (
          <>
            {/* ── Venue Header Banner ── */}
            <div style={{
              background: style.bannerGradient,
              border: `1px solid ${style.border}`,
              borderRadius: "var(--radius-xl)",
              padding: "2.5rem 3rem",
              marginBottom: "2rem",
              position: "relative",
              overflow: "hidden",
              boxShadow: "0 12px 40px rgba(0,0,0,0.4)",
            }}>
              {/* Background watermark icon */}
              <div style={{
                position: "absolute",
                right: "10%",
                top: "50%",
                transform: "translateY(-50%)",
                opacity: 0.05,
                color: style.iconColor,
                pointerEvents: "none",
              }}>
                <div style={{ transform: "scale(6)" }}>{style.icon}</div>
              </div>

              <div style={{ position: "relative", zIndex: 1, display: "flex", gap: "1.5rem", alignItems: "center" }}>
                <div style={{
                  width: 80,
                  height: 80,
                  background: style.iconBg,
                  border: `1px solid ${style.border}`,
                  borderRadius: "var(--radius-lg)",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  color: style.iconColor,
                  flexShrink: 0,
                  boxShadow: `0 8px 32px ${style.iconBg}`,
                }}>
                  {style.icon}
                </div>

                <div>
                  <div style={{
                    fontSize: "0.85rem",
                    fontWeight: 700,
                    letterSpacing: "0.1em",
                    textTransform: "uppercase",
                    color: style.accentColor,
                    marginBottom: "0.5rem"
                  }}>
                    {style.label}
                  </div>
                  <h1 style={{ fontSize: "3rem", fontWeight: 800, marginBottom: "0.75rem", lineHeight: 1.1 }}>
                    {venue.name}
                  </h1>
                  <div style={{ display: "flex", alignItems: "center", gap: "0.5rem", color: "var(--text-secondary)", fontSize: "1.05rem" }}>
                    <MapPin size={18} style={{ color: style.accentColor }} />
                    {[venue.address, venue.city].filter(Boolean).join(", ") || "Location TBD"}
                  </div>
                </div>
              </div>
            </div>

            {/* ── Interactive Layout Section ── */}
            <div style={{
              background: "var(--bg-secondary)",
              border: "1px solid var(--glass-border)",
              borderRadius: "var(--radius-xl)",
              padding: "2.5rem",
              boxShadow: "var(--shadow-md)",
            }}>
              <h2 style={{ fontSize: "1.5rem", marginBottom: "1.5rem" }}>Interactive Layout</h2>
              
              {layoutLoading ? (
                <div style={{ padding: "4rem 0", display: "flex", justifyContent: "center" }}>
                  <div className="skeleton" style={{ height: 300, width: "100%", borderRadius: "var(--radius-lg)" }} />
                </div>
              ) : layout ? (
                <>
                  <div style={{
                    display: "flex",
                    alignItems: "center",
                    gap: "1.5rem",
                    marginBottom: "1.5rem",
                    padding: "1rem 1.5rem",
                    background: "var(--bg-tertiary)",
                    borderRadius: "var(--radius-md)",
                    border: "1px solid var(--glass-border)",
                  }}>
                    <div>
                      <span style={{ fontSize: "1.25rem", fontWeight: 700, color: "var(--text-primary)" }}>
                        {layout.sections.length}
                      </span>
                      <span style={{ color: "var(--text-muted)", marginLeft: "0.4rem", fontSize: "0.9rem" }}>Sections</span>
                    </div>
                    <div style={{ width: 1, height: 24, background: "var(--glass-border)" }} />
                    <div>
                      <span style={{ fontSize: "1.25rem", fontWeight: 700, color: "var(--text-primary)" }}>
                        {layout.sections.reduce((n, s) => n + (s.seats?.length ?? 0), 0)}
                      </span>
                      <span style={{ color: "var(--text-muted)", marginLeft: "0.4rem", fontSize: "0.9rem" }}>Total Seats</span>
                    </div>
                  </div>

                  <div style={{
                    background: "var(--bg-primary)",
                    border: "1px solid var(--glass-border)",
                    borderRadius: "var(--radius-lg)",
                    padding: "1.5rem",
                    overflow: "hidden",
                  }}>
                    <InteractiveSeatMap
                      sections={layout.sections}
                      selectedSeats={new Set()}
                      onSeatToggle={() => {}}
                    />
                  </div>
                </>
              ) : (
                <div style={{
                  padding: "4rem 2rem",
                  textAlign: "center",
                  background: "var(--bg-tertiary)",
                  borderRadius: "var(--radius-lg)",
                  border: "1px dashed var(--glass-border)",
                  color: "var(--text-muted)",
                }}>
                  Layout unavailable for this venue.
                </div>
              )}
            </div>
          </>
        )}
      </div>
    </main>
  );
}
