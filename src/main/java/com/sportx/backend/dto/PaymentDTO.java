package com.sportx.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentDTO {
    private Long id;
    private Long orderId;
    private String orderNumber;
    private String paymentMethod;
    private String status;
    private BigDecimal amount;
    private String currency = "INR";
    private String transactionId;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String keyId;
    private boolean verified;
    private String message;
    private LocalDateTime paidAt;
}
