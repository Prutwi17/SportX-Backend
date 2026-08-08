package com.sportx.backend.service.impl;

import com.sportx.backend.dto.BrandDTO;
import com.sportx.backend.dto.BrandRequest;
import com.sportx.backend.entity.Brand;
import com.sportx.backend.exception.DuplicateResourceException;
import com.sportx.backend.exception.ResourceNotFoundException;
import com.sportx.backend.repository.BrandRepository;
import com.sportx.backend.repository.ProductRepository;
import com.sportx.backend.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;

    @Override
    public List<BrandDTO> getAllBrands() {
        return brandRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public BrandDTO getBrandById(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));
        return mapToDTO(brand);
    }

    @Override
    public BrandDTO createBrand(BrandRequest request) {
        if (brandRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Brand already exists");
        }

        Brand brand = Brand.builder()
                .name(request.getName())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .build();

        brand = brandRepository.save(brand);
        return mapToDTO(brand);
    }

    @Override
    public BrandDTO updateBrand(Long id, BrandRequest request) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));

        if (!brand.getName().equalsIgnoreCase(request.getName())
                && brandRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Brand name already exists");
        }

        brand.setName(request.getName());
        brand.setDescription(request.getDescription());
        brand.setImageUrl(request.getImageUrl());
        brand = brandRepository.save(brand);
        return mapToDTO(brand);
    }

    @Override
    @Transactional
    public void deleteBrand(Long id) {
        if (!brandRepository.existsById(id)) {
            throw new ResourceNotFoundException("Brand not found");
        }
        productRepository.detachBrand(id);
        brandRepository.deleteById(id);
    }

    private BrandDTO mapToDTO(Brand brand) {
        BrandDTO dto = new BrandDTO();
        dto.setId(brand.getId());
        dto.setName(brand.getName());
        dto.setDescription(brand.getDescription());
        dto.setImageUrl(brand.getImageUrl());
        dto.setProductCount(brand.getProducts().size());
        return dto;
    }
}
