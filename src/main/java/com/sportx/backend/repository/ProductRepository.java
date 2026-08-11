package com.sportx.backend.repository;

import com.sportx.backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByName(String name);

    Page<Product> findByActiveTrue(Pageable pageable);

    Page<Product> findByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);

    Page<Product> findByBrandIdAndActiveTrue(Long brandId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.category.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.brand.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
           "p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice,
                                   @Param("maxPrice") BigDecimal maxPrice,
                                   Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:brandId IS NULL OR p.brand.id = :brandId) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:minRating IS NULL OR p.averageRating >= :minRating)")
    Page<Product> filterProducts(@Param("categoryId") Long categoryId,
                                 @Param("brandId") Long brandId,
                                 @Param("minPrice") BigDecimal minPrice,
                                 @Param("maxPrice") BigDecimal maxPrice,
                                 @Param("minRating") Double minRating,
                                 Pageable pageable);

    List<Product> findByStockQuantityLessThanAndActiveTrue(int threshold);
    long countByStockQuantityAndActiveTrue(int stockQuantity);

    @Query("SELECT c.name, COUNT(p) FROM Product p JOIN p.category c GROUP BY c.name ORDER BY COUNT(p) DESC")
    List<Object[]> countProductsByCategory();

    @Query("SELECT p FROM Product p WHERE " +
           "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:brandId IS NULL OR p.brand.id = :brandId) AND " +
           "(:active IS NULL OR p.active = :active) AND " +
           "(:stockStatus = 'ALL' OR " +
           "  (:stockStatus = 'IN_STOCK' AND p.stockQuantity >= :lowThreshold) OR " +
           "  (:stockStatus = 'LOW_STOCK' AND p.stockQuantity > 0 AND p.stockQuantity < :lowThreshold) OR " +
           "  (:stockStatus = 'OUT_OF_STOCK' AND p.stockQuantity = 0))")
    Page<Product> adminFilterProducts(@Param("keyword") String keyword,
                                      @Param("categoryId") Long categoryId,
                                      @Param("brandId") Long brandId,
                                      @Param("active") Boolean active,
                                      @Param("stockStatus") String stockStatus,
                                      @Param("lowThreshold") int lowThreshold,
                                      Pageable pageable);

    @Modifying
    @Query("UPDATE Product p SET p.category = null WHERE p.category.id = :categoryId")
    void detachCategory(@Param("categoryId") Long categoryId);

    @Modifying
    @Query("UPDATE Product p SET p.brand = null WHERE p.brand.id = :brandId")
    void detachBrand(@Param("brandId") Long brandId);
}
