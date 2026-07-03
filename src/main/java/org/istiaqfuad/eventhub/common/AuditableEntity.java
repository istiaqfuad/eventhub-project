package org.istiaqfuad.eventhub.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Auditable base for entities that must record <b>who</b> acted, on top of the
 * <b>when</b> timestamps from {@link BaseEntity}.
 *
 * <p>The listener only populates fields annotated with {@code @CreatedBy}/{@code @LastModifiedBy},
 * so the Hibernate-managed timestamps on {@link BaseEntity} are untouched. The values come from
 * the configured {@code AuditorAware} and stay {@code null} until Spring Security supplies an
 * authenticated principal (see {@code common.config.AuditingConfig}).
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity extends BaseEntity {

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Long lastModifiedBy;
}
