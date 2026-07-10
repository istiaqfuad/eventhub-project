import { useQuery } from "@tanstack/react-query";
import apiClient from "../lib/api-client";

// Exporting types for convenience
export interface VenueResponse {
  id: number;
  name: string;
  layoutType: "SEATED" | "GENERAL_ADMISSION" | "MIXED";
  address: string;
  city: string;
  createdAt: string;
  updatedAt: string;
}

export interface SeatResponse {
  id: number;
  sectionId: number;
  rowLabel: string;
  colNumber: number;
  status: "FREE" | "RESERVED" | "BOOKED" | "HELD";
  version: number;
  createdAt: string;
  updatedAt: string;
}

export interface SectionResponse {
  id: number;
  venueId: number;
  name: string;
  seatType: "RESERVED" | "GENERAL";
  basePrice: number;
  createdAt: string;
  updatedAt: string;
}

export interface SectionWithSeatsResponse {
  section: SectionResponse;
  seats: SeatResponse[];
}

export interface VenueLayoutResponse {
  venue: VenueResponse;
  sections: SectionWithSeatsResponse[];
}

export interface TicketTypeResponse {
  id: number;
  eventId: number;
  name: string;
  price: number;
  quota: number;
  createdAt: string;
  updatedAt: string;
}

// Hook to get available ticket types for an event
export const useTicketTypes = (eventId: string) => {
  return useQuery<TicketTypeResponse[]>({
    queryKey: ["events", eventId, "tickets"],
    queryFn: async () => {
      const { data } = await apiClient.get(`/events/${eventId}/tickets`);
      return data;
    },
    enabled: !!eventId,
  });
};

// Hook to get the venue layout (sections and seats)
export const useVenueLayout = (venueId: string | number) => {
  return useQuery<VenueLayoutResponse>({
    queryKey: ["venues", venueId, "layout"],
    queryFn: async () => {
      const { data } = await apiClient.get(`/venues/${venueId}/layout`);
      return data;
    },
    enabled: !!venueId,
  });
};
