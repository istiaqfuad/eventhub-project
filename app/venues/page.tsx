"use client";

import Link from "next/link";
import { ArrowLeft, MapPin } from "lucide-react";
import Navbar from "../components/Navbar";
import { useVenues } from "../hooks/useVenues";

export default function VenuesPage() {
  const { data: venues, isLoading, error } = useVenues();

  return (
    <main>
      <Navbar />
      <div className="container" style={{ paddingTop: "100px", minHeight: "100vh", paddingBottom: "3rem" }}>
        <Link
          href="/"
          style={{
            color: "var(--text-secondary)",
            display: "inline-flex",
            alignItems: "center",
            gap: "0.5rem",
            marginBottom: "1rem",
          }}
        >
          <ArrowLeft size={16} /> Home
        </Link>
        <h1 style={{ fontSize: "2.5rem", marginBottom: "0.5rem" }}>Venues</h1>
        <p style={{ color: "var(--text-secondary)", marginBottom: "2rem" }}>
          Explore stadiums, theaters, and halls hosting EventHub shows.
        </p>

        {isLoading ? (
          <div style={{ color: "var(--text-secondary)" }}>Loading venues...</div>
        ) : error ? (
          <div className="errorBox">Failed to load venues.</div>
        ) : venues && venues.length > 0 ? (
          <div
            style={{
              display: "grid",
              gridTemplateColumns: "repeat(auto-fill, minmax(280px, 1fr))",
              gap: "1.25rem",
            }}
          >
            {venues.map((v) => (
              <Link
                key={v.id}
                href={`/venues/${v.id}`}
                style={{
                  display: "block",
                  padding: "1.5rem",
                  background: "var(--bg-secondary)",
                  border: "1px solid var(--glass-border)",
                  borderRadius: "var(--radius-lg)",
                  textDecoration: "none",
                  color: "inherit",
                }}
              >
                <h3 style={{ marginBottom: "0.5rem" }}>{v.name}</h3>
                <div
                  style={{
                    fontSize: "0.85rem",
                    color: "var(--accent-primary)",
                    marginBottom: "0.75rem",
                  }}
                >
                  {v.layoutType.replaceAll("_", " ")}
                </div>
                <div
                  style={{
                    display: "flex",
                    alignItems: "center",
                    gap: "0.35rem",
                    color: "var(--text-secondary)",
                    fontSize: "0.9rem",
                  }}
                >
                  <MapPin size={14} />
                  {[v.address, v.city].filter(Boolean).join(", ") || "Location TBD"}
                </div>
              </Link>
            ))}
          </div>
        ) : (
          <div style={{ color: "var(--text-secondary)" }}>No venues listed yet.</div>
        )}
      </div>
    </main>
  );
}
