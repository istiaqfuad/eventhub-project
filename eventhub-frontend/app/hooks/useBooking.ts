"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import apiClient from "../lib/api-client";
import type {
  BookingRequest,
  BookingResponse,
  PaymentRequest,
  PaymentResponse,
  SeatResponse,
  TicketTypeResponse,
  VenueLayoutResponse,
} from "../lib/types";

export type {
  BookingRequest,
  BookingResponse,
  PaymentResponse,
  SeatResponse,
  TicketTypeResponse,
  VenueLayoutResponse,
  SectionWithSeatsResponse,
  VenueResponse,
  SectionResponse,
} from "../lib/types";

export const useTicketTypes = (eventId: string | number) => {
  return useQuery<TicketTypeResponse[]>({
    queryKey: ["events", String(eventId), "tickets"],
    queryFn: async () => {
      const { data } = await apiClient.get<TicketTypeResponse[]>(`/events/${eventId}/tickets`);
      return data;
    },
    enabled: !!eventId,
  });
};

export const useVenueLayout = (venueId: string | number | null | undefined) => {
  return useQuery<VenueLayoutResponse>({
    queryKey: ["venues", String(venueId), "layout"],
    queryFn: async () => {
      const { data } = await apiClient.get<VenueLayoutResponse>(`/venues/${venueId}/layout`);
      return data;
    },
    enabled: !!venueId,
  });
};

export const useBooking = (id: string | number | null | undefined) => {
  return useQuery<BookingResponse>({
    queryKey: ["bookings", String(id)],
    queryFn: async () => {
      const { data } = await apiClient.get<BookingResponse>(`/bookings/${id}`);
      return data;
    },
    enabled: !!id,
    refetchInterval: (query) => {
      const status = query.state.data?.status;
      return status === "PENDING" ? 3000 : false;
    },
  });
};

export const useMyBookings = (enabled = true) => {
  return useQuery<BookingResponse[]>({
    queryKey: ["bookings", "mine"],
    queryFn: async () => {
      const { data } = await apiClient.get<BookingResponse[]>("/bookings");
      return data;
    },
    enabled,
  });
};

export const useCreateBooking = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (body: BookingRequest) => {
      const { data } = await apiClient.post<BookingResponse>("/bookings", body);
      return data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["bookings"] });
      queryClient.invalidateQueries({ queryKey: ["venues"] });
    },
  });
};

export const useCreatePayment = () => {
  return useMutation({
    mutationFn: async (body: PaymentRequest) => {
      const { data } = await apiClient.post<PaymentResponse>("/payments", body);
      return data;
    },
  });
};

/** Build booking line items from seat selection + GA quantities. */
export function buildBookingItems(
  selectedSeatIds: number[],
  gaQuantities: Map<number, number>
): BookingRequest["items"] {
  const items: BookingRequest["items"] = [];
  for (const seatId of selectedSeatIds) {
    items.push({ seatId, ticketTypeId: null });
  }
  gaQuantities.forEach((qty, ticketTypeId) => {
    for (let i = 0; i < qty; i++) {
      items.push({ seatId: null, ticketTypeId });
    }
  });
  return items;
}
