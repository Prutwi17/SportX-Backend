package com.sportx.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentDTO {
    private Long id;
    private Long orderId;
    private String paymentMethod;
    private String status;
    private BigDecimal amount;
    private String transactionId;
    private LocalDateTime paidAt;
}
