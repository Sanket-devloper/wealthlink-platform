package com.wealthlink.security.config;

import com.wealthlink.security.jwt.JwtAuthenticationFilter;
import com.wealthlink.security.user.CustomUserDetailsService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CustomUserDetailsService userDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth

                        // Authentication and API documentation
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Identity administration
                        .requestMatchers("/api/v1/users/**")
                        .hasRole(Dev1RolePermissions.ADMIN)

                        .requestMatchers("/api/v1/roles/**")
                        .hasRole(Dev1RolePermissions.ADMIN)

                        // Reference data: everyone with a Dev1 role may read;
                        // only ADMIN may create/update/delete.
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/countries/**",
                                "/api/v1/currencies/**",
                                "/api/v1/markets/**")
                        .hasAnyRole(
                                Dev1RolePermissions.ADMIN,
                                Dev1RolePermissions.TRADER,
                                Dev1RolePermissions.RECONCILER,
                                Dev1RolePermissions.COMPLIANCE_OFFICER)

                        .requestMatchers(
                                "/api/v1/countries/**",
                                "/api/v1/currencies/**",
                                "/api/v1/markets/**")
                        .hasRole(Dev1RolePermissions.ADMIN)

                        // Customer
                        .requestMatchers(HttpMethod.GET, "/api/v1/customers/**")
                        .hasAnyRole(
                                Dev1RolePermissions.ADMIN,
                                Dev1RolePermissions.TRADER,
                                Dev1RolePermissions.RECONCILER,
                                Dev1RolePermissions.COMPLIANCE_OFFICER)

                        .requestMatchers(
                                "/api/v1/customers/**")
                        .hasAnyRole(
                                Dev1RolePermissions.ADMIN,
                                Dev1RolePermissions.TRADER,
                                Dev1RolePermissions.COMPLIANCE_OFFICER)

                        // Account
                        .requestMatchers(HttpMethod.GET, "/api/v1/accounts/**")
                        .hasAnyRole(
                                Dev1RolePermissions.ADMIN,
                                Dev1RolePermissions.TRADER,
                                Dev1RolePermissions.RECONCILER,
                                Dev1RolePermissions.COMPLIANCE_OFFICER)

                        .requestMatchers(
                                "/api/v1/accounts/**")
                        .hasAnyRole(
                                Dev1RolePermissions.ADMIN,
                                Dev1RolePermissions.TRADER)

                        // IMPORTANT:
                        // Do not secure every /api/v1/** endpoint here.
                        // Dev2/Dev3/Dev4 will define their own authorization.
                        .anyRequest().permitAll()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    OpenAPI jwtOpenAPI() {
        return new OpenAPI()
                .addSecurityItem(
                        new SecurityRequirement().addList("bearerAuth"));
    }

    @Bean
    AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
