"use client";

import React from "react";
import { SeatResponse, SectionWithSeatsResponse } from "../../lib/types";

interface SeatMapProps {
  sections: SectionWithSeatsResponse[];
  selectedSeats: Set<number>;
  onSeatToggle: (seat: SeatResponse, sectionName: string, basePrice: number) => void;
}

export default function InteractiveSeatMap({ sections, selectedSeats, onSeatToggle }: SeatMapProps) {
  
  const renderSection = (sectionData: SectionWithSeatsResponse) => {
    const { section, seats } = sectionData;
    
    const rowMap = new Map<string, SeatResponse[]>();
    seats.forEach(seat => {
      if (!rowMap.has(seat.rowLabel)) rowMap.set(seat.rowLabel, []);
      rowMap.get(seat.rowLabel)!.push(seat);
    });

    const sortedRows = Array.from(rowMap.keys()).sort();

    return (
      <div key={section.id} className="flex flex-col items-center gap-4 mb-8">
        <div className="text-lg font-semibold text-white uppercase tracking-widest">{section.name}</div>
        
        {sortedRows.map(rowLabel => {
          const rowSeats = rowMap.get(rowLabel)!;
          rowSeats.sort((a, b) => a.colNumber - b.colNumber);

          return (
            <div key={rowLabel} className="flex gap-2 items-center">
              <div className="w-8 text-right font-semibold text-gray-400 text-sm">{rowLabel}</div>
              {rowSeats.map(seat => {
                const isSelected = selectedSeats.has(seat.id);
                // Status styles
                const isFree = seat.status === "FREE";
                const baseClasses = "w-9 h-9 rounded-t-lg rounded-b flex items-center justify-center text-xs font-bold transition-all relative group select-none";
                
                let statusClasses = "";
                if (isSelected) {
                  statusClasses = "bg-[#00f0ff] border border-[#00f0ff] text-white -translate-y-0.5 shadow-[0_4px_12px_rgba(0,240,255,0.4)] cursor-pointer";
                } else if (isFree) {
                  statusClasses = "bg-white/5 border border-white/10 text-gray-400 hover:border-[#00f0ff] hover:-translate-y-0.5 hover:shadow-[0_4px_12px_rgba(0,240,255,0.2)] hover:text-white cursor-pointer";
                } else {
                  statusClasses = "bg-white/5 border border-white/10 text-white/20 cursor-not-allowed";
                }
                
                return (
                  <div
                    key={seat.id}
                    className={`${baseClasses} ${statusClasses}`}
                    onClick={() => {
                      if (isFree || isSelected) {
                        onSeatToggle(seat, section.name, Number(section.basePrice));
                      }
                    }}
                  >
                    {seat.colNumber}
                    <div className="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 bg-[#151a23] border border-white/10 px-2 py-1 rounded text-[10px] whitespace-nowrap opacity-0 group-hover:opacity-100 pointer-events-none transition-opacity z-10">
                      Row {rowLabel} - Seat {seat.colNumber} (${Number(section.basePrice).toFixed(2)})
                    </div>
                  </div>
                );
              })}
              <div className="w-8 text-left font-semibold text-gray-400 text-sm">{rowLabel}</div>
            </div>
          );
        })}
      </div>
    );
  };

  if (!sections || sections.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center p-16 text-center bg-white/5 rounded-xl border border-dashed border-white/10">
        <h3 className="text-[#7000ff] mb-2 text-2xl font-bold">General Admission</h3>
        <p className="text-gray-400">This venue does not have assigned seating.</p>
      </div>
    );
  }

  const sectionsWithSeats = sections.filter(s => s.seats && s.seats.length > 0);

  if (sectionsWithSeats.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center p-16 text-center bg-white/5 rounded-xl border border-dashed border-white/10">
        <h3 className="text-[#7000ff] mb-2 text-2xl font-bold">General Admission Event</h3>
        <p className="text-gray-400">Please use the Ticket Types selector on the left to choose your tickets.</p>
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-12 overflow-x-auto p-8 bg-[#202632] rounded-xl min-h-[400px]">
      <div className="w-4/5 mx-auto h-16 bg-gradient-to-b from-[#00f0ff]/20 to-transparent rounded-t-full border-t-2 border-[#00f0ff] flex items-end justify-center pb-2 text-[#00f0ff] font-bold tracking-[0.3em] text-shadow-sm">
        STAGE
      </div>
      
      <div className="flex flex-col gap-12 mt-8">
        {sectionsWithSeats.map(renderSection)}
      </div>
      
      <div className="flex justify-center gap-8 mt-12 pt-4 border-t border-white/10">
        <div className="flex items-center gap-2">
          <div className="w-5 h-5 rounded flex items-center justify-center bg-white/5 border border-white/10" /> 
          <span className="text-sm text-gray-400">Available</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-5 h-5 rounded flex items-center justify-center bg-[#00f0ff] border border-[#00f0ff]" /> 
          <span className="text-sm text-gray-400">Selected</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-5 h-5 rounded flex items-center justify-center bg-white/5 border border-white/10 opacity-50" /> 
          <span className="text-sm text-gray-400">Unavailable</span>
        </div>
      </div>
    </div>
  );
}
