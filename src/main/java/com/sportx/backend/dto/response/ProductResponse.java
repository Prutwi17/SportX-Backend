package com.sportx.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private BigDecimal effectivePrice;
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String brandName;
    private Integer stockQuantity;
    private BigDecimal averageRating;
    private Integer reviewCount;
    private boolean active;
    private boolean inStock;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
}
