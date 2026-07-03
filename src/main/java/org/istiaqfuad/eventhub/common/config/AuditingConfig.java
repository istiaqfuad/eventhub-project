package org.istiaqfuad.eventhub.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * Enables Spring Data JPA auditing so {@code @CreatedBy}/{@code @LastModifiedBy} on
 * {@link org.istiaqfuad.eventhub.common.AuditableEntity} get populated on write.
 *
 * <p>Dormant for now: there is no Spring Security yet, so the {@link AuditorAware} returns no
 * actor and {@code created_by}/{@code updated_by} stay {@code null}. When security lands, the
 * only change needed is the body of {@link #auditorAware()} — read the current user id from
 * {@code SecurityContextHolder}. No entity or migration change required.
 */
@Configuration
@EnableJpaAuditing
public class AuditingConfig {

    @Bean
    public AuditorAware<Long> auditorAware() {
        // TODO(security): return the authenticated user id from SecurityContextHolder.
        return Optional::empty;
    }
}
