package com.sportx.backend.repository;

import com.sportx.backend.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductId(Long productId);
    Page<Review> findByProductIdAndApprovedTrue(Long productId, Pageable pageable);
    List<Review> findByUserId(Long userId);
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    @Query("SELECT COALESCE(AVG(CAST(r.rating as double)), 0.0) FROM Review r WHERE r.product.id = :productId AND r.approved = true")
    Double findAverageRatingByProductId(@Param("productId") Long productId);

    int countByProductId(Long productId);

    @Modifying
    @Query("DELETE FROM Review r WHERE r.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);
}
