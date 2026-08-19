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
    private LocalDate declarationDate;
    private LocalDate exDate;
    private LocalDate recordDate;
    private LocalDate paymentDate;
    private BigDecimal ratePerShare;
    private String currency;
    private String dividendType;
    private DividendEventStatus status;
    private BigDecimal totalGrossAmount;
    private BigDecimal totalTaxAmount;
    private BigDecimal totalNetAmount;
    private Instant createdAt;
}