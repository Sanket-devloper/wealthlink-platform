package com.wealthlink.trade.service;

import com.wealthlink.trade.dto.CancelOrderRequest;
import com.wealthlink.trade.dto.CancelOrderResponse;
import com.wealthlink.trade.dto.CreateOrderRequest;
import com.wealthlink.trade.dto.OrderResponse;

import java.util.List;
import java.util.UUID;

public interface TradeOrderService {

    List<OrderResponse> getAll();

    OrderResponse getById(UUID id);

    OrderResponse createOrder(CreateOrderRequest request);

    CancelOrderResponse cancelOrder(UUID id, CancelOrderRequest request);

    List<OrderResponse> getOrdersForPortfolio(UUID portfolioId);
}
