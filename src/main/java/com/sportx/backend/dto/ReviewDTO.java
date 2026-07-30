package com.sportx.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewDTO {
    private Long id;
    private Long userId;
    private String userName;
    private Long productId;
    private int rating;
    private String comment;
    private boolean approved;
    private LocalDateTime createdAt;
}
