package org.istiaqfuad.eventhub.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.istiaqfuad.eventhub.common.AuditableEntity;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "refunds")
public class Refund extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'REQUESTED'")
    @Column(name = "status", nullable = false, length = 15)
    private RefundStatus status;

    @Column(name = "reason", length = 512)
    private String reason;


}
