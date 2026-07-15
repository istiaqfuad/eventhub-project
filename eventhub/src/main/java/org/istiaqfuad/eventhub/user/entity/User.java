package org.istiaqfuad.eventhub.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.UuidGenerator;
import org.istiaqfuad.eventhub.common.AuditableEntity;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import static org.hibernate.annotations.UuidGenerator.Style.TIME;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @ColumnDefault("false")
    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    @UuidGenerator(style = TIME)
    @Column(name = "public_id", nullable = false, updatable = false)
    private UUID publicId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new LinkedHashSet<>();


}
