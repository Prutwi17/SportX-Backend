package com.sportx.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminUserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String role;
    private String profileImage;
    private boolean enabled;
    private LocalDateTime createdAt;
    private long totalOrders;
    private BigDecimal totalSpending;
}
