package com.sportx.backend.service.impl;

import com.sportx.backend.dto.PaymentDTO;
import com.sportx.backend.dto.PaymentRequest;
import com.sportx.backend.entity.Order;
import com.sportx.backend.entity.Payment;
import com.sportx.backend.enums.PaymentMethod;
import com.sportx.backend.enums.PaymentStatus;
import com.sportx.backend.exception.BadRequestException;
import com.sportx.backend.exception.ResourceNotFoundException;
import com.sportx.backend.repository.OrderRepository;
import com.sportx.backend.repository.PaymentRepository;
import com.sportx.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public PaymentDTO processPayment(PaymentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (paymentRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new BadRequestException("Payment already processed for this order");
        }

        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()))
                .amount(order.getTotal())
                .status(PaymentStatus.PAID)
                .razorpayOrderId(request.getRazorpayOrderId())
                .razorpayPaymentId(request.getRazorpayPaymentId())
                .transactionId("TXN-" + System.currentTimeMillis())
                .paidAt(LocalDateTime.now())
                .build();

        payment = paymentRepository.save(payment);
        return mapToDTO(payment);
    }

    @Override
    public PaymentDTO getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        return mapToDTO(payment);
    }

    private PaymentDTO mapToDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());
        dto.setOrderId(payment.getOrder().getId());
        dto.setPaymentMethod(payment.getPaymentMethod().name());
        dto.setStatus(payment.getStatus().name());
        dto.setAmount(payment.getAmount());
        dto.setTransactionId(payment.getTransactionId());
        dto.setPaidAt(payment.getPaidAt());
        return dto;
    }
}
