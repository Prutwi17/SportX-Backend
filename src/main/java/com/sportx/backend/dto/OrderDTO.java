package com.sportx.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private Long id;
    private String orderNumber;
    private BigDecimal subtotal;
    private BigDecimal shippingCost;
    private BigDecimal tax;
    private BigDecimal discount;
    private BigDecimal total;
    private String status;
    private String paymentMethod;
    private String couponCode;
    private String notes;
    private LocalDateTime createdAt;
    private AddressDTO address;
    private List<OrderItemDTO> items;
}
