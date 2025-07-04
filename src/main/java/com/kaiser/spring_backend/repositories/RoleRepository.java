package com.kaiser.spring_backend.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.kaiser.spring_backend.entities.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, String id);

    @EntityGraph(attributePaths = {"permission"})
    Optional<Role> findWithPermissionsById(String id);
}