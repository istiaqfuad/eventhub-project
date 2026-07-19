package org.istiaqfuad.eventhub.waitingroom;

import org.istiaqfuad.eventhub.security.web.CurrentUserId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Waiting-room endpoints for high-demand events.
 *
 * <p>Clients should:
 * <ol>
 *   <li>Call {@code POST .../join} once.</li>
 *   <li>Poll {@code GET .../status} until {@code state == ADMITTED}.</li>
 *   <li>Proceed to booking — the admission token expires after
 *       {@code app.waiting-room.admission-ttl} if unused.</li>
 * </ol>
 */
@RestController
@RequestMapping(path = "/events/{eventId}/queue", version = "1")
public class WaitingRoomController {

    private final WaitingRoomService service;

    public WaitingRoomController(WaitingRoomService service) {
        this.service = service;
    }

    /**
     * Joins the waiting queue for a high-demand event.
     * Idempotent: calling again for the same event re-reads the current position.
     */
    @PostMapping("/join")
    public ResponseEntity<QueuePositionResponse> join(
            @PathVariable Long eventId,
            @CurrentUserId Long userId) {
        long position = service.join(eventId, userId);
        String msg = position == 0
                ? "You are next — awaiting admission token"
                : "You are number " + (position + 1) + " in the queue";
        return ResponseEntity.ok(new QueuePositionResponse(position, msg));
    }

    /**
     * Returns the caller's current status: ADMITTED, QUEUED (with position), or NOT_IN_QUEUE.
     */
    @GetMapping("/status")
    public ResponseEntity<WaitingRoomStatus> status(
            @PathVariable Long eventId,
            @CurrentUserId Long userId) {
        return ResponseEntity.ok(service.getStatus(eventId, userId));
    }
}
