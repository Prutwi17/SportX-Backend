package com.sportx.backend.repository;

import com.sportx.backend.entity.Payment;
import com.sportx.backend.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);
    long countByStatus(PaymentStatus status);
}
