package com.sportx.backend.controller;

import com.sportx.backend.constant.ApiConstants;
import com.sportx.backend.dto.ReviewDTO;
import com.sportx.backend.dto.ReviewRequest;
import com.sportx.backend.security.SecurityUtil;
import com.sportx.backend.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.REVIEWS)
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final SecurityUtil securityUtil;

    @PostMapping("/product/{productId}")
    public ResponseEntity<ReviewDTO> createReview(@PathVariable Long productId,
                                                   @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.createReview(securityUtil.getCurrentUserId(), productId, request));
    }

    @GetMapping("/public/product/{productId}")
    public ResponseEntity<List<ReviewDTO>> getProductReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getProductReviews(productId));
    }

    @GetMapping("/product/{productId}/check")
    public ResponseEntity<Boolean> hasReviewed(@PathVariable Long productId) {
        return ResponseEntity.ok(
                reviewService.hasUserReviewedProduct(securityUtil.getCurrentUserId(), productId));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ReviewDTO> approveReview(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.approveReview(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}
