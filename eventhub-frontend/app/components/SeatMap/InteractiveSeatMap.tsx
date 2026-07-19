"use client";

import React from "react";
import { SeatResponse, SectionWithSeatsResponse } from "../../lib/types";

interface SeatMapProps {
  sections: SectionWithSeatsResponse[];
  selectedSeats?: Set<number>;
  onSeatToggle?: (seat: SeatResponse, sectionName: string, basePrice: number) => void;
  isReadOnly?: boolean;
}

export default function InteractiveSeatMap({ 
  sections, 
  selectedSeats = new Set(), 
  onSeatToggle,
  isReadOnly = false
}: SeatMapProps) {
  
  const renderSection = (sectionData: SectionWithSeatsResponse) => {
    const { section, seats } = sectionData;
    
    const rowMap = new Map<string, SeatResponse[]>();
    seats.forEach(seat => {
      if (!rowMap.has(seat.rowLabel)) rowMap.set(seat.rowLabel, []);
      rowMap.get(seat.rowLabel)!.push(seat);
    });

    const sortedRows = Array.from(rowMap.keys()).sort();

    return (
      <div key={section.id} className="flex flex-col items-center gap-4 mb-10 w-full overflow-x-auto pb-4">
        <div className="text-lg font-semibold text-white uppercase tracking-widest bg-white/10 px-4 py-1 rounded-full mb-2">
          {section.name}
        </div>
        
        {sortedRows.map(rowLabel => {
          const rowSeats = rowMap.get(rowLabel)!;
          const maxCol = Math.max(...rowSeats.map(s => s.colNumber));
          const seatsByCol = new Map(rowSeats.map(s => [s.colNumber, s]));

          // Prevent rendering an absurd number of empty divs if colNumber is artificially huge
          const renderMaxCol = Math.min(maxCol, 150);

          return (
            <div key={rowLabel} className="flex gap-2 items-center w-max">
              <div className="w-8 text-right font-bold text-gray-400 text-sm shrink-0">{rowLabel}</div>
              
              <div className="flex gap-2">
                {rowSeats.map((seat, index) => {
                  const prevSeat = index > 0 ? rowSeats[index - 1] : null;
                  const isGap = prevSeat && seat.colNumber > prevSeat.colNumber + 1;

                  const isSelected = selectedSeats?.has(seat.id) ?? false;
                  const isFree = seat.status === "FREE";
                  
                  // Base seat styling
                  const baseClasses = "w-8 h-8 rounded-t-lg rounded-b-sm flex items-center justify-center text-[11px] font-bold transition-all relative group select-none shrink-0";
                  
                  let statusClasses = "";
                  
                  if (isReadOnly) {
                    statusClasses = "bg-white/10 text-gray-300";
                  } else {
                    if (isSelected) {
                      statusClasses = "bg-[#00f0ff] text-black shadow-[0_0_15px_rgba(0,240,255,0.6)] cursor-pointer transform -translate-y-1";
                    } else if (isFree) {
                      statusClasses = "bg-white/10 text-gray-300 hover:bg-[#00f0ff]/20 hover:text-[#00f0ff] hover:shadow-[0_0_10px_rgba(0,240,255,0.3)] cursor-pointer";
                    } else {
                      statusClasses = "bg-white/5 text-white/10 cursor-not-allowed";
                    }
                  }
                  
                  return (
                    <React.Fragment key={seat.id}>
                      {isGap && <div className="w-6 h-8 shrink-0 border-l border-white/5 mx-1" />}
                      <div
                        className={`${baseClasses} ${statusClasses}`}
                        onClick={() => {
                          if (!isReadOnly && onSeatToggle && (isFree || isSelected)) {
                            onSeatToggle(seat, section.name, Number(section.basePrice));
                          }
                        }}
                      >
                        {seat.colNumber}
                        
                        <div className="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 bg-[#151a23] border border-white/20 px-2 py-1 rounded text-xs whitespace-nowrap opacity-0 group-hover:opacity-100 pointer-events-none transition-opacity z-10 shadow-lg text-white font-medium">
                          Row {rowLabel} - Seat {seat.colNumber} 
                          {!isReadOnly && <span className="text-gray-400 ml-1 font-normal">(${Number(section.basePrice).toFixed(2)})</span>}
                        </div>
                      </div>
                    </React.Fragment>
                  );
                })}
              </div>

              <div className="w-8 text-left font-bold text-gray-400 text-sm shrink-0">{rowLabel}</div>
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
        <p className="text-gray-400">{isReadOnly ? "This venue is general admission." : "Please use the Ticket Types selector on the left to choose your tickets."}</p>
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
      
      {!isReadOnly && (
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
      )}
    </div>
  );
}
