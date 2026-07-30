package com.sportx.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponDTO {
    private Long id;
    private String code;
    private BigDecimal discountPercent;
    private BigDecimal maxDiscount;
    private BigDecimal minOrderAmount;
    private int usageLimit;
    private int usedCount;
    private boolean active;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
}
