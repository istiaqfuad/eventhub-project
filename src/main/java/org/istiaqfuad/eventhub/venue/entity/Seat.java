package org.istiaqfuad.eventhub.venue.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.istiaqfuad.eventhub.common.BaseEntity;

@Getter
@Setter
@Entity
@Table(name = "seats")
public class Seat extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @Column(name = "row_label", nullable = false, length = 10)
    private String rowLabel;

    @Column(name = "col_number", nullable = false)
    private Integer colNumber;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'FREE'")
    @Column(name = "status", nullable = false, length = 10)
    private SeatStatus status;

    @Version
    @ColumnDefault("0")
    @Column(name = "version", nullable = false)
    private Long version;


}
