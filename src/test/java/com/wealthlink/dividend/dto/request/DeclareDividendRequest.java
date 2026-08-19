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

    @NotNull(message = "Declaration date is required")
    private LocalDate declarationDate;

    @NotNull(message = "Ex-dividend date is required")
    private LocalDate exDate;

    @NotNull(message = "Record date is required")
    private LocalDate recordDate;

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;

    @NotNull(message = "Rate per share is required")
    @DecimalMin(value = "0.00000001", message = "Rate must be positive")
    @Digits(integer = 10, fraction = 8)
    private BigDecimal ratePerShare;

    @NotBlank(message = "Dividend currency is required")
    private String currency;

    @NotBlank(message = "Dividend type is required (e.g., CASH, REINVESTMENT, SPECIAL)")
    private String dividendType;
}