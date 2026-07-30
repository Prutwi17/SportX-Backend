package com.sportx.backend.service;

import com.sportx.backend.dto.ReviewDTO;
import com.sportx.backend.dto.ReviewRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReviewService {
    ReviewDTO createReview(Long userId, Long productId, ReviewRequest request);
    List<ReviewDTO> getProductReviews(Long productId);
    ReviewDTO approveReview(Long reviewId);
    void deleteReview(Long reviewId);
    boolean hasUserReviewedProduct(Long userId, Long productId);
}
