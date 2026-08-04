package com.sportx.backend.service;

import com.sportx.backend.dto.PaymentDTO;
import com.sportx.backend.dto.PaymentRequest;

public interface PaymentService {
    PaymentDTO processPayment(PaymentRequest request);
    PaymentDTO createRazorpayOrder(Long userId, PaymentRequest request);
    PaymentDTO verifyPayment(Long userId, PaymentRequest request);
    PaymentDTO getPaymentByOrderId(Long orderId);
}
