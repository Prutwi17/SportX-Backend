package com.sportx.backend.service;

import com.sportx.backend.dto.request.RazorpayVerifyRequest;
import com.sportx.backend.dto.response.PaymentResponse;

public interface PaymentService {

    PaymentResponse getPaymentByOrderId(Long orderId);

    PaymentResponse initiateRazorpayPayment(Long orderId);

    PaymentResponse verifyRazorpayPayment(RazorpayVerifyRequest request);

    PaymentResponse markCodCollected(Long orderId);
}
