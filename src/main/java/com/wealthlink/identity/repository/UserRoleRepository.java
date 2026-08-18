package com.wealthlink.identity.repository;

import com.wealthlink.identity.entity.UserRole;
import com.wealthlink.identity.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
    List<UserRole> findByUserId(UUID userId);
}
