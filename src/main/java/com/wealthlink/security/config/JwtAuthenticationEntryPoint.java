package com.wealthlink.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wealthlink.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handles requests with NO token (or an invalid/expired one) hitting a
 * protected endpoint. Without this, Spring Security's default behavior
 * returns a bare 403 with no body - misleading, since 403 should mean
 * "you're authenticated but not allowed", not "you never logged in".
 * This runs at the filter-chain level, before the request reaches any
 * @RestController, so GlobalExceptionHandler alone can't cover this case.
 * <p>
 * Uses the Spring-managed ObjectMapper bean (not `new ObjectMapper()`) -
 * Spring Boot auto-configures that bean with the JavaTimeModule already
 * registered, which is required to serialize ErrorResponse.timestamp
 * (a java.time.Instant). A manually-created ObjectMapper does NOT have
 * this module and throws InvalidDefinitionException on any Instant field.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                          AuthenticationException authException) throws IOException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");

        ErrorResponse body = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Authentication is required to access this resource");

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
