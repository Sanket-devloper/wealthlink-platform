package com.wealthlink.identity.service.impl;

import com.wealthlink.identity.service.RoleService;

import com.wealthlink.common.exception.ResourceNotFoundException;
import com.wealthlink.identity.dto.RoleRequest;
import com.wealthlink.identity.dto.RoleResponse;
import com.wealthlink.identity.entity.Role;
import com.wealthlink.identity.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public List<RoleResponse> findAll() {
        return roleRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public RoleResponse create(RoleRequest request) {
        Role role = Role.builder().name(request.name()).build();
        return toResponse(roleRepository.save(role));
    }

    @Override
    public void delete(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));
        roleRepository.delete(role);
    }

    private RoleResponse toResponse(Role r) {
        return new RoleResponse(r.getId(), r.getName());
    }
}
