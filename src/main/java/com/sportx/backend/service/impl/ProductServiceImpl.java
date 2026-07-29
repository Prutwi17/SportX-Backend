package com.sportx.backend.service.impl;

import com.sportx.backend.dto.request.ProductRequest;
import com.sportx.backend.dto.response.PageResponse;
import com.sportx.backend.dto.response.ProductResponse;
import com.sportx.backend.entity.Brand;
import com.sportx.backend.entity.Category;
import com.sportx.backend.entity.Product;
import com.sportx.backend.entity.ProductImage;
import com.sportx.backend.exception.BadRequestException;
import com.sportx.backend.exception.ResourceNotFoundException;
import com.sportx.backend.mapper.EntityMapper;
import com.sportx.backend.repository.BrandRepository;
import com.sportx.backend.repository.CategoryRepository;
import com.sportx.backend.repository.ProductRepository;
import com.sportx.backend.service.ProductService;
import com.sportx.backend.util.ProductSpecification;
import com.sportx.backend.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getProducts(
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
            String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Product> spec = ProductSpecification.withFilters(
                keyword, categoryId, brandId, minPrice, maxPrice, minRating, inStock, hasDiscount, true);

        Page<ProductResponse> result = productRepository.findAll(spec, pageable)
                .map(EntityMapper::toProductResponse);

        return EntityMapper.toPageResponse(result);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        return EntityMapper.toProductResponse(findProduct(id));
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Brand brand = null;
        if (request.getBrandId() != null) {
            brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));
        }

        if (request.getDiscountPrice() != null && request.getDiscountPrice().compareTo(request.getPrice()) >= 0) {
            throw new BadRequestException("Discount price must be less than regular price");
        }

        Product product = Product.builder()
                .name(request.getName())
                .slug(SlugUtil.toSlug(request.getName()))
                .description(request.getDescription())
                .price(request.getPrice())
                .discountPrice(request.getDiscountPrice())
                .category(category)
                .brand(brand)
                .stockQuantity(request.getStockQuantity())
                .active(request.isActive())
                .build();

        product.setImages(buildImages(product, request.getImageUrls()));
        return EntityMapper.toProductResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = findProduct(id);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Brand brand = null;
        if (request.getBrandId() != null) {
            brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));
        }

        product.setName(request.getName());
        product.setSlug(SlugUtil.toSlug(request.getName()));
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setDiscountPrice(request.getDiscountPrice());
        product.setCategory(category);
        product.setBrand(brand);
        product.setStockQuantity(request.getStockQuantity());
        product.setActive(request.isActive());

        product.getImages().clear();
        product.getImages().addAll(buildImages(product, request.getImageUrls()));

        return EntityMapper.toProductResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = findProduct(id);
        product.setActive(false);
        productRepository.save(product);
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    private List<ProductImage> buildImages(Product product, List<String> imageUrls) {
        List<ProductImage> images = new ArrayList<>();
        if (imageUrls == null || imageUrls.isEmpty()) {
            return images;
        }

        for (int i = 0; i < imageUrls.size(); i++) {
            images.add(ProductImage.builder()
                    .product(product)
                    .imageUrl(imageUrls.get(i))
                    .isPrimary(i == 0)
                    .displayOrder(i)
                    .build());
        }
        return images;
    }
}
