import Link from "next/link";
import { ArrowRight, Calendar, MapPin, Ticket } from "lucide-react";
import styles from "./page.module.css";
import Navbar from "./components/Navbar";

export default function Home() {
  const dummyEvents = [
    { id: 1, title: "Neon Nights Music Festival", venue: "The Grand Arena, NY", date: "Aug 15", price: "$85" },
    { id: 2, title: "Symphony Under the Stars", venue: "Central Park, NY", date: "Sep 02", price: "$45" },
    { id: 3, title: "Comedy Super Jam", venue: "Laugh Factory, CA", date: "Jul 22", price: "$30" },
  ];

  return (
    <main>
      <Navbar />
      
      {/* Hero Section */}
      <section className={styles.hero}>
        <div className={styles.heroBackground}></div>
        <div className={styles.heroGlow}></div>
        
        <div className={styles.heroContent}>
          <h1 className={styles.title}>
            Experience Live Events <br />
            <span className="text-gradient">Like Never Before</span>
          </h1>
          <p className={styles.subtitle}>
            Secure your spot at the most anticipated concerts, sports matches, and exclusive events. Fast, reliable, and premium ticketing.
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

      {/* Featured Events */}
      <section id="events" className={`${styles.featured} container`}>
        <div className={styles.sectionHeader}>
          <h2>Trending Now</h2>
          <Link href="/events" className="text-gradient" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontWeight: 600 }}>
            View All <ArrowRight size={18} />
          </Link>
        </div>

        <div className={styles.eventsGrid}>
          {dummyEvents.map((event) => (
            <div key={event.id} className={styles.eventCard}>
              <div className={styles.eventImagePlaceholder}>
                <span className={styles.eventDate}>
                  <Calendar size={14} style={{ display: 'inline', marginRight: '4px', verticalAlign: 'text-bottom' }} />
                  {event.date}
                </span>
                <span style={{ opacity: 0.1, transform: 'scale(5)' }}><Ticket /></span>
              </div>
              <div className={styles.eventContent}>
                <h3 className={styles.eventTitle}>{event.title}</h3>
                <div className={styles.eventVenue}>
                  <MapPin size={14} /> {event.venue}
                </div>
                <div className={styles.eventFooter}>
                  <div className={styles.eventPrice}>From {event.price}</div>
                  <Link href={`/events/${event.id}`} className="btn btn-secondary" style={{ padding: '0.25rem 0.75rem', fontSize: '0.875rem' }}>
                    Get Tickets
                  </Link>
                </div>
              </div>
            </div>
          ))}
        </div>
      </section>
    </main>
  );
}
