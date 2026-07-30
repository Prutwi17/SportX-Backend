package com.sportx.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponRequest {
    @NotBlank
    private String code;
    @NotNull @Positive
    private BigDecimal discountPercent;
    private BigDecimal maxDiscount;
    private BigDecimal minOrderAmount;
    private int usageLimit;
    private boolean active;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
}
