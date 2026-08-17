package com.wealthlink.identity.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Entity-level unit test (Day 1 deliverable) - no database involved, just
 * verifies the @PrePersist / @PreUpdate defaulting logic on the entity
 * itself. Package-private on purpose so it can call onCreate()/onUpdate()
 * directly, same as AppUser.java.
 */
class AppUserTest {

    @Test
    void onCreateDefaultsStatusToActiveWhenNotSet() {
        AppUser user = AppUser.builder()
                .username("test-user")
                .email("test@example.com")
                .passwordHash("hash")
                .build();

        user.onCreate();

        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
        assertThat(user.getCreatedAt()).isEqualTo(user.getUpdatedAt());
    }

    @Test
    void onCreateDoesNotOverrideAnExplicitStatus() {
        AppUser user = AppUser.builder()
                .username("test-user")
                .email("test@example.com")
                .passwordHash("hash")
                .status(UserStatus.LOCKED)
                .build();

        user.onCreate();

        assertThat(user.getStatus()).isEqualTo(UserStatus.LOCKED);
    }

    @Test
    void onUpdateBumpsUpdatedAtOnly() throws InterruptedException {
        AppUser user = AppUser.builder()
                .username("test-user")
                .email("test@example.com")
                .passwordHash("hash")
                .build();
        user.onCreate();

        var createdAt = user.getCreatedAt();
        Thread.sleep(5); // ensure a measurable time gap
        user.onUpdate();

        assertThat(user.getCreatedAt()).isEqualTo(createdAt); // untouched
        assertThat(user.getUpdatedAt()).isAfter(createdAt);
    }
}
