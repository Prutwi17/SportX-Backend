package com.sportx.backend.dto;

import lombok.Data;

@Data
public class BrandDTO {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private long productCount;
}
