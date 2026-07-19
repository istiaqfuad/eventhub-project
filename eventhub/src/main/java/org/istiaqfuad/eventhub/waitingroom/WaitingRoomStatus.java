package org.istiaqfuad.eventhub.waitingroom;

/**
 * Snapshot of a user's position in the waiting room at a point in time.
 */
public record WaitingRoomStatus(
        State state,
        Long position  // null when not queued
) {

    public enum State {
        /** User has a valid admission token and may proceed to booking. */
        ADMITTED,
        /** User is in the queue; {@link #position()} shows 0-indexed rank. */
        QUEUED,
        /** User is not in the queue and has no admission token. */
        NOT_IN_QUEUE
    }

    public static WaitingRoomStatus admitted() {
        return new WaitingRoomStatus(State.ADMITTED, null);
    }

    public static WaitingRoomStatus queued(long position) {
        return new WaitingRoomStatus(State.QUEUED, position);
    }

    public static WaitingRoomStatus notInQueue() {
        return new WaitingRoomStatus(State.NOT_IN_QUEUE, null);
    }
}
