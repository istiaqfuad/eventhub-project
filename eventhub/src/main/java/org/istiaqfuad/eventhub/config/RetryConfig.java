package org.istiaqfuad.eventhub.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Enables Spring Retry AOP support. Combined with {@code @Retryable} on service
 * methods, this provides bounded retry with backoff on transient failures such
 * as {@code ObjectOptimisticLockingFailureException} during concurrent seat booking.
 */
@Configuration
@EnableRetry
public class RetryConfig {
}
