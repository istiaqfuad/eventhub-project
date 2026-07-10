"use client";

import { use, useState } from "react";
import Link from "next/link";
import { ArrowLeft, Calendar, MapPin, Share2, Star, Ticket } from "lucide-react";
import Navbar from "../../components/Navbar";
import { useEvent } from "../../hooks/useEvents";
import { useTicketTypes } from "../../hooks/useBooking";
import { useCreateReview, useEventReviews } from "../../hooks/useReviews";
import styles from "./event-details.module.css";
import { useAuthStore } from "../../providers/auth-store-provider";
import { useRouter } from "next/navigation";
import { getProblemDetail } from "../../lib/api-client";

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

export default function EventDetailsPage({ params }: { params: Promise<{ id: string }> }) {
  const resolvedParams = use(params);
  const { data: event, isLoading, error } = useEvent(resolvedParams.id);
  const { data: ticketTypes } = useTicketTypes(resolvedParams.id);
  const { data: reviews, isLoading: reviewsLoading } = useEventReviews(resolvedParams.id);
  const createReview = useCreateReview();
  const { isAuthenticated } = useAuthStore((s) => s);
  const router = useRouter();

  const [rating, setRating] = useState(5);
  const [body, setBody] = useState("");
  const [reviewError, setReviewError] = useState<string | null>(null);
  const [reviewOk, setReviewOk] = useState(false);

  if (isLoading) {
    return (
      <main>
        <Navbar />
        <div
          style={{
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            minHeight: "100vh",
            color: "var(--text-secondary)",
          }}
        >
          Loading event details...
        </div>
      </main>
    );
  }

  if (error || !event) {
    return (
      <main>
        <Navbar />
        <div className="container" style={{ paddingTop: "100px", textAlign: "center" }}>
          <div className="errorBox">
            <h2>Event not found</h2>
            <p>The event you are looking for does not exist or has been removed.</p>
            <Link href="/events" className="btn btn-primary" style={{ marginTop: "1rem" }}>
              Back to Events
            </Link>
          </div>
        </div>
      </main>
    );
  }

  const startDate = new Date(event.startsAt);
  const endDate = new Date(event.endsAt);
  const dateFormatted = startDate.toLocaleDateString("en-US", {
    weekday: "long",
    month: "long",
    day: "numeric",
  });
  const timeFormatted = `${startDate.toLocaleTimeString("en-US", {
    hour: "numeric",
    minute: "2-digit",
  })} - ${endDate.toLocaleTimeString("en-US", { hour: "numeric", minute: "2-digit" })}`;

  const minPrice =
    ticketTypes && ticketTypes.length > 0
      ? Math.min(...ticketTypes.map((t) => Number(t.price)))
      : null;

  const handleBookClick = () => {
    if (!isAuthenticated) {
      router.push(`/login?redirect=/events/${event.id}/book`);
    } else {
      router.push(`/events/${event.id}/book`);
    }
  };

  const handleReviewSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setReviewError(null);
    setReviewOk(false);
    if (!isAuthenticated) {
      router.push(`/login?redirect=/events/${event.id}`);
      return;
    }
    try {
      await createReview.mutateAsync({ eventId: event.id, rating, body: body || "" });
      setBody("");
      setReviewOk(true);
    } catch (err) {
      setReviewError(getProblemDetail(err));
    }
  };

  return (
    <main>
      <Navbar />

      <section className={styles.hero}>
        {event.imageUrls && event.imageUrls.length > 0 ? (
          // eslint-disable-next-line @next/next/no-img-element
          <img src={event.imageUrls[0]} alt={event.title} className={styles.heroImage} />
        ) : (
          <div
            className={styles.heroImage}
            style={{ background: "linear-gradient(135deg, var(--bg-tertiary), var(--bg-secondary))" }}
          />
        )}
        <div className={styles.heroOverlay} />

        <div className={`container ${styles.heroContent}`}>
          <Link
            href="/events"
            style={{
              color: "rgba(255,255,255,0.7)",
              display: "inline-flex",
              alignItems: "center",
              gap: "0.5rem",
              marginBottom: "1rem",
            }}
          >
            <ArrowLeft size={16} /> All Events
          </Link>
          <div className={styles.metaTags}>
            {event.highDemand && (
              <span
                className={styles.metaBadge}
                style={{ color: "var(--warning)", borderColor: "rgba(255, 234, 0, 0.3)" }}
              >
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

      <section className={`container ${styles.layout}`}>
        <div className={styles.mainCol}>
          <div>
            <h2>About this Event</h2>
            <p style={{ marginTop: "1rem", whiteSpace: "pre-wrap", lineHeight: 1.8 }}>
              {event.description || "No description provided."}
            </p>
          </div>

          <div>
            <h3>Date and Time</h3>
            <div
              style={{
                display: "flex",
                alignItems: "center",
                gap: "1rem",
                marginTop: "1rem",
                color: "var(--text-secondary)",
              }}
            >
              <div
                style={{
                  background: "var(--glass-bg)",
                  padding: "1rem",
                  borderRadius: "var(--radius-md)",
                  border: "1px solid var(--glass-border)",
                }}
              >
                <Calendar size={24} color="var(--accent-primary)" />
              </div>
              <div>
                <div style={{ fontWeight: 600, color: "var(--text-primary)", fontSize: "1.1rem" }}>
                  {dateFormatted}
                </div>
                <div>{timeFormatted}</div>
              </div>
            </div>
          </div>

          <div style={{ marginTop: "2rem" }}>
            <h3 style={{ marginBottom: "1rem" }}>Reviews</h3>
            {reviewsLoading ? (
              <p style={{ color: "var(--text-secondary)" }}>Loading reviews...</p>
            ) : reviews && reviews.length > 0 ? (
              <div style={{ display: "flex", flexDirection: "column", gap: "1rem", marginBottom: "1.5rem" }}>
                {reviews.map((r) => (
                  <div
                    key={r.id}
                    style={{
                      padding: "1rem",
                      background: "var(--bg-secondary)",
                      border: "1px solid var(--glass-border)",
                      borderRadius: "var(--radius-md)",
                    }}
                  >
                    <div style={{ display: "flex", alignItems: "center", gap: "0.35rem", marginBottom: "0.35rem" }}>
                      {Array.from({ length: r.rating }).map((_, i) => (
                        <Star key={i} size={14} fill="var(--warning)" color="var(--warning)" />
                      ))}
                    </div>
                    <p style={{ color: "var(--text-secondary)", fontSize: "0.95rem" }}>
                      {r.body || "No comment"}
                    </p>
                  </div>
                ))}
              </div>
            ) : (
              <p style={{ color: "var(--text-secondary)", marginBottom: "1rem" }}>No reviews yet.</p>
            )}

            <form
              onSubmit={handleReviewSubmit}
              style={{
                padding: "1rem",
                background: "var(--bg-secondary)",
                border: "1px solid var(--glass-border)",
                borderRadius: "var(--radius-md)",
              }}
            >
              <h4 style={{ marginBottom: "0.75rem" }}>Leave a review</h4>
              <label style={{ display: "block", marginBottom: "0.5rem", fontSize: "0.9rem" }}>
                Rating
                <select
                  value={rating}
                  onChange={(e) => setRating(Number(e.target.value))}
                  style={{
                    display: "block",
                    marginTop: "0.25rem",
                    width: "100%",
                    padding: "0.5rem",
                    background: "var(--bg-tertiary)",
                    color: "var(--text-primary)",
                    border: "1px solid var(--glass-border)",
                    borderRadius: "var(--radius-sm)",
                  }}
                >
                  {[5, 4, 3, 2, 1].map((n) => (
                    <option key={n} value={n}>
                      {n} star{n > 1 ? "s" : ""}
                    </option>
                  ))}
                </select>
              </label>
              <label style={{ display: "block", marginBottom: "0.75rem", fontSize: "0.9rem" }}>
                Comment
                <textarea
                  value={body}
                  onChange={(e) => setBody(e.target.value)}
                  rows={3}
                  style={{
                    display: "block",
                    marginTop: "0.25rem",
                    width: "100%",
                    padding: "0.5rem",
                    background: "var(--bg-tertiary)",
                    color: "var(--text-primary)",
                    border: "1px solid var(--glass-border)",
                    borderRadius: "var(--radius-sm)",
                    resize: "vertical",
                  }}
                  placeholder="How was the event?"
                />
              </label>
              {reviewError && (
                <div className="errorBox" style={{ marginBottom: "0.75rem", fontSize: "0.85rem" }}>
                  {reviewError}
                </div>
              )}
              {reviewOk && (
                <p style={{ color: "var(--success)", marginBottom: "0.75rem", fontSize: "0.9rem" }}>
                  Review submitted.
                </p>
              )}
              <button
                type="submit"
                className="btn btn-secondary"
                disabled={createReview.isPending}
              >
                {createReview.isPending ? "Submitting..." : isAuthenticated ? "Submit review" : "Log in to review"}
              </button>
            </form>
          </div>
        </div>

        <div className={styles.sideCol}>
          <div className={styles.bookingCard}>
            <div style={{ marginBottom: "1.5rem" }}>
              <div style={{ color: "var(--text-secondary)", fontSize: "0.9rem" }}>Starting from</div>
              <div className={styles.priceHeader}>
                {minPrice != null ? (
                  <span className="text-gradient">${minPrice.toFixed(2)}</span>
                ) : (
                  <span className="text-gradient">Select Seats</span>
                )}
              </div>
            </div>

            <button onClick={handleBookClick} className={`btn btn-primary ${styles.btnBook}`} type="button">
              <Ticket size={20} />
              Book Tickets
            </button>
            <div
              style={{
                textAlign: "center",
                fontSize: "0.8rem",
                color: "var(--text-secondary)",
                marginBottom: "1.5rem",
              }}
            >
              Secure checkout via Stripe
            </div>

            <div style={{ borderTop: "1px solid var(--glass-border)", paddingTop: "1rem" }}>
              <div className={styles.infoRow}>
                <span style={{ color: "var(--text-secondary)" }}>Status</span>
                <span style={{ fontWeight: 500 }}>
                  {event.status === "ON_SALE" ? (
                    <span style={{ color: "var(--success)" }}>On Sale</span>
                  ) : (
                    statusLabel(event.status)
                  )}
                </span>
              </div>
              <div className={styles.infoRow}>
                <span style={{ color: "var(--text-secondary)" }}>Event ID</span>
                <span style={{ fontWeight: 500 }}>#{event.id}</span>
              </div>
            </div>

            <button
              type="button"
              className="btn btn-secondary"
              style={{ width: "100%", marginTop: "1rem", display: "flex", gap: "0.5rem", justifyContent: "center" }}
              onClick={() => {
                if (navigator.share) {
                  navigator.share({ title: event.title, url: window.location.href }).catch(() => {});
                } else if (navigator.clipboard) {
                  navigator.clipboard.writeText(window.location.href);
                }
              }}
            >
              <Share2 size={16} /> Share Event
            </button>
          </div>
        </div>
      </section>
    </main>
  );
}
