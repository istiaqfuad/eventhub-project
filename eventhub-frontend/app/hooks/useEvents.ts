"use client";

import { useQuery } from "@tanstack/react-query";
import apiClient from "../lib/api-client";
import type { EventResponse } from "../lib/types";

export type { EventResponse };

export const useEvents = () => {
  return useQuery<EventResponse[]>({
    queryKey: ["events"],
    queryFn: async () => {
      const { data } = await apiClient.get<{ content: EventResponse[] }>("/events?size=100&sort=startsAt,asc");
      return data.content;
    },
  });
};

export const useEvent = (id: string | number) => {
  return useQuery<EventResponse>({
    queryKey: ["events", String(id)],
    queryFn: async () => {
      const { data } = await apiClient.get<EventResponse>(`/events/${id}`);
      return data;
    },
    enabled: !!id && id !== "undefined",
  });
};
