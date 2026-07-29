package com.sportx.backend.controller;

import com.sportx.backend.dto.request.RazorpayVerifyRequest;
import com.sportx.backend.dto.response.ApiResponse;
import com.sportx.backend.dto.response.PaymentResponse;
import com.sportx.backend.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payments")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payment details for an order")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPaymentByOrderId(orderId)));
    }

    @PostMapping("/razorpay/initiate/{orderId}")
    @Operation(summary = "Initiate Razorpay payment for an order")
    public ResponseEntity<ApiResponse<PaymentResponse>> initiateRazorpay(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success("Razorpay order created", paymentService.initiateRazorpayPayment(orderId)));
    }

    @PostMapping("/razorpay/verify")
    @Operation(summary = "Verify Razorpay payment callback")
    public ResponseEntity<ApiResponse<PaymentResponse>> verifyRazorpay(@Valid @RequestBody RazorpayVerifyRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Payment verified", paymentService.verifyRazorpayPayment(request)));
    }

    @PutMapping("/cod/{orderId}/collect")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mark COD payment as collected (Admin)")
    public ResponseEntity<ApiResponse<PaymentResponse>> markCodCollected(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success("COD marked collected", paymentService.markCodCollected(orderId)));
    }
}
