"use client";

import { use, useState } from "react";
import Link from "next/link";
import { ArrowLeft, MapPin, Ticket } from "lucide-react";
import Navbar from "../../../components/Navbar";
import { useEvent } from "../../../hooks/useEvents";
import { useTicketTypes, useVenueLayout, SeatResponse } from "../../../hooks/useBooking";
import InteractiveSeatMap from "../../../components/SeatMap/InteractiveSeatMap";

export default function BookingPage({ params }: { params: Promise<{ id: string }> }) {
  const resolvedParams = use(params);
  const { data: event, isLoading: isLoadingEvent } = useEvent(resolvedParams.id);
  const { data: ticketTypes, isLoading: isLoadingTickets } = useTicketTypes(resolvedParams.id);
  
  const { data: layout, isLoading: isLoadingLayout } = useVenueLayout(event?.venueId || "");

  // State to track selected seats for RESERVED sections
  // Maps Seat ID to an object with details for the cart
  const [selectedSeatsMap, setSelectedSeatsMap] = useState<Map<number, { seat: SeatResponse, sectionName: string, price: number }>>(new Map());

  // State to track GA ticket quantities (maps TicketType ID to quantity)
  const [gaQuantities, setGaQuantities] = useState<Map<number, number>>(new Map());

  if (isLoadingEvent || isLoadingTickets) {
    return (
      <main>
        <Navbar />
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', color: 'var(--text-secondary)' }}>
          Loading booking engine...
        </div>
      </main>
    );
  }

  if (!event) return null;

  const handleSeatToggle = (seat: SeatResponse, sectionName: string, price: number) => {
    setSelectedSeatsMap(prev => {
      const newMap = new Map(prev);
      if (newMap.has(seat.id)) {
        newMap.delete(seat.id);
      } else {
        newMap.set(seat.id, { seat, sectionName, price });
      }
      return newMap;
    });
  };

  const handleGaQuantityChange = (ticketId: number, quantity: number) => {
    setGaQuantities(prev => {
      const newMap = new Map(prev);
      if (quantity === 0) {
        newMap.delete(ticketId);
      } else {
        newMap.set(ticketId, quantity);
      }
      return newMap;
    });
  };

  // Calculate Order Total
  let totalAmount = 0;
  
  // Add GA tickets to total
  gaQuantities.forEach((quantity, ticketId) => {
    const ticket = ticketTypes?.find(t => t.id === ticketId);
    if (ticket) totalAmount += ticket.price * quantity;
  });

  // Add Reserved Seats to total
  selectedSeatsMap.forEach(item => {
    totalAmount += item.price;
  });

  const isCartEmpty = totalAmount === 0;

  return (
    <main>
      <Navbar />
      
      <div className="container" style={{ paddingTop: '100px', minHeight: '100vh', paddingBottom: 'var(--space-2xl)' }}>
        <div style={{ marginBottom: 'var(--space-xl)' }}>
          <Link href={`/events/${resolvedParams.id}`} style={{ color: 'var(--text-secondary)', display: 'inline-flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem' }}>
            <ArrowLeft size={16} /> Back to Event Details
          </Link>
          <h1 style={{ fontSize: '2.5rem', marginBottom: '0.5rem' }}>Select Tickets</h1>
          <p style={{ color: 'var(--text-secondary)' }}>{event.title}</p>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 350px', gap: 'var(--space-2xl)' }}>
          {/* Main Booking Area */}
          <div>
            <div style={{ background: 'var(--bg-secondary)', border: '1px solid var(--glass-border)', borderRadius: 'var(--radius-lg)', padding: 'var(--space-xl)', marginBottom: 'var(--space-xl)' }}>
              <h2 style={{ marginBottom: '1rem' }}>General Admission Tickets</h2>
              {ticketTypes && ticketTypes.length > 0 ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                  {ticketTypes.map(ticket => (
                    <div key={ticket.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '1rem', border: '1px solid var(--glass-border)', borderRadius: 'var(--radius-md)', background: 'var(--bg-tertiary)' }}>
                      <div>
                        <div style={{ fontWeight: 600, fontSize: '1.1rem' }}>{ticket.name}</div>
                        <div style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>${ticket.price}</div>
                      </div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                        <select 
                          className="input-field" 
                          style={{ width: '80px', padding: '0.5rem' }}
                          value={gaQuantities.get(ticket.id) || 0}
                          onChange={(e) => handleGaQuantityChange(ticket.id, parseInt(e.target.value))}
                        >
                          {[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10].map(num => (
                            <option key={num} value={num}>{num}</option>
                          ))}
                        </select>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div style={{ color: 'var(--text-secondary)' }}>No ticket types available.</div>
              )}
            </div>

            {event.venueId && (
              <div style={{ background: 'var(--bg-secondary)', border: '1px solid var(--glass-border)', borderRadius: 'var(--radius-lg)', padding: 'var(--space-xl)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                  <h2>Venue Layout</h2>
                  <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <MapPin size={14} /> {event.city}
                  </span>
                </div>
                
                {isLoadingLayout ? (
                  <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-secondary)' }}>Loading Seat Map...</div>
                ) : layout ? (
                  <InteractiveSeatMap 
                    sections={layout.sections} 
                    selectedSeats={new Set(selectedSeatsMap.keys())}
                    onSeatToggle={handleSeatToggle}
                  />
                ) : (
                  <div style={{ color: 'var(--text-secondary)' }}>Venue layout unavailable.</div>
                )}
              </div>
            )}
          </div>

          {/* Checkout Sidebar */}
          <div>
            <div style={{ background: 'var(--bg-secondary)', border: '1px solid var(--glass-border)', borderRadius: 'var(--radius-lg)', padding: 'var(--space-xl)', position: 'sticky', top: '100px' }}>
              <h2 style={{ marginBottom: '1.5rem', fontSize: '1.5rem' }}>Order Summary</h2>
              
              {isCartEmpty ? (
                <div style={{ color: 'var(--text-secondary)', textAlign: 'center', padding: '2rem 0', borderBottom: '1px solid var(--glass-border)', marginBottom: '1.5rem' }}>
                  No tickets selected yet.
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', marginBottom: '1.5rem', paddingBottom: '1.5rem', borderBottom: '1px solid var(--glass-border)' }}>
                  
                  {/* List GA Tickets */}
                  {Array.from(gaQuantities.entries()).map(([ticketId, quantity]) => {
                    const ticket = ticketTypes?.find(t => t.id === ticketId);
                    if (!ticket) return null;
                    return (
                      <div key={`ga-${ticketId}`} style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.9rem' }}>
                        <span>{quantity}x {ticket.name}</span>
                        <span>${(ticket.price * quantity).toFixed(2)}</span>
                      </div>
                    );
                  })}

                  {/* List Reserved Seats */}
                  {Array.from(selectedSeatsMap.values()).map(({ seat, sectionName, price }) => (
                    <div key={`seat-${seat.id}`} style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.9rem' }}>
                      <span>
                        <Ticket size={12} style={{ display: 'inline', marginRight: '4px' }}/>
                        {sectionName} - Row {seat.rowLabel} Seat {seat.colNumber}
                      </span>
                      <span>${price.toFixed(2)}</span>
                    </div>
                  ))}

                </div>
              )}

              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1.5rem', fontSize: '1.2rem', fontWeight: 700 }}>
                <span>Total</span>
                <span>${totalAmount.toFixed(2)}</span>
              </div>

              <button 
                className={`btn ${isCartEmpty ? 'btn-secondary' : 'btn-primary'}`} 
                style={{ width: '100%', padding: '1rem', fontSize: '1.1rem' }} 
                disabled={isCartEmpty}
                onClick={() => alert("Stripe Checkout coming next!")}
              >
                Proceed to Checkout
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>
  );
}
