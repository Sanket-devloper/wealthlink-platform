package com.wealthlink.identity.service;

import com.wealthlink.identity.dto.AppUserRequest;
import com.wealthlink.identity.dto.AppUserResponse;

import java.util.List;
import java.util.UUID;

public interface AppUserService {
    List<AppUserResponse> findAll();
    AppUserResponse findById(UUID id);
    AppUserResponse create(AppUserRequest request);
    AppUserResponse update(UUID id, AppUserRequest request);
    void delete(UUID id);
    AppUserResponse assignRole(UUID userId, UUID roleId);
    void removeRole(UUID userId, UUID roleId);
}