package com.wealthlink.trade.controller;

import com.wealthlink.security.jwt.JwtService;
import com.wealthlink.security.user.CustomUserDetailsService;
import com.wealthlink.trade.service.SettlementService;
import com.wealthlink.trade.service.TradeExecutionService;
import com.wealthlink.trade.service.TradeOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TradeSecurityTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    // Mocking the underlying services so controller doesn't crash on success cases
    @MockBean
    private TradeOrderService tradeOrderService;

    @MockBean
    private TradeExecutionService tradeExecutionService;

    @MockBean
    private SettlementService settlementService;

    private UserDetails traderUser;
    private UserDetails reconcilerUser;

    @BeforeEach
    void setUp() {
        traderUser = new User("trader", "password", List.of(new SimpleGrantedAuthority("ROLE_TRADER")));
        reconcilerUser = new User("reconciler", "password", List.of(new SimpleGrantedAuthority("ROLE_RECONCILER")));

        when(userDetailsService.loadUserByUsername("trader")).thenReturn(traderUser);
        when(userDetailsService.loadUserByUsername("reconciler")).thenReturn(reconcilerUser);
    }

    private HttpHeaders getHeadersForUser(UserDetails userDetails) {
        String token = jwtService.generateToken(userDetails);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return headers;
    }

    @Test
    void accessWithoutToken_ReturnsUnauthorized() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/trade-orders", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void accessWithTraderRole_GetRequest_ReturnsOk() {
        HttpHeaders headers = getHeadersForUser(traderUser);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange("/api/v1/trade-orders", HttpMethod.GET, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void accessWithReconcilerRole_GetRequest_ReturnsOk() {
        HttpHeaders headers = getHeadersForUser(reconcilerUser);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange("/api/v1/trade-orders", HttpMethod.GET, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void accessWithTraderRole_PostRequest_ReturnsOk() {
        HttpHeaders headers = getHeadersForUser(traderUser);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>("{}", headers); // dummy payload

        // Trade Order creation returns 201 Created or 200 OK depending on implementation
        ResponseEntity<String> response = restTemplate.exchange("/api/v1/trade-orders", HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void accessWithReconcilerRole_PostRequest_ReturnsForbidden() {
        HttpHeaders headers = getHeadersForUser(reconcilerUser);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>("{}", headers);

        ResponseEntity<String> response = restTemplate.exchange("/api/v1/trade-orders", HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
