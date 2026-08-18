package com.wealthlink.identity.service;

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
public class RoleService {

    private final RoleRepository roleRepository;

    public List<RoleResponse> findAll() {
        return roleRepository.findAll().stream().map(this::toResponse).toList();
    }

    public RoleResponse create(RoleRequest request) {
        Role role = Role.builder().name(request.name()).build();
        return toResponse(roleRepository.save(role));
    }

    public void delete(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));
        roleRepository.delete(role);
    }

    private RoleResponse toResponse(Role r) {
        return new RoleResponse(r.getId(), r.getName());
    }
}
