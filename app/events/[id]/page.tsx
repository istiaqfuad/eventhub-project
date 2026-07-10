"use client";

import { use, useEffect } from "react";
import Link from "next/link";
import { ArrowLeft, Calendar, MapPin, Share2, Tag, Ticket } from "lucide-react";
import Navbar from "../../components/Navbar";
import { useEvent } from "../../hooks/useEvents";
import styles from "./event-details.module.css";
import { useAuthStore } from "../../providers/auth-store-provider";
import { useRouter } from "next/navigation";

export default function EventDetailsPage({ params }: { params: Promise<{ id: string }> }) {
  // Use React `use` hook to unwrap params
  const resolvedParams = use(params);
  const { data: event, isLoading, error } = useEvent(resolvedParams.id);
  const { isAuthenticated } = useAuthStore((s) => s);
  const router = useRouter();

  if (isLoading) {
    return (
      <main>
        <Navbar />
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', color: 'var(--text-secondary)' }}>
          Loading event details...
        </div>
      </main>
    );
  }

  if (error || !event) {
    return (
      <main>
        <Navbar />
        <div className="container" style={{ paddingTop: '100px', textAlign: 'center' }}>
          <div className="errorBox">
            <h2>Event not found</h2>
            <p>The event you are looking for does not exist or has been removed.</p>
            <Link href="/events" className="btn btn-primary" style={{ marginTop: '1rem' }}>
              Back to Events
            </Link>
          </div>
        </div>
      </main>
    );
  }

  const startDate = new Date(event.startsAt);
  const endDate = new Date(event.endsAt);
  const dateFormatted = startDate.toLocaleDateString("en-US", { weekday: 'long', month: "long", day: "numeric" });
  const timeFormatted = `${startDate.toLocaleTimeString("en-US", { hour: "numeric", minute: "2-digit" })} - ${endDate.toLocaleTimeString("en-US", { hour: "numeric", minute: "2-digit" })}`;

  const handleBookClick = () => {
    if (!isAuthenticated) {
      router.push(`/login?redirect=/events/${event.publicId || event.id}`);
    } else {
      // Navigate to booking/seat selection flow
      router.push(`/events/${event.publicId || event.id}/book`);
    }
  };

  return (
    <main>
      <Navbar />
      
      {/* Hero Header */}
      <section className={styles.hero}>
        {event.imageUrls && event.imageUrls.length > 0 ? (
          <img src={event.imageUrls[0]} alt={event.title} className={styles.heroImage} />
        ) : (
          <div className={styles.heroImage} style={{ background: 'linear-gradient(135deg, var(--bg-tertiary), var(--bg-secondary))' }} />
        )}
        <div className={styles.heroOverlay} />
        
        <div className={`container ${styles.heroContent}`}>
          <Link href="/events" style={{ color: 'rgba(255,255,255,0.7)', display: 'inline-flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem' }}>
            <ArrowLeft size={16} /> All Events
          </Link>
          <div className={styles.metaTags}>
            {event.highDemand && (
              <span className={styles.metaBadge} style={{ color: 'var(--warning)', borderColor: 'rgba(255, 234, 0, 0.3)' }}>
                🔥 High Demand
              </span>
            )}
            <span className={styles.metaBadge}>
              <MapPin size={14} /> {event.city || "Venue TBD"}
            </span>
          </div>
          <h1 className={styles.title}>{event.title}</h1>
        </div>
      </section>

      {/* Two Column Layout */}
      <section className={`container ${styles.layout}`}>
        <div className={styles.mainCol}>
          <div>
            <h2>About this Event</h2>
            <p style={{ marginTop: '1rem', whiteSpace: 'pre-wrap', lineHeight: 1.8 }}>
              {event.description || "No description provided."}
            </p>
          </div>

          <div>
            <h3>Date and Time</h3>
            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginTop: '1rem', color: 'var(--text-secondary)' }}>
              <div style={{ background: 'var(--glass-bg)', padding: '1rem', borderRadius: 'var(--radius-md)', border: '1px solid var(--glass-border)' }}>
                <Calendar size={24} color="var(--accent-primary)" />
              </div>
              <div>
                <div style={{ fontWeight: 600, color: 'var(--text-primary)', fontSize: '1.1rem' }}>{dateFormatted}</div>
                <div>{timeFormatted}</div>
              </div>
            </div>
          </div>
        </div>

        <div className={styles.sideCol}>
          <div className={styles.bookingCard}>
            <div style={{ marginBottom: '1.5rem' }}>
              <div style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>Starting from</div>
              <div className={styles.priceHeader}>
                {/* Real ticket prices would come from a different endpoint. Using dummy price for aesthetic */}
                <span className="text-gradient">Select Seats</span>
              </div>
            </div>

            <button 
              onClick={handleBookClick}
              className={`btn btn-primary ${styles.btnBook}`}
            >
              <Ticket size={20} />
              Book Tickets
            </button>
            <div style={{ textAlign: 'center', fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>
              Secure checkout via Stripe
            </div>

            <div style={{ borderTop: '1px solid var(--glass-border)', paddingTop: '1rem' }}>
              <div className={styles.infoRow}>
                <span style={{ color: 'var(--text-secondary)' }}>Status</span>
                <span style={{ fontWeight: 500 }}>
                  {event.status === 'PUBLISHED' ? <span style={{ color: 'var(--success)' }}>On Sale</span> : event.status}
                </span>
              </div>
              <div className={styles.infoRow}>
                <span style={{ color: 'var(--text-secondary)' }}>Event ID</span>
                <span style={{ fontWeight: 500 }}>#{event.id}</span>
              </div>
            </div>

            <button className="btn btn-secondary" style={{ width: '100%', marginTop: '1rem', display: 'flex', gap: '0.5rem', justifyContent: 'center' }}>
              <Share2 size={16} /> Share Event
            </button>
          </div>
        </div>
      </section>
    </main>
  );
}
