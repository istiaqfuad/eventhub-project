"use client";

import Link from "next/link";
import { ArrowLeft, ArrowRight, MapPin, Building2, Music, Theater, Users } from "lucide-react";
import Navbar from "../components/Navbar/Navbar";
import { useVenues } from "../hooks/useVenues";
import type { LayoutType } from "../lib/types";

// Map layout types to icons, gradient colors, and descriptions
function getVenueStyle(layoutType: LayoutType) {
  switch (layoutType) {
    case "STADIUM":
      return {
        icon: <Users size={28} />,
        gradient: "linear-gradient(135deg, rgba(0, 200, 150, 0.15), rgba(0, 212, 255, 0.08))",
        border: "rgba(0, 200, 150, 0.2)",
        iconBg: "rgba(0, 200, 150, 0.15)",
        iconColor: "#00c896",
        accentColor: "#00c896",
        label: "Stadium",
        description: "Large-scale sporting & concert venue",
      };
    case "THEATER":
      return {
        icon: <Theater size={28} />,
        gradient: "linear-gradient(135deg, rgba(245, 200, 66, 0.12), rgba(240, 160, 60, 0.06))",
        border: "rgba(245, 200, 66, 0.2)",
        iconBg: "rgba(245, 200, 66, 0.15)",
        iconColor: "#f5c842",
        accentColor: "#f5c842",
        label: "Theater",
        description: "Intimate performances & stage shows",
      };
    case "CONFERENCE_HALL":
      return {
        icon: <Music size={28} />,
        gradient: "linear-gradient(135deg, rgba(108, 59, 255, 0.15), rgba(0, 212, 255, 0.08))",
        border: "rgba(108, 59, 255, 0.25)",
        iconBg: "rgba(108, 59, 255, 0.15)",
        iconColor: "#6c3bff",
        accentColor: "#6c3bff",
        label: "Conference Hall",
        description: "Professional events & conferences",
      };
    case "OPEN_GROUND":
      return {
        icon: <Building2 size={28} />,
        gradient: "linear-gradient(135deg, rgba(240, 64, 96, 0.12), rgba(245, 200, 66, 0.06))",
        border: "rgba(240, 64, 96, 0.2)",
        iconBg: "rgba(240, 64, 96, 0.12)",
        iconColor: "#f04060",
        accentColor: "#f04060",
        label: "Open Ground",
        description: "Outdoor festivals & large gatherings",
      };
    default:
      return {
        icon: <Building2 size={28} />,
        gradient: "linear-gradient(135deg, rgba(0, 212, 255, 0.1), rgba(108, 59, 255, 0.06))",
        border: "rgba(0, 212, 255, 0.15)",
        iconBg: "rgba(0, 212, 255, 0.1)",
        iconColor: "var(--accent-primary)",
        accentColor: "var(--accent-primary)",
        label: layoutType.replace(/_/g, " "),
        description: "Live event venue",
      };
  }
}

function VenueSkeleton() {
  return (
    <div style={{
      background: "var(--bg-secondary)",
      border: "1px solid var(--glass-border)",
      borderRadius: "var(--radius-lg)",
      padding: "1.5rem",
    }}>
      <div style={{ display: "flex", gap: "1rem", alignItems: "flex-start", marginBottom: "1.25rem" }}>
        <div className="skeleton" style={{ width: 56, height: 56, borderRadius: "var(--radius-md)", flexShrink: 0 }} />
        <div style={{ flex: 1 }}>
          <div className="skeleton" style={{ height: 22, width: "65%", marginBottom: "0.5rem" }} />
          <div className="skeleton" style={{ height: 16, width: "40%" }} />
        </div>
      </div>
      <div className="skeleton" style={{ height: 14, width: "80%", marginBottom: "0.5rem" }} />
      <div className="skeleton" style={{ height: 14, width: "55%" }} />
    </div>
  );
}

export default function VenuesPage() {
  const { data: venues, isLoading, error } = useVenues();

  return (
    <main style={{ minHeight: "100vh", background: "var(--bg-primary)" }}>
      <Navbar />

      <div className="container" style={{ paddingTop: 96, paddingBottom: "4rem" }}>

        {/* ── Header ── */}
        <Link href="/" style={{
          display: "inline-flex",
          alignItems: "center",
          gap: "0.4rem",
          color: "var(--text-muted)",
          fontSize: "0.875rem",
          marginBottom: "1.5rem",
          transition: "color 0.15s ease",
        }}>
          <ArrowLeft size={15} /> Back to Home
        </Link>

        <h1 style={{ fontSize: "2.25rem", marginBottom: "0.375rem" }}>Venues</h1>
        <p style={{ color: "var(--text-secondary)", marginBottom: "2.5rem" }}>
          Discover the stadiums, arenas, and theatres hosting EventHub shows.
        </p>

        {/* ── Content ── */}
        {isLoading ? (
          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(300px, 1fr))", gap: "1.25rem" }}>
            {[...Array(6)].map((_, i) => <VenueSkeleton key={i} />)}
          </div>
        ) : error ? (
          <div className="errorBox">Failed to load venues. Please try again later.</div>
        ) : venues && venues.length > 0 ? (
          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(300px, 1fr))", gap: "1.25rem" }}>
            {venues.map((v) => {
              const style = getVenueStyle(v.layoutType);
              const location = [v.address, v.city].filter(Boolean).join(", ") || "Location TBD";

              return (
                <Link
                  key={v.id}
                  href={`/venues/${v.id}`}
                  style={{
                    display: "flex",
                    flexDirection: "column",
                    background: "var(--bg-secondary)",
                    border: `1px solid var(--glass-border)`,
                    borderRadius: "var(--radius-lg)",
                    overflow: "hidden",
                    textDecoration: "none",
                    color: "inherit",
                    transition: "border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease",
                  }}
                  onMouseEnter={(e) => {
                    (e.currentTarget as HTMLAnchorElement).style.borderColor = style.border;
                    (e.currentTarget as HTMLAnchorElement).style.boxShadow = `0 8px 32px rgba(0,0,0,0.5)`;
                    (e.currentTarget as HTMLAnchorElement).style.transform = "translateY(-4px)";
                  }}
                  onMouseLeave={(e) => {
                    (e.currentTarget as HTMLAnchorElement).style.borderColor = "var(--glass-border)";
                    (e.currentTarget as HTMLAnchorElement).style.boxShadow = "none";
                    (e.currentTarget as HTMLAnchorElement).style.transform = "translateY(0)";
                  }}
                >
                  {/* Colored header banner */}
                  <div style={{
                    background: style.gradient,
                    padding: "1.5rem",
                    borderBottom: `1px solid ${style.border}`,
                    display: "flex",
                    alignItems: "center",
                    gap: "1rem",
                  }}>
                    <div style={{
                      width: 52,
                      height: 52,
                      background: style.iconBg,
                      border: `1px solid ${style.border}`,
                      borderRadius: "var(--radius-md)",
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                      color: style.iconColor,
                      flexShrink: 0,
                    }}>
                      {style.icon}
                    </div>
                    <div>
                      <div style={{ fontSize: "0.7rem", fontWeight: 700, letterSpacing: "0.08em", textTransform: "uppercase", color: style.accentColor, marginBottom: "0.2rem" }}>
                        {style.label}
                      </div>
                      <h3 style={{ fontSize: "1.2rem", fontWeight: 700, lineHeight: 1.2 }}>{v.name}</h3>
                    </div>
                  </div>

                  {/* Card body */}
                  <div style={{ padding: "1.25rem 1.5rem", flex: 1, display: "flex", flexDirection: "column", gap: "0.75rem" }}>
                    <p style={{ fontSize: "0.825rem", color: "var(--text-muted)", margin: 0 }}>
                      {style.description}
                    </p>

                    <div style={{ display: "flex", alignItems: "center", gap: "0.4rem", color: "var(--text-secondary)", fontSize: "0.875rem" }}>
                      <MapPin size={14} style={{ flexShrink: 0, color: "var(--text-muted)" }} />
                      <span style={{ overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                        {location}
                      </span>
                    </div>

                    <div style={{
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "flex-end",
                      marginTop: "auto",
                      paddingTop: "0.875rem",
                      borderTop: "1px solid var(--glass-border)",
                    }}>
                      <span style={{ display: "flex", alignItems: "center", gap: "0.35rem", fontSize: "0.8125rem", fontWeight: 600, color: style.accentColor }}>
                        View Venue <ArrowRight size={14} />
                      </span>
                    </div>
                  </div>
                </Link>
              );
            })}
          </div>
        ) : (
          <div style={{
            textAlign: "center",
            padding: "4rem 2rem",
            background: "var(--bg-secondary)",
            border: "1px solid var(--glass-border)",
            borderRadius: "var(--radius-xl)",
          }}>
            <Building2 size={48} style={{ opacity: 0.2, marginBottom: "1rem", display: "block", margin: "0 auto 1rem" }} />
            <h3 style={{ marginBottom: "0.5rem" }}>No venues listed yet</h3>
            <p>Check back soon for exciting new locations.</p>
          </div>
        )}
      </div>
    </main>
  );
}
