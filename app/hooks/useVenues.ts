"use client";

import { useQuery } from "@tanstack/react-query";
import apiClient from "../lib/api-client";
import type { VenueLayoutResponse, VenueResponse } from "../lib/types";

export const useVenues = () => {
  return useQuery<VenueResponse[]>({
    queryKey: ["venues"],
    queryFn: async () => {
      const { data } = await apiClient.get<VenueResponse[]>("/venues");
      return data;
    },
  });
};

export const useVenue = (id: string | number) => {
  return useQuery<VenueResponse>({
    queryKey: ["venues", String(id)],
    queryFn: async () => {
      const { data } = await apiClient.get<VenueResponse>(`/venues/${id}`);
      return data;
    },
    enabled: !!id,
  });
};

export const useVenueLayout = (id: string | number) => {
  return useQuery<VenueLayoutResponse>({
    queryKey: ["venues", String(id), "layout"],
    queryFn: async () => {
      const { data } = await apiClient.get<VenueLayoutResponse>(`/venues/${id}/layout`);
      return data;
    },
    enabled: !!id,
  });
};
