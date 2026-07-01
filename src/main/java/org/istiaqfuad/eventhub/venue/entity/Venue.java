package org.istiaqfuad.eventhub.venue.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.istiaqfuad.eventhub.common.BaseEntity;

@Getter
@Setter
@Entity
@Table(name = "venues")
public class Venue extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "layout_type", nullable = false, length = 20)
    private LayoutType layoutType;

    @Column(name = "address")
    private String address;

    @Column(name = "city", length = 120)
    private String city;


}
