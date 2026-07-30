package com.sportx.backend.service;

import com.sportx.backend.dto.CouponDTO;
import com.sportx.backend.dto.CouponRequest;

import java.math.BigDecimal;
import java.util.List;

public interface CouponService {
    CouponDTO createCoupon(CouponRequest request);
    List<CouponDTO> getAllCoupons();
    CouponDTO getCouponById(Long id);
    CouponDTO getCouponByCode(String code);
    void deleteCoupon(Long id);
    BigDecimal calculateDiscount(String code, BigDecimal orderAmount);
}
