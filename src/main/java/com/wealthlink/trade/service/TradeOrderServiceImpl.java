package com.wealthlink.trade.service;

import com.wealthlink.common.exception.ResourceNotFoundException;

import com.wealthlink.fund.repository.FundShareClassRepository;
import com.wealthlink.portfolio.repository.PortfolioRepository;
import com.wealthlink.reference.repository.CurrencyRepository;
import com.wealthlink.trade.dto.CancelOrderRequest;
import com.wealthlink.trade.dto.CancelOrderResponse;
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
public class TradeOrderServiceImpl implements TradeOrderService {

    private final TradeOrderRepository tradeOrderRepository;
    private final PortfolioRepository portfolioRepository;
    private final FundShareClassRepository fundShareClassRepository;
    private final CurrencyRepository currencyRepository;

    @Transactional(readOnly = true)
    public List<OrderResponse> getAll() {
        return tradeOrderRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponse getById(UUID id) {
        TradeOrder order = tradeOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TradeOrder", id));
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
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", request.getPortfolioId())));
        order.setFundShareClass(fundShareClassRepository.findById(request.getFundShareClassId())
                .orElseThrow(() -> new ResourceNotFoundException("FundShareClass", request.getFundShareClassId())));
        order.setCurrency(currencyRepository.findById(request.getCurrencyId())
                .orElseThrow(() -> new ResourceNotFoundException("Currency", request.getCurrencyId())));
        
        order.setOrderType(TradeOrderType.valueOf(request.getOrderType()));
        order.setRequestedQuantity(request.getQuantity());
        order.setLimitPrice(request.getLimitPrice());
        order.setIdempotencyKey(request.getIdempotencyKey());
        order.setOrderReference(UUID.randomUUID().toString());
        order.setStatus(TradeOrderStatus.PENDING);
        
        TradeOrder savedOrder = tradeOrderRepository.save(order);
        return mapToResponse(savedOrder);
    }

    @Override
    @Transactional
    public CancelOrderResponse cancelOrder(UUID id, CancelOrderRequest request) {
        TradeOrder order = tradeOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TradeOrder", id));
        
        if (order.getStatus() != TradeOrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be cancelled.");
        }
        
        order.setStatus(TradeOrderStatus.CANCELLED);
        TradeOrder saved = tradeOrderRepository.save(order);
        
        return CancelOrderResponse.builder()
                .orderId(saved.getId())
                .status(saved.getStatus().name())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersForPortfolio(UUID portfolioId) {
        return tradeOrderRepository.findByPortfolioId(portfolioId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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
