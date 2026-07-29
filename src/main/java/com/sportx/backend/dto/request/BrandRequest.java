package com.sportx.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BrandRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    private String logoUrl;

    private boolean active = true;
}
