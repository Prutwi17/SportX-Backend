package com.sportx.backend.service;

import com.sportx.backend.dto.*;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    PagedResponse<ProductDTO> getAllProducts(Pageable pageable);
    ProductDTO getProductById(Long id);
    ProductDTO createProduct(ProductRequest request);
    ProductDTO updateProduct(Long id, ProductRequest request);
    void deleteProduct(Long id);
    PagedResponse<ProductDTO> searchProducts(String keyword, Pageable pageable);
    PagedResponse<ProductDTO> filterProducts(Long categoryId, Long brandId,
                                              BigDecimal minPrice, BigDecimal maxPrice,
                                              Double minRating, Pageable pageable);
    PagedResponse<ProductDTO> getProductsByCategory(Long categoryId, Pageable pageable);
    PagedResponse<ProductDTO> getProductsByBrand(Long brandId, Pageable pageable);
}
