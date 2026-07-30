package com.sportx.backend.service.impl;

import com.sportx.backend.dto.ReviewDTO;
import com.sportx.backend.dto.ReviewRequest;
import com.sportx.backend.entity.Product;
import com.sportx.backend.entity.Review;
import com.sportx.backend.entity.User;
import com.sportx.backend.exception.BadRequestException;
import com.sportx.backend.exception.ResourceNotFoundException;
import com.sportx.backend.repository.ProductRepository;
import com.sportx.backend.repository.ReviewRepository;
import com.sportx.backend.repository.UserRepository;
import com.sportx.backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ReviewDTO createReview(Long userId, Long productId, ReviewRequest request) {
        if (reviewRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new BadRequestException("User has already reviewed this product");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(request.getRating())
                .comment(request.getComment())
                .approved(true)
                .build();

        review = reviewRepository.save(review);
        updateProductRating(product);

        return mapToDTO(review);
    }

    @Override
    public List<ReviewDTO> getProductReviews(Long productId) {
        return reviewRepository.findByProductId(productId).stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    @Transactional
    public ReviewDTO approveReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        review.setApproved(true);
        review = reviewRepository.save(review);
        return mapToDTO(review);
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

    @Override
    public boolean hasUserReviewedProduct(Long userId, Long productId) {
        return reviewRepository.existsByUserIdAndProductId(userId, productId);
    }

    private void updateProductRating(Product product) {
        double avgRating = reviewRepository.findAverageRatingByProductId(product.getId());
        int count = reviewRepository.countByProductId(product.getId());
        product.setAverageRating(avgRating);
        product.setRatingCount(count);
        productRepository.save(product);
    }

    private ReviewDTO mapToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setUserId(review.getUser().getId());
        dto.setUserName(review.getUser().getFirstName() + " " + review.getUser().getLastName());
        dto.setProductId(review.getProduct().getId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setApproved(review.isApproved());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }
}
