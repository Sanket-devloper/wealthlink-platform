package com.wealthlink.security.user;

import com.wealthlink.identity.entity.AppUser;
import com.wealthlink.identity.entity.UserRole;
import com.wealthlink.identity.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Invalid username or password"));

        var authorities = user.getRoles().stream()
                .map(UserRole::getRole)
                .map(role -> new SimpleGrantedAuthority(
                        "ROLE_" + role.getName().toUpperCase()))
                .toList();

        return User.withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .disabled(user.getStatus() != com.wealthlink.identity.entity.UserStatus.ACTIVE)
                .build();
    }
}
