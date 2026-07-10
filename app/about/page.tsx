"use client";

import Link from "next/link";
import { ArrowLeft, Ticket } from "lucide-react";
import Navbar from "../components/Navbar";

export default function AboutPage() {
  return (
    <main>
      <Navbar />
      <div className="container" style={{ paddingTop: "100px", minHeight: "100vh", paddingBottom: "3rem", maxWidth: 720 }}>
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
        <div style={{ display: "flex", alignItems: "center", gap: "0.75rem", marginBottom: "1rem" }}>
          <Ticket size={32} color="var(--accent-primary)" />
          <h1 style={{ fontSize: "2.5rem" }}>About EventHub</h1>
        </div>
        <p style={{ color: "var(--text-secondary)", lineHeight: 1.8, marginBottom: "1rem" }}>
          EventHub is a high-concurrency event ticketing platform. Organizers list events; fans
          browse, hold seats, and pay through Stripe Checkout. Holds expire automatically if payment
          is not completed, keeping inventory fair under demand.
        </p>
        <p style={{ color: "var(--text-secondary)", lineHeight: 1.8, marginBottom: "2rem" }}>
          This frontend talks to the EventHub Spring Boot API for auth (JWT + refresh cookies),
          events, venues, bookings, payments, and reviews.
        </p>
        <Link href="/events" className="btn btn-primary">
          Browse Events
        </Link>
      </div>
    </main>
  );
}
