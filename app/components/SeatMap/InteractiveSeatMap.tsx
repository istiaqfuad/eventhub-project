"use client";

import React, { useMemo } from "react";
import styles from "./InteractiveSeatMap.module.css";
import { SeatResponse, SectionWithSeatsResponse } from "../../hooks/useBooking";

interface SeatMapProps {
  sections: SectionWithSeatsResponse[];
  selectedSeats: Set<number>; // Set of selected Seat IDs
  onSeatToggle: (seat: SeatResponse, sectionName: string, basePrice: number) => void;
}

export default function InteractiveSeatMap({ sections, selectedSeats, onSeatToggle }: SeatMapProps) {
  
  // Group seats by row for easier rendering
  const renderSection = (sectionData: SectionWithSeatsResponse) => {
    const { section, seats } = sectionData;
    
    // Group seats by rowLabel
    const rowMap = new Map<string, SeatResponse[]>();
    seats.forEach(seat => {
      if (!rowMap.has(seat.rowLabel)) {
        rowMap.set(seat.rowLabel, []);
      }
      rowMap.get(seat.rowLabel)!.push(seat);
    });

    // Sort rows alphabetically (A, B, C...)
    const sortedRows = Array.from(rowMap.keys()).sort();

    return (
      <div key={section.id} className={styles.section}>
        <div className={styles.sectionName}>{section.name}</div>
        
        {sortedRows.map(rowLabel => {
          const rowSeats = rowMap.get(rowLabel)!;
          // Sort seats by column number
          rowSeats.sort((a, b) => a.colNumber - b.colNumber);

          return (
            <div key={rowLabel} className={styles.row}>
              <div className={styles.rowLabel}>{rowLabel}</div>
              {rowSeats.map(seat => {
                const isSelected = selectedSeats.has(seat.id);
                const statusClass = isSelected ? styles.SELECTED : styles[seat.status] || styles.BOOKED;
                
                return (
                  <div
                    key={seat.id}
                    className={`${styles.seat} ${statusClass}`}
                    onClick={() => {
                      if (seat.status === "FREE" || isSelected) {
                        onSeatToggle(seat, section.name, section.basePrice);
                      }
                    }}
                    data-tooltip={`Row ${rowLabel} - Seat ${seat.colNumber} ($${section.basePrice})`}
                  >
                    {seat.colNumber}
                  </div>
                );
              })}
              <div className={styles.rowLabel} style={{ textAlign: "left" }}>{rowLabel}</div>
            </div>
          );
        })}
      </div>
    );
  };

  if (!sections || sections.length === 0) {
    return (
      <div className={styles.gaContainer}>
        <h3>General Admission</h3>
        <p style={{ color: 'var(--text-secondary)' }}>This venue does not have assigned seating.</p>
      </div>
    );
  }

  // Check if it's purely general admission based on seatType
  const hasReservedSeating = sections.some(s => s.section.seatType === "RESERVED");

  if (!hasReservedSeating) {
    return (
      <div className={styles.gaContainer}>
        <h3>General Admission Event</h3>
        <p style={{ color: 'var(--text-secondary)' }}>
          Please use the Ticket Types selector on the left to choose your tickets.
        </p>
      </div>
    );
  }

  return (
    <div className={styles.seatMapContainer}>
      <div className={styles.stage}>STAGE</div>
      
      {/* Render all sections */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '3rem', marginTop: '2rem' }}>
        {sections.filter(s => s.section.seatType === "RESERVED").map(renderSection)}
      </div>
      
      {/* Legend */}
      <div style={{ display: 'flex', justifyContent: 'center', gap: '2rem', marginTop: '3rem', paddingTop: '1rem', borderTop: '1px solid var(--glass-border)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <div className={`${styles.seat} ${styles.FREE}`} style={{ width: 20, height: 20, transform: 'none' }} /> 
          <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Available</span>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <div className={`${styles.seat} ${styles.SELECTED}`} style={{ width: 20, height: 20, transform: 'none' }} /> 
          <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Selected</span>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <div className={`${styles.seat} ${styles.BOOKED}`} style={{ width: 20, height: 20, transform: 'none' }} /> 
          <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Unavailable</span>
        </div>
      </div>
    </div>
  );
}
