package com.sportx.backend.service.impl;

import com.sportx.backend.dto.request.RazorpayVerifyRequest;
import com.sportx.backend.dto.response.PaymentResponse;
import com.sportx.backend.entity.Order;
import com.sportx.backend.entity.Payment;
import com.sportx.backend.entity.User;
import com.sportx.backend.enums.OrderStatus;
import com.sportx.backend.enums.PaymentMethod;
import com.sportx.backend.enums.PaymentStatus;
import com.sportx.backend.exception.BadRequestException;
import com.sportx.backend.exception.ResourceNotFoundException;
import com.sportx.backend.mapper.EntityMapper;
import com.sportx.backend.repository.OrderRepository;
import com.sportx.backend.repository.PaymentRepository;
import com.sportx.backend.repository.UserRepository;
import com.sportx.backend.service.PaymentService;
import com.sportx.backend.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        validateOrderAccess(payment.getOrder());
        return EntityMapper.toPaymentResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse initiateRazorpayPayment(Long orderId) {
        Order order = findOrder(orderId);
        validateOrderAccess(order);

        if (order.getPaymentMethod() != PaymentMethod.RAZORPAY) {
            throw new BadRequestException("Order is not configured for Razorpay");
        }

        Payment payment = order.getPayment();
        if (payment == null) {
            throw new ResourceNotFoundException("Payment record not found");
        }

        // Placeholder Razorpay order ID — replace with actual Razorpay SDK integration
        String razorpayOrderId = "order_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
        payment.setRazorpayOrderId(razorpayOrderId);
        payment.setStatus(PaymentStatus.PENDING);

        return EntityMapper.toPaymentResponse(paymentRepository.save(payment));
    }

    @Override
    @Transactional
    public PaymentResponse verifyRazorpayPayment(RazorpayVerifyRequest request) {
        Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for Razorpay order"));

        validateOrderAccess(payment.getOrder());

        // Placeholder verification — in production, verify HMAC signature with Razorpay secret
        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(request.getRazorpaySignature());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId(request.getRazorpayPaymentId());

        Order order = payment.getOrder();
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
        }

        return EntityMapper.toPaymentResponse(paymentRepository.save(payment));
    }

    @Override
    @Transactional
    public PaymentResponse markCodCollected(Long orderId) {
        Order order = findOrder(orderId);
        Payment payment = order.getPayment();

        if (payment == null || payment.getMethod() != PaymentMethod.COD) {
            throw new BadRequestException("Order is not a COD order");
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionId("COD-" + order.getOrderNumber());

        if (order.getStatus() == OrderStatus.DELIVERED) {
            orderRepository.save(order);
        }

        return EntityMapper.toPaymentResponse(paymentRepository.save(payment));
    }

    private Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    private void validateOrderAccess(Order order) {
        User user = userRepository.findByEmail(AuthUtil.currentUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!order.getUser().getId().equals(user.getId()) && !EntityMapper.isAdmin(user)) {
            throw new BadRequestException("Access denied to this payment");
        }
    }
}
