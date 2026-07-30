package com.sportx.backend.repository;

import com.sportx.backend.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductId(Long productId);
    Page<Review> findByProductIdAndApprovedTrue(Long productId, Pageable pageable);
    List<Review> findByUserId(Long userId);
    boolean existsByUserIdAndProductId(Long userId, Long productId);
    double findAverageRatingByProductId(Long productId);
    int countByProductId(Long productId);
}
