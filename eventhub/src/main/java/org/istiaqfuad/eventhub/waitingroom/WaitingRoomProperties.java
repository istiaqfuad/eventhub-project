package org.istiaqfuad.eventhub.waitingroom;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Waiting room tuning parameters. Externalized so drain rate, admission
 * window, and poll cadence can be adjusted per environment without code changes.
 */
@ConfigurationProperties(prefix = "app.waiting-room")
public record WaitingRoomProperties(
        int drainPerSecond,
        Duration admissionTtl,
        Duration pollDelay
) {
    public WaitingRoomProperties {
        if (drainPerSecond <= 0) drainPerSecond = 10;
        if (admissionTtl == null) admissionTtl = Duration.ofMinutes(5);
        if (pollDelay == null || pollDelay.isZero() || pollDelay.isNegative()) {
            pollDelay = Duration.ofSeconds(1);
        }
    }
}
