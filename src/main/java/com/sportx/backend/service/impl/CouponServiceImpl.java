package com.sportx.backend.service.impl;

import com.sportx.backend.dto.CouponDTO;
import com.sportx.backend.dto.CouponRequest;
import com.sportx.backend.entity.Coupon;
import com.sportx.backend.exception.BadRequestException;
import com.sportx.backend.exception.DuplicateResourceException;
import com.sportx.backend.exception.ResourceNotFoundException;
import com.sportx.backend.repository.CouponRepository;
import com.sportx.backend.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;

    @Override
    public CouponDTO createCoupon(CouponRequest request) {
        if (couponRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Coupon code already exists");
        }

        Coupon coupon = Coupon.builder()
                .code(request.getCode().toUpperCase())
                .discountPercent(request.getDiscountPercent())
                .maxDiscount(request.getMaxDiscount())
                .minOrderAmount(request.getMinOrderAmount())
                .usageLimit(request.getUsageLimit())
                .active(request.isActive())
                .validFrom(request.getValidFrom())
                .validUntil(request.getValidUntil())
                .build();

        coupon = couponRepository.save(coupon);
        return mapToDTO(coupon);
    }

    @Override
    public List<CouponDTO> getAllCoupons() {
        return couponRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public CouponDTO getCouponById(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
        return mapToDTO(coupon);
    }

    @Override
    public CouponDTO getCouponByCode(String code) {
        Coupon coupon = couponRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
        return mapToDTO(coupon);
    }

    @Override
    public void deleteCoupon(Long id) {
        if (!couponRepository.existsById(id)) {
            throw new ResourceNotFoundException("Coupon not found");
        }
        couponRepository.deleteById(id);
    }

    @Override
    public BigDecimal calculateDiscount(String code, BigDecimal orderAmount) {
        Coupon coupon = couponRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new BadRequestException("Invalid coupon code"));

        if (!coupon.isActive()) {
            throw new BadRequestException("Coupon is inactive");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getValidFrom()) || now.isAfter(coupon.getValidUntil())) {
            throw new BadRequestException("Coupon has expired");
        }

        if (coupon.getUsageLimit() > 0 && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new BadRequestException("Coupon usage limit reached");
        }

        if (coupon.getMinOrderAmount() != null
                && orderAmount.compareTo(coupon.getMinOrderAmount()) < 0) {
            throw new BadRequestException(
                    "Minimum order amount of " + coupon.getMinOrderAmount() + " required");
        }

        BigDecimal discount = orderAmount
                .multiply(coupon.getDiscountPercent())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        if (coupon.getMaxDiscount() != null
                && discount.compareTo(coupon.getMaxDiscount()) > 0) {
            discount = coupon.getMaxDiscount();
        }

        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponRepository.save(coupon);

        return discount;
    }

    private CouponDTO mapToDTO(Coupon coupon) {
        CouponDTO dto = new CouponDTO();
        dto.setId(coupon.getId());
        dto.setCode(coupon.getCode());
        dto.setDiscountPercent(coupon.getDiscountPercent());
        dto.setMaxDiscount(coupon.getMaxDiscount());
        dto.setMinOrderAmount(coupon.getMinOrderAmount());
        dto.setUsageLimit(coupon.getUsageLimit());
        dto.setUsedCount(coupon.getUsedCount());
        dto.setActive(coupon.isActive());
        dto.setValidFrom(coupon.getValidFrom());
        dto.setValidUntil(coupon.getValidUntil());
        return dto;
    }
}
