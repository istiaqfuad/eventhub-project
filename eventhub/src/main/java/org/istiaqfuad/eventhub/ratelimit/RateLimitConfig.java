package org.istiaqfuad.eventhub.ratelimit;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Registers {@link RateLimitFilter} as a servlet filter at high precedence
 * so it runs before Spring Security's filter chain.
 */
@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitConfig {

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter(
            StringRedisTemplate redis, RateLimitProperties properties) {
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RateLimitFilter(redis, properties));
        registration.addUrlPatterns(
                "/api/auth/login",
                "/api/bookings",
                "/api/bookings/*",
                "/api/payments",
                "/api/payments/*"
        );
        // Runs before the default Spring Security filter chain order (100)
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return registration;
    }
}
