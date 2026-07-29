package com.sportx.backend.service;

import com.sportx.backend.dto.request.ProductRequest;
import com.sportx.backend.dto.response.PageResponse;
import com.sportx.backend.dto.response.ProductResponse;

import java.math.BigDecimal;

public interface ProductService {

    PageResponse<ProductResponse> getProducts(
            String keyword,
            Long categoryId,
            Long brandId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            BigDecimal minRating,
            Boolean inStock,
            Boolean hasDiscount,
            int page,
            int size,
            String sortBy,
            String sortDir);

    ProductResponse getProductById(Long id);

    ProductResponse createProduct(ProductRequest request);

    ProductResponse updateProduct(Long id, ProductRequest request);

    void deleteProduct(Long id);
}
