package com.sportx.backend.controller;

import com.sportx.backend.constant.ApiConstants;
import com.sportx.backend.dto.CartDTO;
import com.sportx.backend.dto.CartItemRequest;
import com.sportx.backend.security.SecurityUtil;
import com.sportx.backend.service.CartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.CART)
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final SecurityUtil securityUtil;

    @GetMapping
    public ResponseEntity<CartDTO> getCart() {
        return ResponseEntity.ok(cartService.getCart(securityUtil.getCurrentUserId()));
    }

    @PostMapping
    public ResponseEntity<CartDTO> addItem(@Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.addItem(securityUtil.getCurrentUserId(), request));
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<CartDTO> updateItemQuantity(@PathVariable Long itemId,
                                                       @RequestParam @Min(value = 1, message = "Quantity must be at least 1") int quantity) {
        return ResponseEntity.ok(cartService.updateItemQuantity(securityUtil.getCurrentUserId(), itemId, quantity));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<CartDTO> removeItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItem(securityUtil.getCurrentUserId(), itemId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart(securityUtil.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
