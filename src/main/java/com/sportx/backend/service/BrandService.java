package com.sportx.backend.service;

import com.sportx.backend.dto.BrandDTO;
import com.sportx.backend.dto.BrandRequest;

import java.util.List;

public interface BrandService {
    List<BrandDTO> getAllBrands();
    BrandDTO getBrandById(Long id);
    BrandDTO createBrand(BrandRequest request);
    BrandDTO updateBrand(Long id, BrandRequest request);
    void deleteBrand(Long id);
}
