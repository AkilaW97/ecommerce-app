package com.ewis.ecommerce.repository;

import com.ewis.ecommerce.enums.AppRole;
import com.ewis.ecommerce.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
   Optional<Role> findByRoleName(AppRole appRole);
}
