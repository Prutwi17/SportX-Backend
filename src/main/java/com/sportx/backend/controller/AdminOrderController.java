package com.sportx.backend.controller;

import com.sportx.backend.constant.ApiConstants;
import com.sportx.backend.dto.OrderDTO;
import com.sportx.backend.dto.PagedResponse;
import com.sportx.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.ORDERS + "/admin")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<PagedResponse<OrderDTO>> getAllOrders(
            @RequestParam(defaultValue = ApiConstants.PAGE_DEFAULT) int page,
            @RequestParam(defaultValue = ApiConstants.SIZE_DEFAULT) int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(orderService.getAllOrders(pageable));
    }
}
