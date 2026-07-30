package com.sportx.backend.repository;

import com.sportx.backend.entity.Order;
import com.sportx.backend.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    Page<Order> findByUserId(Long userId, Pageable pageable);
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    long countByStatus(OrderStatus status);
}
