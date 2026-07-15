package org.istiaqfuad.eventhub.booking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UuidGenerator;
import org.istiaqfuad.eventhub.common.BaseEntity;
import org.istiaqfuad.eventhub.event.entity.TicketType;
import org.istiaqfuad.eventhub.venue.entity.Seat;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hibernate.annotations.UuidGenerator.Style.TIME;

@Getter
@Setter
@Entity
@Table(name = "booking_items")
public class BookingItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @OneToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "seat_id")
    private Seat seat;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "ticket_type_id")
    private TicketType ticketType;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @UuidGenerator(style = TIME)
    @Column(name = "public_id", nullable = false, updatable = false)
    private UUID publicId;


}
