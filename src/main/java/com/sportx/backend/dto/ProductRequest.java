package com.sportx.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequest {
    @NotBlank
    private String name;
    private String description;
    @NotNull @Positive
    private BigDecimal price;
    private BigDecimal discountedPrice;
    @PositiveOrZero
    private int stockQuantity;
    @NotNull
    private Long categoryId;
    @NotNull
    private Long brandId;
    private List<String> imageUrls;
}
