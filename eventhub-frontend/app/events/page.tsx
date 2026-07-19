"use client";

import Link from "next/link";
import { ArrowRight, Calendar, Clock, MapPin, Search, SlidersHorizontal, Ticket } from "lucide-react";
import Navbar from "../components/Navbar";
import { useEvents } from "../hooks/useEvents";
import styles from "../page.module.css";
import { useState, useMemo } from "react";

function statusLabel(status: string) {
  switch (status) {
    case "ON_SALE": return "On Sale";
    case "DRAFT": return "Draft";
    case "CLOSED": return "Closed";
    case "CANCELLED": return "Cancelled";
    default: return status;
  }
}

function statusBadgeClass(status: string) {
  switch (status) {
    case "ON_SALE": return "badge badge-success";
    case "CANCELLED": return "badge badge-danger";
    default: return "badge badge-neutral";
  }
}

function EventSkeleton() {
  return (
    <div className={styles.eventCard} style={{ pointerEvents: "none" }}>
      <div className="skeleton" style={{ height: 195 }} />
      <div style={{ padding: "1.125rem", display: "flex", flexDirection: "column", gap: "0.75rem" }}>
        <div className="skeleton" style={{ height: 18, width: "72%" }} />
        <div className="skeleton" style={{ height: 14, width: "48%" }} />
        <div className="skeleton" style={{ height: 14, width: "56%" }} />
        <div style={{ display: "flex", justifyContent: "space-between", marginTop: "0.25rem" }}>
          <div className="skeleton" style={{ height: 22, width: 64, borderRadius: 99 }} />
          <div className="skeleton" style={{ height: 18, width: 88 }} />
        </div>
      </div>
    </div>
  );
}

export default function EventsCatalog() {
  const { data: events, isLoading, error } = useEvents();
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");

  const filtered = useMemo(() => {
    if (!events) return [];
    return events.filter((e) => {
      if (e.status === "DRAFT") return false;
      const matchesSearch =
        search.trim() === "" ||
        e.title.toLowerCase().includes(search.toLowerCase()) ||
        (e.city ?? "").toLowerCase().includes(search.toLowerCase());
      const matchesStatus = statusFilter === "ALL" || e.status === statusFilter;
      return matchesSearch && matchesStatus;
    });
  }, [events, search, statusFilter]);

  return (
    <main>
      <Navbar />

      <div className="container" style={{ paddingTop: "96px", paddingBottom: "4rem", minHeight: "100vh" }}>

        {/* ── Header ── */}
        <div style={{ marginBottom: "2rem" }}>
          <h1 style={{ fontSize: "2.25rem", marginBottom: "0.375rem" }}>Upcoming Events</h1>
          <p style={{ color: "var(--text-secondary)", margin: 0 }}>
            Browse the full catalog of concerts, sports, and live shows.
          </p>
        </div>

        {/* ── Search & Filter Bar ── */}
        <div style={{
          display: "flex",
          gap: "0.75rem",
          marginBottom: "2rem",
          flexWrap: "wrap",
          alignItems: "center",
        }}>
          <div style={{
            flex: 1,
            minWidth: 200,
            display: "flex",
            alignItems: "center",
            gap: "0.625rem",
            background: "var(--bg-secondary)",
            border: "1.5px solid var(--glass-border)",
            borderRadius: "var(--radius-md)",
            padding: "0.6rem 1rem",
            transition: "border-color 0.15s ease",
          }}>
            <Search size={16} color="var(--text-muted)" style={{ flexShrink: 0 }} />
            <input
              type="text"
              placeholder="Search events or cities..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              style={{
                background: "transparent",
                border: "none",
                outline: "none",
                color: "var(--text-primary)",
                fontSize: "0.9rem",
                width: "100%",
                padding: 0,
              }}
            />
          </div>

          <div style={{ display: "flex", alignItems: "center", gap: "0.5rem", flexWrap: "wrap" }}>
            <SlidersHorizontal size={15} color="var(--text-muted)" />
            {["ALL", "ON_SALE", "CLOSED"].map((s) => (
              <button
                key={s}
                onClick={() => setStatusFilter(s)}
                type="button"
                style={{
                  padding: "0.45rem 0.875rem",
                  borderRadius: "var(--radius-full)",
                  border: `1.5px solid ${statusFilter === s ? "var(--accent-primary)" : "var(--glass-border)"}`,
                  background: statusFilter === s ? "rgba(0, 212, 255, 0.1)" : "var(--bg-secondary)",
                  color: statusFilter === s ? "var(--accent-primary)" : "var(--text-secondary)",
                  fontSize: "0.8rem",
                  fontWeight: 600,
                  cursor: "pointer",
                  transition: "all 0.15s ease",
                  fontFamily: "inherit",
                }}
              >
                {s === "ALL" ? "All" : s === "ON_SALE" ? "On Sale" : "Closed"}
              </button>
            ))}
          </div>
        </div>

        {/* ── Events Grid ── */}
        {isLoading ? (
          <div className={styles.eventsGrid}>
            {[...Array(9)].map((_, i) => <EventSkeleton key={i} />)}
          </div>
        ) : error ? (
          <div className="errorBox" style={{ textAlign: "center" }}>
            Failed to load events. Please try again later.
          </div>
        ) : filtered.length > 0 ? (
          <>
            <p style={{ fontSize: "0.875rem", color: "var(--text-muted)", marginBottom: "1rem" }}>
              {filtered.length} event{filtered.length !== 1 ? "s" : ""} found
            </p>
            <div className={styles.eventsGrid}>
              {filtered.map((event) => {
                const eventDate = new Date(event.startsAt);
                const formattedDate = eventDate.toLocaleDateString("en-US", { month: "short", day: "numeric" });
                const formattedTime = eventDate.toLocaleTimeString("en-US", { hour: "numeric", minute: "2-digit" });

                return (
                  <Link key={event.id} href={`/events/${event.id}`} className={styles.eventCard}>
                    <div className={styles.eventImage}>
                      {event.imageUrls && event.imageUrls.length > 0 ? (
                        // eslint-disable-next-line @next/next/no-img-element
                        <img src={event.imageUrls[0]} alt={event.title} className={styles.eventImg} />
                      ) : (
                        <div className={styles.eventImgPlaceholder}>
                          <Ticket size={36} style={{ opacity: 0.2 }} />
                        </div>
                      )}
                      <div className={styles.eventImageOverlay} />
                      <span className={styles.eventDateBadge}>
                        <Calendar size={12} /> {formattedDate}
                      </span>
                      {event.highDemand && (
                        <span className={styles.eventHotBadge}>🔥 Hot</span>
                      )}
                    </div>

                    <div className={styles.eventBody}>
                      <h3 className={styles.eventTitle}>{event.title}</h3>
                      <div className={styles.eventMeta}>
                        <span className={styles.eventMetaItem}><MapPin size={13} /> {event.city || "Various Locations"}</span>
                        <span className={styles.eventMetaItem}><Clock size={13} /> {formattedTime}</span>
                      </div>
                      <div className={styles.eventFooter}>
                        <span className={statusBadgeClass(event.status)}>{statusLabel(event.status)}</span>
                        <span className={styles.eventCta}>
                          View Details <ArrowRight size={14} />
                        </span>
                      </div>
                    </div>
                  </Link>
                );
              })}
            </div>
          </>
        ) : (
          <div className={styles.emptyState}>
            <Search size={48} style={{ opacity: 0.2, marginBottom: "1rem" }} />
            <h3>No events found</h3>
            <p>{search ? "Try a different search term." : "Check back soon for new events!"}</p>
            {search && (
              <button onClick={() => setSearch("")} className="btn btn-secondary" style={{ marginTop: "1rem" }} type="button">
                Clear search
              </button>
            )}
          </div>
        )}
      </div>
    </main>
  );
}
