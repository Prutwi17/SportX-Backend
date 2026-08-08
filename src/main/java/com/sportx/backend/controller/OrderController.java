package com.sportx.backend.controller;

import com.sportx.backend.constant.ApiConstants;
import com.sportx.backend.dto.OrderDTO;
import com.sportx.backend.dto.OrderRequest;
import com.sportx.backend.dto.OrderStatusRequest;
import com.sportx.backend.dto.PagedResponse;
import com.sportx.backend.entity.User;
import com.sportx.backend.security.SecurityUtil;
import com.sportx.backend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.ORDERS)
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final SecurityUtil securityUtil;

    @PostMapping
    public ResponseEntity<OrderDTO> placeOrder(@Valid @RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.placeOrder(securityUtil.getCurrentUserId(), request));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<OrderDTO>> getUserOrders(
            @RequestParam(defaultValue = ApiConstants.PAGE_DEFAULT) int page,
            @RequestParam(defaultValue = ApiConstants.SIZE_DEFAULT) int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(orderService.getUserOrders(securityUtil.getCurrentUserId(), pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        OrderDTO order = orderService.getOrderById(id);
        verifyOrderAccess(order.getUserId());
        return ResponseEntity.ok(order);
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<OrderDTO> getOrderByNumber(@PathVariable String orderNumber) {
        OrderDTO order = orderService.getOrderByNumber(orderNumber);
        verifyOrderAccess(order.getUserId());
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        OrderDTO order = orderService.getOrderById(id);
        verifyOrderAccess(order.getUserId());
        orderService.cancelOrder(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(@PathVariable Long id,
                                                       @Valid @RequestBody OrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request.getStatus()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        OrderDTO order = orderService.getOrderById(id);
        verifyOrderAccess(order.getUserId());
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    private void verifyOrderAccess(Long orderUserId) {
        User currentUser = securityUtil.getCurrentUser();
        if (!currentUser.getRole().name().equals("ROLE_ADMIN")
                && (orderUserId != null && !orderUserId.equals(currentUser.getId()))) {
            throw new com.sportx.backend.exception.UnauthorizedException("Access denied to this order");
        }
    }
}
