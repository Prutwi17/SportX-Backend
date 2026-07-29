package com.sportx.backend.service.impl;

import com.sportx.backend.dto.request.ReviewRequest;
import com.sportx.backend.dto.response.PageResponse;
import com.sportx.backend.dto.response.ReviewResponse;
import com.sportx.backend.entity.Product;
import com.sportx.backend.entity.Review;
import com.sportx.backend.entity.User;
import com.sportx.backend.exception.BadRequestException;
import com.sportx.backend.exception.ResourceNotFoundException;
import com.sportx.backend.mapper.EntityMapper;
import com.sportx.backend.repository.ProductRepository;
import com.sportx.backend.repository.ReviewRepository;
import com.sportx.backend.repository.UserRepository;
import com.sportx.backend.service.ReviewService;
import com.sportx.backend.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ReviewResponse addReview(Long productId, ReviewRequest request) {
        User user = getCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (reviewRepository.existsByProductIdAndUserId(productId, user.getId())) {
            throw new BadRequestException("You have already reviewed this product");
        }

        Review review = Review.builder()
                .product(product)
                .user(user)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review saved = reviewRepository.save(review);
        updateProductRating(product);
        return EntityMapper.toReviewResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getProductReviews(Long productId, int page, int size) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewResponse> result = reviewRepository
                .findByProductIdOrderByCreatedAtDesc(productId, pageable)
                .map(EntityMapper::toReviewResponse);

        return EntityMapper.toPageResponse(result);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        Product product = review.getProduct();
        reviewRepository.delete(review);
        updateProductRating(product);
    }

    private User getCurrentUser() {
        return userRepository.findByEmail(AuthUtil.currentUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void updateProductRating(Product product) {
        Double average = reviewRepository.findAverageRatingByProductId(product.getId());
        long count = reviewRepository.countByProductId(product.getId());

        product.setAverageRating(BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP));
        product.setReviewCount((int) count);
        productRepository.save(product);
    }
}
