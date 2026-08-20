package com.wealthlink.dividend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeclareDividendRequest {

    @NotNull(message = "Fund share class ID is required")
    private UUID fundShareClassId;

    @NotNull(message = "Currency ID is required")
    private UUID currencyId;

    @NotNull(message = "Ex-dividend date is required")
    private LocalDate exDate;

    @NotNull(message = "Record date is required")
    private LocalDate recordDate;

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;

    @NotNull(message = "Dividend per unit is required")
    @DecimalMin(value = "0.00000001", message = "Dividend per unit must be greater than zero")
    @Digits(integer = 10, fraction = 8)
    private BigDecimal dividendPerUnit;

    @NotBlank(message = "Source is required (e.g. MANUAL, MARKET_DATA)")
    private String source;

    private UUID correctedFromEventId;
}