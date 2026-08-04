package com.sportx.backend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String productDescription;
    private String categoryName;
    private String productImage;
    private int quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
}
