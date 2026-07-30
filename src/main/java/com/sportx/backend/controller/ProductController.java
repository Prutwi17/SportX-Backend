package com.sportx.backend.controller;

import com.sportx.backend.constant.ApiConstants;
import com.sportx.backend.dto.PagedResponse;
import com.sportx.backend.dto.ProductDTO;
import com.sportx.backend.dto.ProductRequest;
import com.sportx.backend.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping(ApiConstants.PRODUCTS)
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<PagedResponse<ProductDTO>> getAllProducts(
            @RequestParam(defaultValue = ApiConstants.PAGE_DEFAULT) int page,
            @RequestParam(defaultValue = ApiConstants.SIZE_DEFAULT) int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy));
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id,
                                                     @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<PagedResponse<ProductDTO>> searchProducts(
            @RequestParam String q,
            @RequestParam(defaultValue = ApiConstants.PAGE_DEFAULT) int page,
            @RequestParam(defaultValue = ApiConstants.SIZE_DEFAULT) int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy));
        return ResponseEntity.ok(productService.searchProducts(q, pageable));
    }

    @GetMapping("/filter")
    public ResponseEntity<PagedResponse<ProductDTO>> filterProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(defaultValue = ApiConstants.PAGE_DEFAULT) int page,
            @RequestParam(defaultValue = ApiConstants.SIZE_DEFAULT) int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy));
        return ResponseEntity.ok(productService.filterProducts(
                categoryId, brandId, minPrice, maxPrice, minRating, pageable));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<PagedResponse<ProductDTO>> getByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = ApiConstants.PAGE_DEFAULT) int page,
            @RequestParam(defaultValue = ApiConstants.SIZE_DEFAULT) int size) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId, PageRequest.of(page, size)));
    }

    @GetMapping("/brand/{brandId}")
    public ResponseEntity<PagedResponse<ProductDTO>> getByBrand(
            @PathVariable Long brandId,
            @RequestParam(defaultValue = ApiConstants.PAGE_DEFAULT) int page,
            @RequestParam(defaultValue = ApiConstants.SIZE_DEFAULT) int size) {
        return ResponseEntity.ok(productService.getProductsByBrand(brandId, PageRequest.of(page, size)));
    }
}
