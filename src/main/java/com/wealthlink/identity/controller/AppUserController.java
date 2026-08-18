package com.wealthlink.identity.controller;

import com.wealthlink.identity.dto.AppUserRequest;
import com.wealthlink.identity.dto.AppUserResponse;
import com.wealthlink.identity.service.AppUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class AppUserController {

    private final AppUserService appUserService;

    @GetMapping
    public List<AppUserResponse> findAll() {
        return appUserService.findAll();
    }

    @GetMapping("/{id}")
    public AppUserResponse findById(@PathVariable UUID id) {
        return appUserService.findById(id);
    }

    @PostMapping
    public ResponseEntity<AppUserResponse> create(@Valid @RequestBody AppUserRequest request) {
        AppUserResponse created = appUserService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/users/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public AppUserResponse update(@PathVariable UUID id, @Valid @RequestBody AppUserRequest request) {
        return appUserService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        appUserService.delete(id);
    }

    @PostMapping("/{id}/roles/{roleId}")
    public AppUserResponse assignRole(@PathVariable UUID id, @PathVariable UUID roleId) {
        return appUserService.assignRole(id, roleId);
    }

    @DeleteMapping("/{id}/roles/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeRole(@PathVariable UUID id, @PathVariable UUID roleId) {
        appUserService.removeRole(id, roleId);
    }
}
