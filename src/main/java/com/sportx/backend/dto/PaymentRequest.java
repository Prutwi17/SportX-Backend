package com.sportx.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequest {
    @NotNull
    private Long orderId;
    @NotBlank
    private String paymentMethod;
    private String razorpayPaymentId;
    private String razorpayOrderId;
}
