package org.istiaqfuad.eventhub.analytics;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.istiaqfuad.eventhub.common.BaseEntity;
import org.istiaqfuad.eventhub.event.Event;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "daily_sales")
public class DailySale extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "sales_date", nullable = false)
    private LocalDate salesDate;

    @ColumnDefault("0")
    @Column(name = "tickets_count", nullable = false)
    private Long ticketsCount;

    @ColumnDefault("0")
    @Column(name = "revenue", nullable = false, precision = 14, scale = 2)
    private BigDecimal revenue;


}
