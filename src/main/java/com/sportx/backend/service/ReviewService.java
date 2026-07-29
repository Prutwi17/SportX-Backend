package com.sportx.backend.service;

import com.sportx.backend.dto.request.ReviewRequest;
import com.sportx.backend.dto.response.PageResponse;
import com.sportx.backend.dto.response.ReviewResponse;

public interface ReviewService {

    ReviewResponse addReview(Long productId, ReviewRequest request);

    PageResponse<ReviewResponse> getProductReviews(Long productId, int page, int size);

    void deleteReview(Long reviewId);
}
