package com.sportx.backend.service;

import com.sportx.backend.dto.request.BrandRequest;
import com.sportx.backend.dto.response.BrandResponse;

import java.util.List;

public interface BrandService {

    List<BrandResponse> getAllBrands();

    BrandResponse createBrand(BrandRequest request);

    BrandResponse updateBrand(Long id, BrandRequest request);

    void deleteBrand(Long id);
}
