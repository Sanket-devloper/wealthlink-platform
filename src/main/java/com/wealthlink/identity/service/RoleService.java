package com.wealthlink.identity.service;

import com.wealthlink.identity.dto.RoleRequest;
import com.wealthlink.identity.dto.RoleResponse;

import java.util.List;
import java.util.UUID;

public interface RoleService {
    List<RoleResponse> findAll();
    RoleResponse create(RoleRequest request);
    void delete(UUID id);
}