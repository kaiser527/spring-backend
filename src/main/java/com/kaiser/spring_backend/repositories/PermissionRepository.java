package com.kaiser.spring_backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.kaiser.spring_backend.entities.Permission;
import com.kaiser.spring_backend.enums.ApiMethod;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {
    boolean existsByApiPathAndMethod(String apiPath, ApiMethod method);

    boolean existsByApiPathAndMethodAndIdNot(String apiPath, ApiMethod method, String id);
}
