export interface TokenResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface UserResponse {
  id: number;
  publicId: string;
  email: string;
  roles: string[];
  createdAt: string;
  updatedAt: string;
}

export type EventStatus = "DRAFT" | "ON_SALE" | "CLOSED" | "CANCELLED";

export interface EventResponse {
  id: number;
  publicId: string;
  organizerId: number;
  title: string;
  description: string;
  categoryId?: number;
  venueId?: number;
  city: string;
  latitude?: number;
  longitude?: number;
  startsAt: string;
  endsAt: string;
  status: EventStatus;
  highDemand: boolean;
  imageUrls: string[];
  tagIds: number[];
  createdAt: string;
  updatedAt: string;
}

export type SeatStatus = "FREE" | "HELD" | "BOOKED";
export type LayoutType = "STADIUM" | "THEATER" | "CONFERENCE_HALL" | "OPEN_GROUND";
export type SeatType = "REGULAR" | "VIP" | "PREMIUM" | "ACCESSIBLE";

export interface VenueResponse {
  id: number;
  name: string;
  layoutType: LayoutType;
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
  status: SeatStatus;
  version: number;
  createdAt: string;
  updatedAt: string;
}

export interface SectionResponse {
  id: number;
  venueId: number;
  name: string;
  seatType: SeatType;
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

export type BookingStatus = "PENDING" | "CONFIRMED" | "CANCELLED" | "EXPIRED";

export interface BookingResponse {
  id: number;
  userId: number;
  eventId: number;
  totalAmount: number;
  status: BookingStatus;
  createdAt: string;
  updatedAt: string;
  items: BookingItemResponse[];
}

export interface BookingItemResponse {
  id: number;
  bookingId: number;
  seatId?: number;
  ticketTypeId?: number;
  price: number;
}

export interface BookingRequest {
  eventId: number;
  items: BookingItemRequest[];
}

export interface BookingItemRequest {
  seatId?: number | null;
  ticketTypeId?: number | null;
}

export interface PaymentRequest {
  bookingId: number;
  idempotencyKey: string;
}

export interface PaymentResponse {
  id: number;
  bookingId: number;
  amount: number;
  status: "PENDING" | "SUCCEEDED" | "FAILED" | "REFUNDED";
  checkoutUrl?: string;
  createdAt: string;
  updatedAt: string;
}
export interface LoginRequest {
  email: string;
  password?: string;
}

export interface RegisterRequest {
  email: string;
  password?: string;
}

export interface ReviewRequest {
  eventId: number;
  rating: number;
  body: string;
}

export interface ReviewResponse {
  id: number;
  eventId: number;
  userId: number;
  rating: number;
  body: string;
  createdAt: string;
}
