package com.wealthlink.dividend.dto.response;

import com.wealthlink.dividend.entity.DividendEventStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DividendEventResponse {
    private UUID id;
    private UUID fundShareClassId;
    private UUID currencyId;
    private String currencyCode;
    private LocalDate exDate;
    private LocalDate recordDate;
    private LocalDate paymentDate;
    private BigDecimal dividendPerUnit;
    private DividendEventStatus status;
    private String source;
    private UUID correctedFromEventId;
    private Instant createdAt;
}