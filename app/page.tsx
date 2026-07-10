"use client";

import Link from "next/link";
import { ArrowRight, Calendar, MapPin, Ticket } from "lucide-react";
import styles from "./page.module.css";
import Navbar from "./components/Navbar";
import { useEvents } from "./hooks/useEvents";

function statusLabel(status: string) {
  switch (status) {
    case "ON_SALE":
      return "On Sale";
    case "DRAFT":
      return "Draft";
    case "CLOSED":
      return "Closed";
    case "CANCELLED":
      return "Cancelled";
    default:
      return status;
  }
}

export default function Home() {
  const { data: events, isLoading, error } = useEvents();

  return (
    <main>
      <Navbar />

      <section className={styles.hero}>
        <div className={styles.heroBackground}></div>
        <div className={styles.heroGlow}></div>

        <div className={styles.heroContent}>
          <h1 className={styles.title}>
            Experience Live Events <br />
            <span className="text-gradient">Like Never Before</span>
          </h1>
          <p className={styles.subtitle}>
            Secure your spot at the most anticipated concerts, sports matches, and exclusive
            events. Fast, reliable, and premium ticketing.
          </p>
          <div className={styles.ctaGroup}>
            <Link href="#events" className={`btn btn-primary ${styles.btnLarge}`}>
              <Ticket size={20} />
              Browse Events
            </Link>
            <Link href="/about" className={`btn btn-secondary ${styles.btnLarge}`}>
              Learn More
            </Link>
          </div>
        </div>
      </section>

      <section id="events" className={`${styles.featured} container`}>
        <div className={styles.sectionHeader}>
          <h2>Trending Now</h2>
          <Link
            href="/events"
            className="text-gradient"
            style={{ display: "flex", alignItems: "center", gap: "0.5rem", fontWeight: 600 }}
          >
            View All <ArrowRight size={18} />
          </Link>
        </div>

        {isLoading ? (
          <div
            style={{
              textAlign: "center",
              padding: "var(--space-2xl) 0",
              color: "var(--text-secondary)",
            }}
          >
            Loading events...
          </div>
        ) : error ? (
          <div className="errorBox" style={{ textAlign: "center" }}>
            Failed to load events. Please try again later.
          </div>
        ) : events && events.length > 0 ? (
          <div className={styles.eventsGrid}>
            {events.slice(0, 6).map((event) => {
              const eventDate = new Date(event.startsAt);
              const formattedDate = eventDate.toLocaleDateString("en-US", {
                month: "short",
                day: "numeric",
              });

              return (
                <div key={event.id} className={styles.eventCard}>
                  <div className={styles.eventImagePlaceholder}>
                    {event.imageUrls && event.imageUrls.length > 0 ? (
                      // eslint-disable-next-line @next/next/no-img-element
                      <img
                        src={event.imageUrls[0]}
                        alt={event.title}
                        style={{
                          width: "100%",
                          height: "100%",
                          objectFit: "cover",
                          position: "absolute",
                        }}
                      />
                    ) : (
                      <span style={{ opacity: 0.1, transform: "scale(5)" }}>
                        <Ticket />
                      </span>
                    )}
                    <span className={styles.eventDate}>
                      <Calendar
                        size={14}
                        style={{ display: "inline", marginRight: "4px", verticalAlign: "text-bottom" }}
                      />
                      {formattedDate}
                    </span>
                  </div>
                  <div className={styles.eventContent}>
                    <h3 className={styles.eventTitle}>{event.title}</h3>
                    <div className={styles.eventVenue}>
                      <MapPin size={14} /> {event.city || "Various Locations"}
                    </div>
                    <div className={styles.eventFooter}>
                      <div className={styles.eventPrice}>
                        {event.highDemand && (
                          <span
                            style={{
                              color: "var(--warning)",
                              fontSize: "0.8rem",
                              marginRight: "0.5rem",
                            }}
                          >
                            🔥 High Demand
                          </span>
                        )}
                        <span style={{ fontSize: "0.8rem", color: "var(--text-secondary)" }}>
                          {statusLabel(event.status)}
                        </span>
                      </div>
                      <Link
                        href={`/events/${event.id}`}
                        className="btn btn-secondary"
                        style={{ padding: "0.25rem 0.75rem", fontSize: "0.875rem" }}
                      >
                        Get Tickets
                      </Link>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        ) : (
          <div
            style={{
              textAlign: "center",
              padding: "var(--space-2xl) 0",
              color: "var(--text-secondary)",
            }}
          >
            No upcoming events found. Check back soon!
          </div>
        )}
      </section>
    </main>
  );
}
