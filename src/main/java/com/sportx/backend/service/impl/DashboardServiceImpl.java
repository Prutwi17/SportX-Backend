package com.sportx.backend.service.impl;

import com.sportx.backend.dto.DashboardDTO;
import com.sportx.backend.dto.OrderDTO;
import com.sportx.backend.enums.OrderStatus;
import com.sportx.backend.repository.OrderRepository;
import com.sportx.backend.repository.ProductRepository;
import com.sportx.backend.repository.UserRepository;
import com.sportx.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final int LOW_STOCK_THRESHOLD = 10;
    private static final int RECENT_ORDERS_LIMIT = 10;

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Override
    public DashboardDTO getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalProducts = productRepository.count();
        long totalOrders = orderRepository.count();
        long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);
        long lowStockProducts = productRepository
                .findByStockQuantityLessThanAndActiveTrue(LOW_STOCK_THRESHOLD).size();

        BigDecimal totalRevenue = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .map(o -> o.getTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var recentOrders = orderRepository.findAll(
                PageRequest.of(0, RECENT_ORDERS_LIMIT)).getContent().stream()
                .map(order -> {
                    OrderDTO dto = new OrderDTO();
                    dto.setId(order.getId());
                    dto.setOrderNumber(order.getOrderNumber());
                    dto.setTotal(order.getTotal());
                    dto.setStatus(order.getStatus().name());
                    dto.setCreatedAt(order.getCreatedAt());
                    return dto;
                }).toList();

        return DashboardDTO.builder()
                .totalUsers(totalUsers)
                .totalProducts(totalProducts)
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .pendingOrders(pendingOrders)
                .lowStockProducts(lowStockProducts)
                .recentOrders(recentOrders)
                .build();
    }
}
