package com.sportx.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequest {

    @NotBlank
    @Size(max = 200)
    private String name;

    private String description;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal price;

    @DecimalMin("0.01")
    private BigDecimal discountPrice;

    @NotNull
    private Long categoryId;

    private Long brandId;

    @NotNull
    @Min(0)
    private Integer stockQuantity;

    private boolean active = true;

    private List<String> imageUrls;
}
