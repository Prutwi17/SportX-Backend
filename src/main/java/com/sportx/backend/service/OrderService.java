package com.sportx.backend.service;

import com.sportx.backend.dto.*;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {
    OrderDTO placeOrder(Long userId, OrderRequest request);
    OrderDTO getOrderById(Long orderId);
    OrderDTO getOrderByNumber(String orderNumber);
    PagedResponse<OrderDTO> getUserOrders(Long userId, Pageable pageable);
    PagedResponse<OrderDTO> getAllOrders(Pageable pageable);
    OrderDTO updateOrderStatus(Long orderId, String status);
    void cancelOrder(Long orderId);
    void deleteOrder(Long orderId);
}
