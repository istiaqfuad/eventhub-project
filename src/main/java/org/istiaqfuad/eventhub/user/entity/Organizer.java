package org.istiaqfuad.eventhub.user.entity;

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
@Table(name = "organizers")
public class Organizer extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "org_name", nullable = false)
    private String orgName;

    @ColumnDefault("false")
    @Column(name = "verified", nullable = false)
    private Boolean verified;


}
