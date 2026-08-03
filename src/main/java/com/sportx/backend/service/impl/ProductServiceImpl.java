package com.sportx.backend.service.impl;

import com.sportx.backend.dto.*;
import com.sportx.backend.entity.*;
import com.sportx.backend.exception.BadRequestException;
import com.sportx.backend.exception.ResourceNotFoundException;
import com.sportx.backend.repository.*;
import com.sportx.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final ProductImageRepository productImageRepository;

    @Override
    public PagedResponse<ProductDTO> getAllProducts(Pageable pageable) {
        Page<Product> page = productRepository.findByActiveTrue(pageable);
        return mapToPagedResponse(page);
    }

    @Override
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return mapToDTO(product);
    }

    @Override
    @Transactional
    public ProductDTO createProduct(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .discountedPrice(request.getDiscountedPrice())
                .stockQuantity(request.getStockQuantity())
                .category(category)
                .brand(brand)
                .active(true)
                .build();

        product = productRepository.save(product);

        if (request.getImageUrls() != null) {
            List<ProductImage> images = new ArrayList<>();
            for (int i = 0; i < request.getImageUrls().size(); i++) {
                ProductImage image = ProductImage.builder()
                        .product(product)
                        .imageUrl(request.getImageUrls().get(i))
                        .isPrimary(i == 0)
                        .build();
                images.add(image);
            }
            productImageRepository.saveAll(images);
            product.setImages(images);
        }

        return mapToDTO(product);
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setDiscountedPrice(request.getDiscountedPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(category);
        product.setBrand(brand);
        product = productRepository.save(product);

        if (request.getImageUrls() != null) {
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                productImageRepository.deleteAll(new ArrayList<>(product.getImages()));
                product.getImages().clear();
            }
            List<ProductImage> images = new ArrayList<>();
            for (int i = 0; i < request.getImageUrls().size(); i++) {
                ProductImage image = ProductImage.builder()
                        .product(product)
                        .imageUrl(request.getImageUrls().get(i))
                        .isPrimary(i == 0)
                        .build();
                images.add(image);
            }
            List<ProductImage> savedImages = productImageRepository.saveAll(images);
            product.setImages(savedImages);
        }

        return mapToDTO(product);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        product.setActive(false);
        productRepository.save(product);
    }

    @Override
    public PagedResponse<ProductDTO> searchProducts(String keyword, Pageable pageable) {
        Page<Product> page = productRepository.searchByKeyword(keyword, pageable);
        return mapToPagedResponse(page);
    }

    @Override
    public PagedResponse<ProductDTO> filterProducts(Long categoryId, Long brandId,
                                                     BigDecimal minPrice, BigDecimal maxPrice,
                                                     Double minRating, Pageable pageable) {
        Page<Product> page = productRepository.filterProducts(
                categoryId, brandId, minPrice, maxPrice, minRating, pageable);
        return mapToPagedResponse(page);
    }

    @Override
    public PagedResponse<ProductDTO> getProductsByCategory(Long categoryId, Pageable pageable) {
        Page<Product> page = productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable);
        return mapToPagedResponse(page);
    }

    @Override
    public PagedResponse<ProductDTO> getProductsByBrand(Long brandId, Pageable pageable) {
        Page<Product> page = productRepository.findByBrandIdAndActiveTrue(brandId, pageable);
        return mapToPagedResponse(page);
    }

    private PagedResponse<ProductDTO> mapToPagedResponse(Page<Product> page) {
        List<ProductDTO> products = page.getContent().stream()
                .map(this::mapToDTO)
                .toList();
        return new PagedResponse<>(products, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isLast());
    }

    private ProductDTO mapToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setDiscountedPrice(product.getDiscountedPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setCategoryId(product.getCategory().getId());
        dto.setCategoryName(product.getCategory().getName());
        dto.setBrandId(product.getBrand().getId());
        dto.setBrandName(product.getBrand().getName());
        dto.setActive(product.isActive());
        dto.setAverageRating(product.getAverageRating());
        dto.setRatingCount(product.getRatingCount());

        List<String> imageUrls = product.getImages().stream()
                .map(ProductImage::getImageUrl)
                .toList();
        dto.setImageUrls(imageUrls);
        dto.setPrimaryImage(product.getImages().stream()
                .filter(ProductImage::isPrimary)
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElse(imageUrls.isEmpty() ? null : imageUrls.get(0)));

        return dto;
    }
}
