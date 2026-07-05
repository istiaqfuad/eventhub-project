package org.istiaqfuad.eventhub.booking;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Enables the hold-expiry scheduler and binds {@link BookingProperties}. */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(BookingProperties.class)
public class BookingConfig {
}
