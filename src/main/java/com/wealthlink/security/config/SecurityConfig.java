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
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CustomUserDetailsService userDetailsService,
            JwtAuthenticationEntryPoint authenticationEntryPoint,
            JwtAccessDeniedHandler accessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth

                        // =====================================================
                        // Authentication and API documentation
                        // =====================================================
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()


                        // =====================================================
                        // Dev 1: Identity administration
                        // =====================================================
                        .requestMatchers("/api/v1/users/**")
                        .hasRole(Dev1RolePermissions.ADMIN)

                        .requestMatchers("/api/v1/roles/**")
                        .hasRole(Dev1RolePermissions.ADMIN)


                        // =====================================================
                        // Dev 1: Reference data
                        // GET -> all operational roles
                        // Write -> ADMIN only
                        // =====================================================
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


                        // =====================================================
                        // Dev 1: Customer
                        // GET -> all operational roles
                        // Write -> ADMIN, TRADER, COMPLIANCE_OFFICER
                        // =====================================================
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


                        // =====================================================
                        // Dev 1: Account
                        // GET -> all operational roles
                        // Write -> ADMIN, TRADER
                        // =====================================================
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


                        // =====================================================
                        // Dev 3: Portfolio, Ledger & Trade
                        // GET -> all operational roles
                        // Write -> ADMIN, TRADER
                        // =====================================================
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/portfolios/**",
                                "/api/v1/positions/**",
                                "/api/v1/trade-orders/**",
                                "/api/v1/trade-executions/**",
                                "/api/v1/settlements/**",
                                "/api/v1/journals/**",
                                "/api/v1/ledger-accounts/**"
                        ).hasAnyRole(
                                Dev1RolePermissions.ADMIN,
                                Dev1RolePermissions.TRADER,
                                Dev1RolePermissions.RECONCILER,
                                Dev1RolePermissions.COMPLIANCE_OFFICER)

                        .requestMatchers(
                                "/api/v1/portfolios/**",
                                "/api/v1/trade-orders/**",
                                "/api/v1/trade-executions/**",
                                "/api/v1/settlements/**",
                                "/api/v1/journals/**",
                                "/api/v1/ledger-accounts/**"
                        ).hasAnyRole(
                                Dev1RolePermissions.ADMIN,
                                Dev1RolePermissions.TRADER)


                        // =====================================================
                        // Dev 2: Funds & Market Data
                        // =====================================================

                        // -----------------------------------------------------
                        // Funds
                        // GET -> all operational roles
                        // Write -> ADMIN, TRADER
                        // -----------------------------------------------------
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/funds/**")
                        .hasAnyRole(
                                Dev1RolePermissions.ADMIN,
                                Dev1RolePermissions.TRADER,
                                Dev1RolePermissions.RECONCILER,
                                Dev1RolePermissions.COMPLIANCE_OFFICER)

                        .requestMatchers(
                                "/api/v1/funds/**")
                        .hasAnyRole(
                                Dev1RolePermissions.ADMIN,
                                Dev1RolePermissions.TRADER)


                        // -----------------------------------------------------
                        // Fund Share Classes
                        // -----------------------------------------------------
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/fund-share-classes/**")
                        .hasAnyRole(
                                Dev1RolePermissions.ADMIN,
                                Dev1RolePermissions.TRADER,
                                Dev1RolePermissions.RECONCILER,
                                Dev1RolePermissions.COMPLIANCE_OFFICER)

                        .requestMatchers(
                                "/api/v1/fund-share-classes/**")
                        .hasAnyRole(
                                Dev1RolePermissions.ADMIN,
                                Dev1RolePermissions.TRADER)


                        // -----------------------------------------------------
                        // Providers
                        // -----------------------------------------------------
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/providers/**")
                        .hasAnyRole(
                                Dev1RolePermissions.ADMIN,
                                Dev1RolePermissions.TRADER,
                                Dev1RolePermissions.RECONCILER,
                                Dev1RolePermissions.COMPLIANCE_OFFICER)

                        .requestMatchers(
                                "/api/v1/providers/**")
                        .hasAnyRole(
                                Dev1RolePermissions.ADMIN,
                                Dev1RolePermissions.TRADER)


                        // -----------------------------------------------------
                        // Fund Provider Mappings
                        // -----------------------------------------------------
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/fund-provider-mappings/**")
                        .hasAnyRole(
                                Dev1RolePermissions.ADMIN,
                                Dev1RolePermissions.TRADER,
                                Dev1RolePermissions.RECONCILER,
                                Dev1RolePermissions.COMPLIANCE_OFFICER)

                        .requestMatchers(
                                "/api/v1/fund-provider-mappings/**")
                        .hasAnyRole(
                                Dev1RolePermissions.ADMIN,
                                Dev1RolePermissions.TRADER)


                        // -----------------------------------------------------
                        // FX Rate Sources
                        // -----------------------------------------------------
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/fx-rate-sources/**")
                        .hasAnyRole(
                                Dev1RolePermissions.ADMIN,
                                Dev1RolePermissions.TRADER,
                                Dev1RolePermissions.RECONCILER,
                                Dev1RolePermissions.COMPLIANCE_OFFICER)

                        .requestMatchers(
                                "/api/v1/fx-rate-sources/**")
                        .hasAnyRole(
                                Dev1RolePermissions.ADMIN,
                                Dev1RolePermissions.TRADER)


                        // -----------------------------------------------------
                        // FX Rates
                        // -----------------------------------------------------
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/fx-rates/**")
                        .hasAnyRole(
                                Dev1RolePermissions.ADMIN,
                                Dev1RolePermissions.TRADER,
                                Dev1RolePermissions.RECONCILER,
                                Dev1RolePermissions.COMPLIANCE_OFFICER)

                        .requestMatchers(
                                "/api/v1/fx-rates/**")
                        .hasAnyRole(
                                Dev1RolePermissions.ADMIN,
                                Dev1RolePermissions.TRADER)


                        // -----------------------------------------------------
                        // Fund Prices
                        // -----------------------------------------------------
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/fund-prices/**")
                        .hasAnyRole(
                                Dev1RolePermissions.ADMIN,
                                Dev1RolePermissions.TRADER,
                                Dev1RolePermissions.RECONCILER,
                                Dev1RolePermissions.COMPLIANCE_OFFICER)

                        .requestMatchers(
                                "/api/v1/fund-prices/**")
                        .hasAnyRole(
                                Dev1RolePermissions.ADMIN,
                                Dev1RolePermissions.TRADER)


                        // -----------------------------------------------------
                        // Import Jobs
                        // -----------------------------------------------------
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/import-jobs/**")
                        .hasAnyRole(
                                Dev1RolePermissions.ADMIN,
                                Dev1RolePermissions.TRADER,
                                Dev1RolePermissions.RECONCILER,
                                Dev1RolePermissions.COMPLIANCE_OFFICER)

                        .requestMatchers(
                                "/api/v1/import-jobs/**")
                        .hasAnyRole(
                                Dev1RolePermissions.ADMIN,
                                Dev1RolePermissions.TRADER)


                        // -----------------------------------------------------
                        // Import Batches
                        // -----------------------------------------------------
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/import-batches/**")
                        .hasAnyRole(
                                Dev1RolePermissions.ADMIN,
                                Dev1RolePermissions.TRADER,
                                Dev1RolePermissions.RECONCILER,
                                Dev1RolePermissions.COMPLIANCE_OFFICER)

                        .requestMatchers(
                                "/api/v1/import-batches/**")
                        .hasAnyRole(
                                Dev1RolePermissions.ADMIN,
                                Dev1RolePermissions.TRADER)


                        // -----------------------------------------------------
                        // Import Items
                        // -----------------------------------------------------
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/import-items/**")
                        .hasAnyRole(
                                Dev1RolePermissions.ADMIN,
                                Dev1RolePermissions.TRADER,
                                Dev1RolePermissions.RECONCILER,
                                Dev1RolePermissions.COMPLIANCE_OFFICER)

                        .requestMatchers(
                                "/api/v1/import-items/**")
                        .hasAnyRole(
                                Dev1RolePermissions.ADMIN,
                                Dev1RolePermissions.TRADER)


                        // =====================================================
                        // IMPORTANT
                        // Do not secure every /api/v1/** endpoint here.
                        // Dev2/Dev3/Dev4 define their own authorization rules.
                        // =====================================================
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