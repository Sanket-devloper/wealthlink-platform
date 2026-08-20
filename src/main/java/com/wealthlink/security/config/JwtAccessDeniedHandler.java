package com.wealthlink.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wealthlink.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handles requests with a VALID token that lacks the required role (e.g.
 * a TRADER hitting an ADMIN-only endpoint like /api/v1/users). Same
 * reasoning as JwtAuthenticationEntryPoint - this is filter-chain level,
 * so it needs its own handler rather than relying on GlobalExceptionHandler.
 * <p>
 * Also uses the Spring-managed ObjectMapper bean, same fix as
 * JwtAuthenticationEntryPoint - see its Javadoc for why this matters.
 */
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                        AccessDeniedException accessDeniedException) throws IOException {

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json");

        ErrorResponse body = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                "You do not have permission to perform this action");

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
