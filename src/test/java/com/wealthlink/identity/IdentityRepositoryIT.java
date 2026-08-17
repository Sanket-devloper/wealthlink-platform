package com.wealthlink.identity;

import com.wealthlink.identity.entity.AppUser;
import com.wealthlink.identity.entity.Role;
import com.wealthlink.identity.entity.UserRole;
import com.wealthlink.identity.entity.UserRoleId;
import com.wealthlink.identity.entity.UserStatus;
import com.wealthlink.identity.repository.AppUserRepository;
import com.wealthlink.identity.repository.RoleRepository;
import com.wealthlink.identity.repository.UserRoleRepository;
import com.wealthlink.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Repository-layer tests for Identity & Access:
 * APP_USER, ROLE, USER_ROLE (RBAC junction).
 */
class IdentityRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Test
    void savingAUserDefaultsStatusAndTimestamps() {
        AppUser user = AppUser.builder()
                .username("kdev")
                .email("kdev@example.com")
                .passwordHash("hashed-value")
                .build();

        AppUser saved = appUserRepository.saveAndFlush(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE); // @PrePersist default
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void usernameUniqueConstraintIsEnforced() {
        appUserRepository.saveAndFlush(AppUser.builder()
                .username("duplicate-user")
                .email("first@example.com")
                .passwordHash("hash1")
                .build());

        AppUser secondWithSameUsername = AppUser.builder()
                .username("duplicate-user")
                .email("second@example.com")
                .passwordHash("hash2")
                .build();

        assertThatThrownBy(() -> appUserRepository.saveAndFlush(secondWithSameUsername))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void emailUniqueConstraintIsEnforced() {
        appUserRepository.saveAndFlush(AppUser.builder()
                .username("user-a")
                .email("shared@example.com")
                .passwordHash("hash1")
                .build());

        AppUser secondWithSameEmail = AppUser.builder()
                .username("user-b")
                .email("shared@example.com")
                .passwordHash("hash2")
                .build();

        assertThatThrownBy(() -> appUserRepository.saveAndFlush(secondWithSameEmail))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void userCanBeAssignedARoleThroughTheJunctionTable() {
        AppUser user = appUserRepository.saveAndFlush(AppUser.builder()
                .username("trader1")
                .email("trader1@example.com")
                .passwordHash("hash")
                .build());

        Role role = roleRepository.saveAndFlush(Role.builder()
                .name("TRADER")
                .build());

        UserRole userRole = UserRole.builder()
                .id(new UserRoleId(user.getId(), role.getId()))
                .user(user)
                .role(role)
                .build();
        userRoleRepository.saveAndFlush(userRole);

        assertThat(userRoleRepository.findByUserId(user.getId())).hasSize(1);
        assertThat(userRoleRepository.findByUserId(user.getId()).get(0).getRole().getName())
                .isEqualTo("TRADER");
    }

    @Test
    void roleNameUniqueConstraintIsEnforced() {
        roleRepository.saveAndFlush(Role.builder().name("COMPLIANCE_OFFICER").build());

        assertThatThrownBy(() ->
                roleRepository.saveAndFlush(Role.builder().name("COMPLIANCE_OFFICER").build())
        ).isInstanceOf(DataIntegrityViolationException.class);
    }
}
