package com.sportx.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderRequest {
    @NotNull
    private Long addressId;
    private String paymentMethod;
    private String couponCode;
    private String notes;
}
