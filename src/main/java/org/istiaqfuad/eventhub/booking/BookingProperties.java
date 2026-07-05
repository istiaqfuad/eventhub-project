package org.istiaqfuad.eventhub.booking;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/** Booking configuration. {@code holdTtl} is how long a PENDING booking holds its inventory. */
@ConfigurationProperties(prefix = "app.booking")
public record BookingProperties(Duration holdTtl) {
}
