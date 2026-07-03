package org.istiaqfuad.eventhub.booking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UuidGenerator;
import org.istiaqfuad.eventhub.common.AuditableEntity;
import org.istiaqfuad.eventhub.event.entity.Event;
import org.istiaqfuad.eventhub.user.entity.User;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hibernate.annotations.UuidGenerator.Style.TIME;

@Getter
@Setter
@Entity
@Table(name = "bookings")
public class Booking extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'PENDING'")
    @Column(name = "status", nullable = false, length = 15)
    private BookingStatus status;

    @ColumnDefault("0")
    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @UuidGenerator(style = TIME)
    @Column(name = "public_id", nullable = false, updatable = false)
    private UUID publicId;


}
