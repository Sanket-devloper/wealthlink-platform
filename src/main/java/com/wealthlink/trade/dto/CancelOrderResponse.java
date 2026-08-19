package com.wealthlink.trade.dto;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class CancelOrderResponse {
    private UUID orderId;
    private String status;
    private Instant cancelledAt;
}
