package org.istiaqfuad.eventhub.common.config;

import org.istiaqfuad.eventhub.security.userdetails.AppUserPrincipal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Enables Spring Data JPA auditing so {@code @CreatedBy}/{@code @LastModifiedBy} on
 * {@link org.istiaqfuad.eventhub.common.AuditableEntity} get populated on write.
 *
 * <p>Supplies the current actor id from the security context. Two principal shapes are
 * possible: {@link AppUserPrincipal} (password login) and a bare {@code Long} user id
 * (bearer token). Anonymous/system writes leave {@code created_by}/{@code updated_by} null.
 */
@Configuration
@EnableJpaAuditing
public class AuditingConfig {

    @Bean
    public AuditorAware<Long> auditorAware() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()
                    || auth instanceof AnonymousAuthenticationToken) {
                return Optional.empty();
            }
            Object principal = auth.getPrincipal();
            if (principal instanceof AppUserPrincipal p) {
                return Optional.of(p.getUserId());
            }
            if (principal instanceof Long id) {
                return Optional.of(id);
            }
            return Optional.empty();
        };
    }
}
