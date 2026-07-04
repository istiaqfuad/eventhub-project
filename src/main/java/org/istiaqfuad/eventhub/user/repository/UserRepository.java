package org.istiaqfuad.eventhub.user.repository;

import org.istiaqfuad.eventhub.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // open-in-view=false: fetch roles in the same query so authorities resolve
    // for the UserDetailsService without a second round-trip.
    @EntityGraph(attributePaths = "roles")
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
