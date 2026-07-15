package org.istiaqfuad.eventhub.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "idempotency_keys")
public class IdempotencyKey {
    @Id
    @Column(name = "idem_key", nullable = false, length = 80)
    private String idemKey;

    @Column(name = "request_hash", nullable = false, length = 128)
    private String requestHash;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response")
    private Map<String, Object> response;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private IdempotencyStatus status;

    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;


}
