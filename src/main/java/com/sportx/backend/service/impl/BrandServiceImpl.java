package com.sportx.backend.service.impl;

import com.sportx.backend.dto.request.BrandRequest;
import com.sportx.backend.dto.response.BrandResponse;
import com.sportx.backend.entity.Brand;
import com.sportx.backend.exception.BadRequestException;
import com.sportx.backend.exception.ResourceNotFoundException;
import com.sportx.backend.mapper.EntityMapper;
import com.sportx.backend.repository.BrandRepository;
import com.sportx.backend.service.BrandService;
import com.sportx.backend.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BrandResponse> getAllBrands() {
        return brandRepository.findByActiveTrue().stream()
                .map(EntityMapper::toBrandResponse)
                .toList();
    }

    @Override
    @Transactional
    public BrandResponse createBrand(BrandRequest request) {
        if (brandRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BadRequestException("Brand already exists");
        }

        Brand brand = Brand.builder()
                .name(request.getName())
                .slug(SlugUtil.toSlug(request.getName()))
                .logoUrl(request.getLogoUrl())
                .active(request.isActive())
                .build();

        return EntityMapper.toBrandResponse(brandRepository.save(brand));
    }

    @Override
    @Transactional
    public BrandResponse updateBrand(Long id, BrandRequest request) {
        Brand brand = findBrand(id);
        brand.setName(request.getName());
        brand.setSlug(SlugUtil.toSlug(request.getName()));
        brand.setLogoUrl(request.getLogoUrl());
        brand.setActive(request.isActive());
        return EntityMapper.toBrandResponse(brandRepository.save(brand));
    }

    @Override
    @Transactional
    public void deleteBrand(Long id) {
        Brand brand = findBrand(id);
        brand.setActive(false);
        brandRepository.save(brand);
    }

    private Brand findBrand(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));
    }
}
