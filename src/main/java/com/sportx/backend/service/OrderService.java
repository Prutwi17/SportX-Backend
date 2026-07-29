package com.sportx.backend.service;

import com.sportx.backend.dto.request.PlaceOrderRequest;
import com.sportx.backend.dto.request.UpdateOrderStatusRequest;
import com.sportx.backend.dto.response.OrderResponse;
import com.sportx.backend.dto.response.PageResponse;
import com.sportx.backend.enums.OrderStatus;

public interface OrderService {

    OrderResponse placeOrder(PlaceOrderRequest request);

    PageResponse<OrderResponse> getMyOrders(int page, int size);

    OrderResponse getOrderById(Long orderId);

    OrderResponse cancelOrder(Long orderId);

    PageResponse<OrderResponse> getAllOrders(int page, int size);

    PageResponse<OrderResponse> getOrdersByStatus(OrderStatus status, int page, int size);

    OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request);
}
