package com.sportx.backend.controller;

import com.sportx.backend.constant.ApiConstants;
import com.sportx.backend.dto.WishlistItemDTO;
import com.sportx.backend.security.SecurityUtil;
import com.sportx.backend.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.WISHLIST)
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;
    private final SecurityUtil securityUtil;

    @GetMapping
    public ResponseEntity<List<WishlistItemDTO>> getWishlist() {
        return ResponseEntity.ok(wishlistService.getWishlist(securityUtil.getCurrentUserId()));
    }

    @PostMapping("/{productId}")
    public ResponseEntity<WishlistItemDTO> addItem(@PathVariable Long productId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(wishlistService.addItem(securityUtil.getCurrentUserId(), productId));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long productId) {
        wishlistService.removeItem(securityUtil.getCurrentUserId(), productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/check/{productId}")
    public ResponseEntity<Boolean> checkInWishlist(@PathVariable Long productId) {
        return ResponseEntity.ok(wishlistService.isInWishlist(securityUtil.getCurrentUserId(), productId));
    }
}
