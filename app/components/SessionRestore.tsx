"use client";

import { useSessionRestore } from "../hooks/useAuth";

/** Restores access-token session on app load (client-only). */
export default function SessionRestore() {
  useSessionRestore();
  return null;
}
