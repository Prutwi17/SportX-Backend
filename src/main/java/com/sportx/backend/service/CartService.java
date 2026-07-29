package com.sportx.backend.service;

import com.sportx.backend.dto.request.CartItemRequest;
import com.sportx.backend.dto.response.CartResponse;

public interface CartService {

    CartResponse getCart();

    CartResponse addItem(CartItemRequest request);

    CartResponse updateItem(Long productId, CartItemRequest request);

    CartResponse removeItem(Long productId);

    void clearCart();
}
