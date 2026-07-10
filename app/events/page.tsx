"use client";

import Link from "next/link";
import { ArrowLeft, Calendar, MapPin, Ticket } from "lucide-react";
import Navbar from "../components/Navbar";
import { useEvents } from "../hooks/useEvents";
import styles from "../page.module.css";

export default function EventsCatalog() {
  const { data: events, isLoading, error } = useEvents();

  return (
    <main>
      <Navbar />
      
      <div className="container" style={{ paddingTop: '100px', minHeight: '100vh' }}>
        <div style={{ marginBottom: 'var(--space-xl)' }}>
          <Link href="/" style={{ color: 'var(--text-secondary)', display: 'inline-flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem' }}>
            <ArrowLeft size={16} /> Back to Home
          </Link>
          <h1 style={{ fontSize: '2.5rem', marginBottom: '0.5rem' }}>Upcoming Events</h1>
          <p style={{ color: 'var(--text-secondary)' }}>Browse the full catalog of concerts, sports, and live shows.</p>
        </div>

        {/* Filters could go here in the future */}
        
        {isLoading ? (
          <div style={{ textAlign: 'center', padding: 'var(--space-2xl) 0', color: 'var(--text-secondary)' }}>
            Loading events...
          </div>
        ) : error ? (
          <div className="errorBox" style={{ textAlign: 'center' }}>
            Failed to load events. Please try again later.
          </div>
        ) : events && events.length > 0 ? (
          <div className={styles.eventsGrid} style={{ paddingBottom: 'var(--space-2xl)' }}>
            {events.map((event) => {
              const eventDate = new Date(event.startsAt);
              const formattedDate = eventDate.toLocaleDateString("en-US", { weekday: 'short', month: "short", day: "numeric", hour: "numeric", minute: "numeric" });
              
              return (
                <div key={event.id} className={styles.eventCard}>
                  <div className={styles.eventImagePlaceholder}>
                    {event.imageUrls && event.imageUrls.length > 0 ? (
                      <img 
                        src={event.imageUrls[0]} 
                        alt={event.title} 
                        style={{ width: '100%', height: '100%', objectFit: 'cover', position: 'absolute' }} 
                      />
                    ) : (
                      <span style={{ opacity: 0.1, transform: 'scale(5)' }}><Ticket /></span>
                    )}
                    <span className={styles.eventDate}>
                      <Calendar size={14} style={{ display: 'inline', marginRight: '4px', verticalAlign: 'text-bottom' }} />
                      {formattedDate.split(',')[1]}
                    </span>
                  </div>
                  <div className={styles.eventContent}>
                    <h3 className={styles.eventTitle}>{event.title}</h3>
                    <div className={styles.eventVenue} style={{ marginBottom: '0.5rem' }}>
                      <MapPin size={14} /> {event.city || "Various Locations"}
                    </div>
                    <div style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', marginBottom: 'var(--space-md)' }}>
                      {formattedDate}
                    </div>
                    <div className={styles.eventFooter}>
                      <div className={styles.eventPrice}>
                        {event.highDemand && <span style={{ color: 'var(--warning)', fontSize: '0.8rem', marginRight: '0.5rem' }}>🔥 High Demand</span>}
                      </div>
                      <Link href={`/events/${event.publicId || event.id}`} className="btn btn-secondary" style={{ padding: '0.25rem 0.75rem', fontSize: '0.875rem' }}>
                        Details
                      </Link>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        ) : (
          <div style={{ textAlign: 'center', padding: 'var(--space-2xl) 0', color: 'var(--text-secondary)' }}>
            No upcoming events found. Check back soon!
          </div>
        )}
      </div>
    </main>
  );
}
