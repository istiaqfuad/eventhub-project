package org.istiaqfuad.eventhub.user.repository;

import org.istiaqfuad.eventhub.user.entity.Role;
import org.istiaqfuad.eventhub.user.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
