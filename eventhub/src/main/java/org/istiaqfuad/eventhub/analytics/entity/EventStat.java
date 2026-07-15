package org.istiaqfuad.eventhub.analytics.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.istiaqfuad.eventhub.common.BaseEntity;
import org.istiaqfuad.eventhub.event.entity.Event;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "event_stats")
public class EventStat extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ColumnDefault("0")
    @Column(name = "tickets_sold", nullable = false)
    private Long ticketsSold;

    @ColumnDefault("0")
    @Column(name = "revenue", nullable = false, precision = 14, scale = 2)
    private BigDecimal revenue;

    @ColumnDefault("0")
    @Column(name = "occupancy_rate", nullable = false)
    private Double occupancyRate;


}
