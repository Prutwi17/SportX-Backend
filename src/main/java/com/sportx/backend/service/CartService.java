package com.sportx.backend.service;

import com.sportx.backend.dto.CartDTO;
import com.sportx.backend.dto.CartItemRequest;

public interface CartService {
    CartDTO getCart(Long userId);
    CartDTO addItem(Long userId, CartItemRequest request);
    CartDTO updateItemQuantity(Long userId, Long cartItemId, int quantity);
    CartDTO removeItem(Long userId, Long cartItemId);
    void clearCart(Long userId);
}
