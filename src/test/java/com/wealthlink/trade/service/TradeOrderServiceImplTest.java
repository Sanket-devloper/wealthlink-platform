package com.wealthlink.trade.service;

import com.wealthlink.fund.entity.FundShareClass;
import com.wealthlink.fund.repository.FundShareClassRepository;
import com.wealthlink.portfolio.entity.Portfolio;
import com.wealthlink.portfolio.repository.PortfolioRepository;
import com.wealthlink.reference.entity.Currency;
import com.wealthlink.reference.repository.CurrencyRepository;
import com.wealthlink.trade.dto.CancelOrderRequest;
import com.wealthlink.trade.dto.CancelOrderResponse;
import com.wealthlink.trade.dto.CreateOrderRequest;
import com.wealthlink.trade.dto.OrderResponse;
import com.wealthlink.trade.entity.TradeOrder;
import com.wealthlink.trade.entity.TradeOrderStatus;
import com.wealthlink.trade.entity.TradeOrderType;
import com.wealthlink.trade.repository.TradeOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link TradeOrderServiceImpl}.
 * All collaborators are mocked — no Spring context or database required.
 */
@ExtendWith(MockitoExtension.class)
class TradeOrderServiceImplTest {

    @Mock TradeOrderRepository tradeOrderRepository;
    @Mock PortfolioRepository portfolioRepository;
    @Mock FundShareClassRepository fundShareClassRepository;
    @Mock CurrencyRepository currencyRepository;

    @InjectMocks TradeOrderServiceImpl service;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Currency usd(UUID id) {
        Currency c = new Currency();
        c.setId(id);
        c.setIsoCode("USD");
        return c;
    }

    private TradeOrder pendingOrder(UUID id) {
        Portfolio portfolio = new Portfolio();
        portfolio.setId(UUID.randomUUID());

        FundShareClass fsc = new FundShareClass();
        fsc.setId(UUID.randomUUID());

        return TradeOrder.builder()
                .id(id)
                .portfolio(portfolio)
                .fundShareClass(fsc)
                .orderReference("ORD-" + id)
                .orderType(TradeOrderType.BUY)
                .requestedQuantity(BigDecimal.valueOf(100))
                .status(TradeOrderStatus.PENDING)
                .currency(usd(UUID.randomUUID()))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    // -------------------------------------------------------------------------
    // getById
    // -------------------------------------------------------------------------

    @Test
    void getById_returnsMappedResponse_whenOrderExists() {
        UUID id = UUID.randomUUID();
        TradeOrder order = pendingOrder(id);
        when(tradeOrderRepository.findById(id)).thenReturn(Optional.of(order));

        OrderResponse response = service.getById(id);

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getOrderType()).isEqualTo("BUY");
        assertThat(response.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void getById_throwsRuntimeException_whenOrderNotFound() {
        UUID id = UUID.randomUUID();
        when(tradeOrderRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    // -------------------------------------------------------------------------
    // createOrder — idempotency
    // -------------------------------------------------------------------------

    @Test
    void createOrder_returnsExistingOrder_whenIdempotencyKeyExists() {
        UUID existingId = UUID.randomUUID();
        String idemKey = "order-idem-key-xyz";
        TradeOrder existing = pendingOrder(existingId);
        existing.setIdempotencyKey(idemKey);

        when(tradeOrderRepository.findByIdempotencyKey(idemKey)).thenReturn(Optional.of(existing));

        CreateOrderRequest req = new CreateOrderRequest();
        req.setIdempotencyKey(idemKey);
        req.setPortfolioId(UUID.randomUUID());
        req.setFundShareClassId(UUID.randomUUID());
        req.setOrderType("BUY");
        req.setQuantity(BigDecimal.valueOf(50));
        req.setCurrencyId(UUID.randomUUID());

        OrderResponse response = service.createOrder(req);

        assertThat(response.getId()).isEqualTo(existingId);
        verify(tradeOrderRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // createOrder — happy path
    // -------------------------------------------------------------------------

    @Test
    void createOrder_savesOrder_withPendingStatus() {
        UUID portfolioId = UUID.randomUUID();
        UUID fundShareClassId = UUID.randomUUID();
        UUID currencyId = UUID.randomUUID();

        Portfolio portfolio = new Portfolio();
        portfolio.setId(portfolioId);

        FundShareClass fsc = new FundShareClass();
        fsc.setId(fundShareClassId);

        Currency currency = usd(currencyId);

        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(portfolio));
        when(fundShareClassRepository.findById(fundShareClassId)).thenReturn(Optional.of(fsc));
        when(currencyRepository.findById(currencyId)).thenReturn(Optional.of(currency));

        ArgumentCaptor<TradeOrder> captor = ArgumentCaptor.forClass(TradeOrder.class);
        when(tradeOrderRepository.save(captor.capture())).thenAnswer(inv -> {
            TradeOrder o = inv.getArgument(0);
            o.setId(UUID.randomUUID());
            o.setCreatedAt(Instant.now());
            return o;
        });

        CreateOrderRequest req = new CreateOrderRequest();
        req.setPortfolioId(portfolioId);
        req.setFundShareClassId(fundShareClassId);
        req.setOrderType("BUY");
        req.setQuantity(BigDecimal.valueOf(200));
        req.setCurrencyId(currencyId);

        OrderResponse response = service.createOrder(req);

        TradeOrder saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(TradeOrderStatus.PENDING);
        assertThat(saved.getOrderType()).isEqualTo(TradeOrderType.BUY);
        assertThat(response.getOrderType()).isEqualTo("BUY");
    }

    // -------------------------------------------------------------------------
    // cancelOrder
    // -------------------------------------------------------------------------

    @Test
    void cancelOrder_throwsIllegalState_whenOrderIsNotPending() {
        UUID id = UUID.randomUUID();
        TradeOrder order = pendingOrder(id);
        order.setStatus(TradeOrderStatus.FILLED);

        when(tradeOrderRepository.findById(id)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.cancelOrder(id, new CancelOrderRequest()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only PENDING orders can be cancelled");
    }

    @Test
    void cancelOrder_setsStatusToCancelled_whenOrderIsPending() {
        UUID id = UUID.randomUUID();
        TradeOrder order = pendingOrder(id);

        when(tradeOrderRepository.findById(id)).thenReturn(Optional.of(order));
        when(tradeOrderRepository.save(any(TradeOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        CancelOrderResponse response = service.cancelOrder(id, new CancelOrderRequest());

        assertThat(response.getOrderId()).isEqualTo(id);
        assertThat(response.getStatus()).isEqualTo("CANCELLED");
        assertThat(order.getStatus()).isEqualTo(TradeOrderStatus.CANCELLED);
    }
}
