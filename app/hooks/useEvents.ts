import { useQuery } from "@tanstack/react-query";
import apiClient from "../lib/api-client";

export interface EventResponse {
  id: number;
  publicId: string;
  organizerId: number;
  title: string;
  description: string;
  categoryId: number;
  venueId: number;
  city: string;
  latitude: number;
  longitude: number;
  startsAt: string;
  endsAt: string;
  status: "DRAFT" | "PUBLISHED" | "CANCELLED" | "COMPLETED";
  highDemand: boolean;
  imageUrls: string[];
  tagIds: number[];
  createdAt: string;
  updatedAt: string;
}

export const useEvents = () => {
  return useQuery<EventResponse[]>({
    queryKey: ["events"],
    queryFn: async () => {
      const { data } = await apiClient.get("/events");
      return data;
    },
  });
};

export const useEvent = (id: string) => {
  return useQuery<EventResponse>({
    queryKey: ["events", id],
    queryFn: async () => {
      const { data } = await apiClient.get(`/events/${id}`);
      return data;
    },
    enabled: !!id,
  });
};
