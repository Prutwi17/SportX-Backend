package com.sportx.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal discountedPrice;
    private int stockQuantity;
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String brandName;
    private boolean active;
    private double averageRating;
    private int ratingCount;
    private List<String> imageUrls;
    private String primaryImage;
    private java.time.LocalDateTime createdAt;
}
