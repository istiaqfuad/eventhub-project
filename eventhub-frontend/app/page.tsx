"use client";

import Link from "next/link";
import { ArrowRight, Calendar, MapPin, Ticket, Search, Zap, Shield, Clock } from "lucide-react";
import styles from "./page.module.css";
import Navbar from "./components/Navbar";
import { useEvents } from "./hooks/useEvents";

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
      <div className="skeleton" style={{ height: 200 }} />
      <div style={{ padding: "1.25rem", display: "flex", flexDirection: "column", gap: "0.75rem" }}>
        <div className="skeleton" style={{ height: 20, width: "70%" }} />
        <div className="skeleton" style={{ height: 14, width: "45%" }} />
        <div className="skeleton" style={{ height: 14, width: "55%" }} />
        <div style={{ display: "flex", justifyContent: "space-between", marginTop: "0.5rem" }}>
          <div className="skeleton" style={{ height: 24, width: 60, borderRadius: 99 }} />
          <div className="skeleton" style={{ height: 32, width: 90 }} />
        </div>
      </div>
    </div>
  );
}

export default function Home() {
  const { data: events, isLoading, error } = useEvents();

  return (
    <main>
      <Navbar />

      {/* ── Hero ── */}
      <section className={styles.hero}>
        <div className={styles.heroBg} />
        <div className={styles.heroGlow1} />
        <div className={styles.heroGlow2} />

        <div className={styles.heroContent}>
          <span className={styles.heroPill}>
            <Zap size={13} /> Live Events Platform
          </span>
          <h1 className={styles.heroTitle}>
            Find Your Next<br />
            <span className="text-gradient">Unforgettable Experience</span>
          </h1>
          <p className={styles.heroSubtitle}>
            Discover concerts, sports, theatre, and more. Book tickets instantly with secure, seamless checkout.
          </p>

          <div className={styles.heroSearch}>
            <Search size={18} className={styles.heroSearchIcon} />
            <Link href="/events" className={styles.heroSearchInput}>
              Search events, artists, venues...
            </Link>
            <Link href="/events" className={`btn btn-primary ${styles.heroSearchBtn}`}>
              Explore
            </Link>
          </div>

          <div className={styles.heroTrustRow}>
            <span className={styles.heroTrustItem}><Shield size={14} /> Secure Checkout</span>
            <span className={styles.heroTrustItem}><Clock size={14} /> Instant Confirmation</span>
            <span className={styles.heroTrustItem}><Ticket size={14} /> Digital Tickets</span>
          </div>
        </div>
      </section>

      {/* ── Trending Events ── */}
      <section id="events" className={`${styles.featured} container`}>
        <div className={styles.sectionHeader}>
          <div>
            <h2 className={styles.sectionTitle}>Trending Now</h2>
            <p style={{ color: "var(--text-secondary)", marginBottom: 0, fontSize: "0.95rem" }}>
              The most popular upcoming events
            </p>
          </div>
          <Link href="/events" className={`btn btn-secondary`} style={{ fontSize: "0.875rem", gap: "0.4rem" }}>
            View All <ArrowRight size={16} />
          </Link>
        </div>

        {isLoading ? (
          <div className={styles.eventsGrid}>
            {[...Array(6)].map((_, i) => <EventSkeleton key={i} />)}
          </div>
        ) : error ? (
          <div className="errorBox" style={{ textAlign: "center" }}>
            Failed to load events. Please try again later.
          </div>
        ) : events && events.length > 0 ? (
          <div className={styles.eventsGrid}>
            {events.slice(0, 6).map((event) => {
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
                      <Calendar size={12} />
                      {formattedDate}
                    </span>
                    {event.highDemand && (
                      <span className={styles.eventHotBadge}>🔥 Hot</span>
                    )}
                  </div>

                  <div className={styles.eventBody}>
                    <h3 className={styles.eventTitle}>{event.title}</h3>
                    <div className={styles.eventMeta}>
                      <span className={styles.eventMetaItem}>
                        <MapPin size={13} /> {event.city || "Various Locations"}
                      </span>
                      <span className={styles.eventMetaItem}>
                        <Clock size={13} /> {formattedTime}
                      </span>
                    </div>
                    <div className={styles.eventFooter}>
                      <span className={statusBadgeClass(event.status)}>
                        {statusLabel(event.status)}
                      </span>
                      <span className={styles.eventCta}>
                        Get Tickets <ArrowRight size={14} />
                      </span>
                    </div>
                  </div>
                </Link>
              );
            })}
          </div>
        ) : (
          <div className={styles.emptyState}>
            <Ticket size={48} style={{ opacity: 0.2, marginBottom: "1rem" }} />
            <h3>No upcoming events</h3>
            <p>New events are added regularly. Check back soon!</p>
          </div>
        )}
      </section>
    </main>
  );
}
