package org.istiaqfuad.eventhub.waitingroom;

/**
 * Response returned when a user joins the waiting queue.
 *
 * @param position 0-indexed queue position (0 = next to be admitted)
 * @param message  human-readable status
 */
public record QueuePositionResponse(long position, String message) {
}
