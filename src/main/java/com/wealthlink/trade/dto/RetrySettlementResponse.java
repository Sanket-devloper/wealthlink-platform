package com.wealthlink.trade.dto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class RetrySettlementResponse {
    private UUID settlementId;
    private String settlementStatus;
    private Integer retryCount;
}
