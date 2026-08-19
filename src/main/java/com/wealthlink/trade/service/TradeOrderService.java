package com.wealthlink.trade.service;

import com.wealthlink.fund.repository.FundShareClassRepository;
import com.wealthlink.portfolio.repository.PortfolioRepository;
import com.wealthlink.reference.repository.CurrencyRepository;
import com.wealthlink.trade.dto.CreateOrderRequest;
import com.wealthlink.trade.dto.OrderResponse;
import com.wealthlink.trade.entity.TradeOrder;
import com.wealthlink.trade.entity.TradeOrderStatus;
import com.wealthlink.trade.entity.TradeOrderType;
import com.wealthlink.trade.repository.TradeOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TradeOrderService {

    private final TradeOrderRepository tradeOrderRepository;
    private final PortfolioRepository portfolioRepository;
    private final FundShareClassRepository fundShareClassRepository;
    private final CurrencyRepository currencyRepository;

    public List<OrderResponse> getAll() {
        return tradeOrderRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public OrderResponse getById(UUID id) {
        TradeOrder order = tradeOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return mapToResponse(order);
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        if (request.getIdempotencyKey() != null) {
            Optional<TradeOrder> existingOrder = tradeOrderRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existingOrder.isPresent()) {
                return mapToResponse(existingOrder.get());
            }
        }

        TradeOrder order = new TradeOrder();
        order.setPortfolio(portfolioRepository.findById(request.getPortfolioId())
                .orElseThrow(() -> new RuntimeException("Portfolio not found")));
        order.setFundShareClass(fundShareClassRepository.findById(request.getFundShareClassId())
                .orElseThrow(() -> new RuntimeException("FundShareClass not found")));
        order.setCurrency(currencyRepository.findById(request.getCurrencyId())
                .orElseThrow(() -> new RuntimeException("Currency not found")));
        
        order.setOrderType(TradeOrderType.valueOf(request.getOrderType()));
        order.setRequestedQuantity(request.getQuantity());
        order.setLimitPrice(request.getLimitPrice());
        order.setIdempotencyKey(request.getIdempotencyKey());
        order.setOrderReference(UUID.randomUUID().toString());
        order.setStatus(TradeOrderStatus.PENDING);
        
        TradeOrder savedOrder = tradeOrderRepository.save(order);
        return mapToResponse(savedOrder);
    }

    private OrderResponse mapToResponse(TradeOrder order) {
        return OrderResponse.builder()
                .id(order.getId())
                .portfolioId(order.getPortfolio().getId())
                .fundShareClassId(order.getFundShareClass().getId())
                .orderType(order.getOrderType().name())
                .quantity(order.getRequestedQuantity())
                .limitPrice(order.getLimitPrice())
                .currency(order.getCurrency().getIsoCode())
                .status(order.getStatus().name())
                .idempotencyKey(order.getIdempotencyKey())
                .createdAt(order.getCreatedAt() != null ? order.getCreatedAt() : Instant.now())
                .build();
    }
}
