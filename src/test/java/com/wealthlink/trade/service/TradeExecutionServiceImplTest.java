package com.wealthlink.trade.service;

import com.wealthlink.common.exception.ResourceNotFoundException;
import com.wealthlink.reference.entity.Currency;
import com.wealthlink.reference.repository.CurrencyRepository;
import com.wealthlink.trade.dto.ExecutionResponse;
import com.wealthlink.trade.dto.RecordExecutionRequest;
import com.wealthlink.trade.entity.TradeExecution;
import com.wealthlink.trade.entity.TradeExecutionStatus;
import com.wealthlink.trade.entity.TradeOrder;
import com.wealthlink.trade.entity.TradeOrderStatus;
import com.wealthlink.trade.entity.TradeOrderType;
import com.wealthlink.trade.repository.TradeExecutionRepository;
import com.wealthlink.trade.repository.TradeOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link TradeExecutionServiceImpl}.
 * All collaborators are mocked — no Spring context or database required.
 */
@ExtendWith(MockitoExtension.class)
class TradeExecutionServiceImplTest {

    @Mock TradeExecutionRepository tradeExecutionRepository;
    @Mock TradeOrderRepository tradeOrderRepository;
    @Mock CurrencyRepository currencyRepository;

    @InjectMocks TradeExecutionServiceImpl service;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Currency usd(UUID id) {
        Currency c = new Currency();
        c.setId(id);
        c.setIsoCode("USD");
        return c;
    }

    private TradeOrder order(UUID id, BigDecimal requestedQty) {
        TradeOrder o = new TradeOrder();
        o.setId(id);
        o.setOrderType(TradeOrderType.BUY);
        o.setRequestedQuantity(requestedQty);
        o.setStatus(TradeOrderStatus.PENDING);
        return o;
    }

    private TradeExecution execution(UUID id, UUID orderId, BigDecimal qty) {
        TradeOrder o = order(orderId, qty);
        return TradeExecution.builder()
                .id(id)
                .tradeOrder(o)
                .executionReference("EXEC-" + id)
                .status(TradeExecutionStatus.CONFIRMED)
                .executedQuantity(qty)
                .executionPrice(BigDecimal.valueOf(10))
                .grossAmount(qty.multiply(BigDecimal.valueOf(10)))
                .netAmount(qty.multiply(BigDecimal.valueOf(10)))
                .currency(usd(UUID.randomUUID()))
                .executedAt(Instant.now())
                .build();
    }

    private RecordExecutionRequest execRequest(UUID currencyId, BigDecimal qty) {
        RecordExecutionRequest req = new RecordExecutionRequest();
        req.setTradeDate(LocalDate.now());
        req.setExecutedQuantity(qty);
        req.setExecutedPrice(BigDecimal.valueOf(10));
        req.setGrossAmount(qty.multiply(BigDecimal.valueOf(10)));
        req.setNetAmount(qty.multiply(BigDecimal.valueOf(10)));
        req.setCurrencyId(currencyId);
        return req;
    }

    // -------------------------------------------------------------------------
    // getById
    // -------------------------------------------------------------------------

    @Test
    void getById_returnsMappedResponse_whenExecutionExists() {
        UUID id = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        TradeExecution exec = execution(id, orderId, BigDecimal.valueOf(50));
        when(tradeExecutionRepository.findById(id)).thenReturn(Optional.of(exec));

        ExecutionResponse response = service.getById(id);

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getExecutionStatus()).isEqualTo("CONFIRMED");
        assertThat(response.getExecutedQuantity()).isEqualByComparingTo(BigDecimal.valueOf(50));
    }

    @Test
    void getById_throwsResourceNotFoundException_whenExecutionNotFound() {
        UUID id = UUID.randomUUID();
        when(tradeExecutionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    // -------------------------------------------------------------------------
    // recordExecution — saves with CONFIRMED status
    // -------------------------------------------------------------------------

    @Test
    void recordExecution_savesExecution_withConfirmedStatus() {
        UUID orderId = UUID.randomUUID();
        UUID currencyId = UUID.randomUUID();
        BigDecimal qty = BigDecimal.valueOf(100);

        TradeOrder tradeOrder = order(orderId, BigDecimal.valueOf(200)); // not yet fully filled
        when(tradeOrderRepository.findById(orderId)).thenReturn(Optional.of(tradeOrder));
        when(currencyRepository.findById(currencyId)).thenReturn(Optional.of(usd(currencyId)));

        ArgumentCaptor<TradeExecution> execCaptor = ArgumentCaptor.forClass(TradeExecution.class);
        when(tradeExecutionRepository.save(execCaptor.capture())).thenAnswer(inv -> {
            TradeExecution e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        // Return only the new execution when queried for total quantity
        when(tradeExecutionRepository.findByTradeOrderId(orderId))
                .thenReturn(Collections.singletonList(execution(UUID.randomUUID(), orderId, qty)));

        when(tradeOrderRepository.save(any(TradeOrder.class))).thenReturn(tradeOrder);

        RecordExecutionRequest req = execRequest(currencyId, qty);
        ExecutionResponse response = service.recordExecution(orderId, req);

        TradeExecution savedExec = execCaptor.getValue();
        assertThat(savedExec.getStatus()).isEqualTo(TradeExecutionStatus.CONFIRMED);
        assertThat(savedExec.getExecutedQuantity()).isEqualByComparingTo(qty);
    }

    // -------------------------------------------------------------------------
    // recordExecution — order fill status transitions
    // -------------------------------------------------------------------------

    @Test
    void recordExecution_marksOrderFilled_whenTotalExecutedEqualsRequested() {
        UUID orderId = UUID.randomUUID();
        UUID currencyId = UUID.randomUUID();
        BigDecimal requestedQty = BigDecimal.valueOf(100);
        BigDecimal execQty = BigDecimal.valueOf(100);

        TradeOrder tradeOrder = order(orderId, requestedQty);
        when(tradeOrderRepository.findById(orderId)).thenReturn(Optional.of(tradeOrder));
        when(currencyRepository.findById(currencyId)).thenReturn(Optional.of(usd(currencyId)));
        when(tradeExecutionRepository.save(any(TradeExecution.class))).thenAnswer(inv -> {
            TradeExecution e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        // Total equals requested → FILLED
        TradeExecution fullyFilled = execution(UUID.randomUUID(), orderId, execQty);
        when(tradeExecutionRepository.findByTradeOrderId(orderId))
                .thenReturn(Collections.singletonList(fullyFilled));

        ArgumentCaptor<TradeOrder> orderCaptor = ArgumentCaptor.forClass(TradeOrder.class);
        when(tradeOrderRepository.save(orderCaptor.capture())).thenReturn(tradeOrder);

        service.recordExecution(orderId, execRequest(currencyId, execQty));

        TradeOrder savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getStatus()).isEqualTo(TradeOrderStatus.FILLED);
    }

    @Test
    void recordExecution_marksOrderPartiallyFilled_whenTotalExecutedLessThanRequested() {
        UUID orderId = UUID.randomUUID();
        UUID currencyId = UUID.randomUUID();
        BigDecimal requestedQty = BigDecimal.valueOf(200);
        BigDecimal execQty = BigDecimal.valueOf(80); // partial

        TradeOrder tradeOrder = order(orderId, requestedQty);
        when(tradeOrderRepository.findById(orderId)).thenReturn(Optional.of(tradeOrder));
        when(currencyRepository.findById(currencyId)).thenReturn(Optional.of(usd(currencyId)));
        when(tradeExecutionRepository.save(any(TradeExecution.class))).thenAnswer(inv -> {
            TradeExecution e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        TradeExecution partial = execution(UUID.randomUUID(), orderId, execQty);
        when(tradeExecutionRepository.findByTradeOrderId(orderId))
                .thenReturn(Collections.singletonList(partial));

        ArgumentCaptor<TradeOrder> orderCaptor = ArgumentCaptor.forClass(TradeOrder.class);
        when(tradeOrderRepository.save(orderCaptor.capture())).thenReturn(tradeOrder);

        service.recordExecution(orderId, execRequest(currencyId, execQty));

        TradeOrder savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getStatus()).isEqualTo(TradeOrderStatus.PARTIALLY_FILLED);
    }

    @Test
    void recordExecution_marksOrderFilled_whenMultipleExecutionsReachRequestedTotal() {
        UUID orderId = UUID.randomUUID();
        UUID currencyId = UUID.randomUUID();
        BigDecimal requestedQty = BigDecimal.valueOf(100);

        TradeOrder tradeOrder = order(orderId, requestedQty);
        when(tradeOrderRepository.findById(orderId)).thenReturn(Optional.of(tradeOrder));
        when(currencyRepository.findById(currencyId)).thenReturn(Optional.of(usd(currencyId)));
        when(tradeExecutionRepository.save(any(TradeExecution.class))).thenAnswer(inv -> {
            TradeExecution e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        // Two prior executions of 50 each = 100 total → FILLED
        TradeExecution exec1 = execution(UUID.randomUUID(), orderId, BigDecimal.valueOf(50));
        TradeExecution exec2 = execution(UUID.randomUUID(), orderId, BigDecimal.valueOf(50));
        when(tradeExecutionRepository.findByTradeOrderId(orderId))
                .thenReturn(Arrays.asList(exec1, exec2));

        ArgumentCaptor<TradeOrder> orderCaptor = ArgumentCaptor.forClass(TradeOrder.class);
        when(tradeOrderRepository.save(orderCaptor.capture())).thenReturn(tradeOrder);

        service.recordExecution(orderId, execRequest(currencyId, BigDecimal.valueOf(50)));

        assertThat(orderCaptor.getValue().getStatus()).isEqualTo(TradeOrderStatus.FILLED);
    }
}
