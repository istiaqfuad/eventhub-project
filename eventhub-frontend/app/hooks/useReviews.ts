"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import apiClient from "../lib/api-client";
import type { ReviewRequest, ReviewResponse } from "../lib/types";

export const useEventReviews = (eventId: string | number) => {
  return useQuery<ReviewResponse[]>({
    queryKey: ["reviews", "event", String(eventId)],
    queryFn: async () => {
      const { data } = await apiClient.get<ReviewResponse[]>("/reviews", {
        params: { eventId },
      });
      return data;
    },
    enabled: !!eventId,
  });
};

export const useCreateReview = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (body: ReviewRequest) => {
      const { data } = await apiClient.post<ReviewResponse>("/reviews", body);
      return data;
    },
    onSuccess: (review) => {
      queryClient.invalidateQueries({ queryKey: ["reviews", "event", String(review.eventId)] });
    },
  });
};
