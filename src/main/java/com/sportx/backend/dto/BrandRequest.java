package com.sportx.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BrandRequest {
    @NotBlank
    private String name;
    private String description;
    private String imageUrl;
}
