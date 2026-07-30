package com.sportx.backend.service;

import com.sportx.backend.dto.WishlistItemDTO;

import java.util.List;

public interface WishlistService {
    List<WishlistItemDTO> getWishlist(Long userId);
    WishlistItemDTO addItem(Long userId, Long productId);
    void removeItem(Long userId, Long productId);
    boolean isInWishlist(Long userId, Long productId);
}
