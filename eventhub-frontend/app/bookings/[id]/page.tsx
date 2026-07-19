"use client";

import { use } from "react";
import Link from "next/link";
import { ArrowLeft, Ticket } from "lucide-react";
import Navbar from "../../components/Navbar/Navbar";
import { useBooking } from "../../hooks/useBooking";
import { useAuthStore } from "../../providers/auth-store-provider";
import { useRouter } from "next/navigation";
import { useEffect } from "react";

export default function BookingDetailsPage({ params }: { params: Promise<{ id: string }> }) {
  const resolvedParams = use(params);
  const { data: booking, isLoading, error } = useBooking(resolvedParams.id);
  const { isAuthenticated, hydrated } = useAuthStore((s) => s);
  const router = useRouter();

  useEffect(() => {
    if (hydrated && !isAuthenticated) {
      router.push("/login?redirect=/bookings");
    }
  }, [hydrated, isAuthenticated, router]);

  if (!hydrated || isLoading) {
    return (
      <main className="min-h-screen bg-[#0b0e14] text-white">
        <Navbar />
        <div className="flex items-center justify-center min-h-[calc(100vh-80px)] text-gray-400">
          Loading ticket details...
        </div>
      </main>
    );
  }

  if (error || !booking) {
    return (
      <main className="min-h-screen bg-[#0b0e14] text-white">
        <Navbar />
        <div className="container mx-auto px-6 pt-[100px] text-center">
          <div className="bg-[#ff3366]/10 border border-[#ff3366]/30 text-[#ff8fab] p-4 rounded-lg inline-block">
            Failed to load booking. It may not exist or you don't have access.
          </div>
        </div>
      </main>
    );
  }

  const isConfirmed = booking.status === "CONFIRMED";
  const isPending = booking.status === "PENDING";
  const isCancelled = booking.status === "CANCELLED";
  const isExpired = booking.status === "EXPIRED";

  const statusColorClass = isConfirmed 
    ? "text-[#00e676]" 
    : isPending 
      ? "text-[#ffea00]" 
      : isCancelled || isExpired 
        ? "text-[#ff3366]" 
        : "text-gray-400";

  return (
    <main style={{ minHeight: "100vh", background: "var(--bg-primary)", color: "var(--text-primary)" }}>
      <Navbar />
      
      <div className="container" style={{ paddingTop: "140px", paddingBottom: "4rem", maxWidth: 1000, margin: "0 auto" }}>
        
        <Link
          href="/bookings"
          style={{
            display: "inline-flex",
            alignItems: "center",
            gap: "0.5rem",
            color: "var(--text-muted)",
            fontSize: "0.9rem",
            marginBottom: "2rem",
            transition: "color 0.2s ease",
            textDecoration: "none"
          }}
          className="hover:text-[#00f0ff]"
        >
          <ArrowLeft size={16} /> Back to My Tickets
        </Link>

        {(!hydrated || isLoading) ? (
          // Loading Skeleton
          <div className="animate-pulse">
            <div className="h-10 bg-white/5 rounded w-1/3 mb-8"></div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
              <div className="bg-white/5 rounded-2xl h-96 border border-white/5"></div>
              <div className="bg-white/5 rounded-2xl h-96 border border-white/5"></div>
            </div>
          </div>
        ) : error || !booking ? (
          // Error State
          <div style={{ padding: "4rem", textAlign: "center", background: "var(--bg-secondary)", borderRadius: "var(--radius-xl)", border: "1px dashed var(--glass-border)" }}>
            <div style={{ color: "var(--text-muted)", marginBottom: "1rem" }}>
              <Ticket size={48} style={{ opacity: 0.2, margin: "0 auto" }} />
            </div>
            <h3 style={{ fontSize: "1.5rem", fontWeight: 700, marginBottom: "0.5rem" }}>Booking Not Found</h3>
            <p style={{ color: "var(--text-secondary)" }}>Failed to load booking. It may not exist or you don't have access.</p>
          </div>
        ) : (
          // Loaded State
          <>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-end", marginBottom: "2rem", flexWrap: "wrap", gap: "1rem" }}>
              <h1 style={{ fontSize: "3rem", fontWeight: 800, letterSpacing: "-0.02em", lineHeight: 1.1 }}>
                Order <span style={{ color: "var(--accent-primary)" }}>#{booking.id}</span>
              </h1>
              
              {/* Status Badge */}
              <div style={{
                padding: "0.5rem 1.25rem",
                borderRadius: "var(--radius-full)",
                fontSize: "0.85rem",
                fontWeight: 700,
                letterSpacing: "0.05em",
                textTransform: "uppercase",
                display: "inline-flex",
                alignItems: "center",
                gap: "0.5rem",
                background: booking.status === "CONFIRMED" ? "rgba(0, 230, 118, 0.1)" 
                           : booking.status === "PENDING" ? "rgba(255, 234, 0, 0.1)"
                           : "rgba(255, 51, 102, 0.1)",
                color: booking.status === "CONFIRMED" ? "#00e676" 
                      : booking.status === "PENDING" ? "#ffea00"
                      : "#ff3366",
                border: `1px solid ${booking.status === "CONFIRMED" ? "rgba(0, 230, 118, 0.2)" 
                                   : booking.status === "PENDING" ? "rgba(255, 234, 0, 0.2)"
                                   : "rgba(255, 51, 102, 0.2)"}`
              }}>
                <div style={{
                  width: 8, height: 8, borderRadius: "50%",
                  background: "currentColor",
                  boxShadow: `0 0 10px currentColor`
                }} />
                {booking.status}
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-8 items-start">
              
              {/* Order Details Card */}
              <div style={{
                background: "var(--bg-secondary)",
                border: "1px solid var(--glass-border)",
                borderRadius: "var(--radius-xl)",
                padding: "2.5rem",
                boxShadow: "var(--shadow-md)"
              }}>
                <h2 style={{ fontSize: "1.25rem", fontWeight: 700, marginBottom: "1.5rem", paddingBottom: "1rem", borderBottom: "1px solid var(--glass-border)" }}>
                  Order Details
                </h2>
                
                <div style={{ display: "flex", flexDirection: "column", gap: "1rem" }}>
                  <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                    <span style={{ color: "var(--text-secondary)", fontSize: "0.95rem" }}>Event ID</span>
                    <span style={{ fontWeight: 600 }}>#{booking.eventId}</span>
                  </div>
                  <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                    <span style={{ color: "var(--text-secondary)", fontSize: "0.95rem" }}>Date Ordered</span>
                    <span style={{ fontWeight: 600 }}>{new Date(booking.createdAt).toLocaleString()}</span>
                  </div>
                  <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", paddingTop: "1rem", borderTop: "1px solid var(--glass-border)", marginTop: "0.5rem" }}>
                    <span style={{ color: "var(--text-secondary)", fontSize: "1.1rem" }}>Total</span>
                    <span style={{ fontWeight: 800, fontSize: "1.75rem", color: "var(--accent-primary)" }}>
                      ${Number(booking.total).toFixed(2)}
                    </span>
                  </div>
                </div>

                <div style={{ marginTop: "2.5rem", paddingTop: "2rem", borderTop: "1px solid var(--glass-border)" }}>
                  <h3 style={{ fontSize: "1rem", fontWeight: 700, marginBottom: "1rem", color: "var(--text-secondary)" }}>
                    Line Items
                  </h3>
                  
                  {booking.items.length === 0 ? (
                    <div style={{ color: "var(--text-muted)", fontSize: "0.9rem", fontStyle: "italic" }}>No items in this booking.</div>
                  ) : (
                    <div style={{ display: "flex", flexDirection: "column", gap: "0.75rem" }}>
                      {booking.items.map((item) => (
                        <div
                          key={item.id}
                          style={{
                            display: "flex",
                            justifyContent: "space-between",
                            alignItems: "center",
                            background: "var(--bg-tertiary)",
                            padding: "1rem",
                            borderRadius: "var(--radius-lg)",
                            border: "1px solid var(--glass-border)"
                          }}
                        >
                          <div style={{ display: "flex", alignItems: "center", gap: "0.75rem" }}>
                            <Ticket size={16} className="text-[#00f0ff]" />
                            <div>
                              <div style={{ fontWeight: 600, fontSize: "0.95rem" }}>
                                {item.seatId ? "Reserved Seat" : "General Admission"}
                              </div>
                              <div style={{ fontSize: "0.8rem", color: "var(--text-muted)", fontFamily: "monospace" }}>
                                ID: {item.id}
                              </div>
                            </div>
                          </div>
                          <div style={{ fontWeight: 700 }}>${Number(item.price).toFixed(2)}</div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </div>

              {/* Digital Ticket / Status Card */}
              <div style={{
                background: "linear-gradient(145deg, var(--bg-secondary), var(--bg-tertiary))",
                border: booking.status === "CONFIRMED" ? "1px solid var(--accent-primary)" : "1px solid var(--glass-border)",
                borderRadius: "var(--radius-xl)",
                padding: "3rem",
                boxShadow: booking.status === "CONFIRMED" ? "0 20px 40px rgba(0,240,255,0.1)" : "var(--shadow-md)",
                display: "flex",
                flexDirection: "column",
                alignItems: "center",
                justifyContent: "center",
                textAlign: "center",
                minHeight: "400px",
                position: "relative",
                overflow: "hidden"
              }}>
                {booking.status === "CONFIRMED" ? (
                  <>
                    <div className="absolute top-0 left-0 right-0 h-2 bg-gradient-to-r from-[#00f0ff] to-[#7000ff]" />
                    <div style={{
                      width: "160px", height: "160px",
                      background: "white",
                      borderRadius: "1rem",
                      display: "flex",
                      flexDirection: "column",
                      alignItems: "center",
                      justifyContent: "center",
                      boxShadow: "0 10px 30px rgba(0,240,255,0.3)",
                      border: "4px dashed #e2e8f0",
                      marginBottom: "2rem"
                    }}>
                      <Ticket size={56} color="#000" style={{ marginBottom: "0.5rem" }} />
                      <div style={{ color: "#000", fontWeight: 900, fontSize: "1.1rem", letterSpacing: "0.05em" }}>VALID TICKET</div>
                    </div>
                    <h3 style={{ fontSize: "1.25rem", fontWeight: 700, marginBottom: "0.5rem" }}>Ready for Entry</h3>
                    <p style={{ color: "var(--text-secondary)", fontSize: "0.95rem", maxWidth: "80%" }}>
                      Present this QR code (simulated) at the venue for entry.
                    </p>
                  </>
                ) : (
                  <div style={{ display: "flex", flexDirection: "column", alignItems: "center" }}>
                    <div style={{
                      width: "80px", height: "80px",
                      background: "var(--bg-tertiary)",
                      borderRadius: "50%",
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                      marginBottom: "1.5rem",
                      border: "1px solid var(--glass-border)"
                    }}>
                      <Ticket size={32} style={{ opacity: 0.5 }} />
                    </div>
                    <h3 style={{ fontSize: "1.5rem", fontWeight: 700, marginBottom: "0.5rem" }}>Tickets Unavailable</h3>
                    <p style={{ color: "var(--text-secondary)", fontSize: "1rem", maxWidth: "250px", lineHeight: 1.5 }}>
                      {booking.status === "PENDING"
                        ? "Your booking is pending payment completion."
                        : "This booking was cancelled or expired. No tickets were issued."}
                    </p>
                  </div>
                )}
              </div>

            </div>
          </>
        )}
      </div>
    </main>
  );
}
