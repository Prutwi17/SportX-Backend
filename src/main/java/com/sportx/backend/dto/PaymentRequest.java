package com.sportx.backend.dto;

import lombok.Data;

@Data
public class PaymentRequest {
    private Long orderId;
    private Long addressId;
    private String paymentMethod;
    private String couponCode;
    private String notes;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
}
