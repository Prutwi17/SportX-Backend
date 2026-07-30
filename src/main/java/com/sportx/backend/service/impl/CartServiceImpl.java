package com.sportx.backend.service.impl;

import com.sportx.backend.dto.CartDTO;
import com.sportx.backend.dto.CartItemDTO;
import com.sportx.backend.dto.CartItemRequest;
import com.sportx.backend.entity.CartItem;
import com.sportx.backend.entity.Product;
import com.sportx.backend.entity.User;
import com.sportx.backend.exception.BadRequestException;
import com.sportx.backend.exception.ResourceNotFoundException;
import com.sportx.backend.repository.CartItemRepository;
import com.sportx.backend.repository.ProductRepository;
import com.sportx.backend.repository.UserRepository;
import com.sportx.backend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public CartDTO getCart(Long userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);
        return buildCartDTO(items);
    }

    @Override
    @Transactional
    public CartDTO addItem(Long userId, CartItemRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (request.getQuantity() > product.getStockQuantity()) {
            throw new BadRequestException("Insufficient stock");
        }

        var existing = cartItemRepository.findByUserIdAndProductId(userId, request.getProductId());
        if (existing.isPresent()) {
            CartItem item = existing.get();
            int newQty = item.getQuantity() + request.getQuantity();
            if (newQty > product.getStockQuantity()) {
                throw new BadRequestException("Insufficient stock");
            }
            item.setQuantity(newQty);
            cartItemRepository.save(item);
        } else {
            CartItem item = CartItem.builder()
                    .user(user)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cartItemRepository.save(item);
        }

        List<CartItem> items = cartItemRepository.findByUserId(userId);
        return buildCartDTO(items);
    }

    @Override
    @Transactional
    public CartDTO updateItemQuantity(Long userId, Long cartItemId, int quantity) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!item.getUser().getId().equals(userId)) {
            throw new BadRequestException("Cart item does not belong to user");
        }

        if (quantity > item.getProduct().getStockQuantity()) {
            throw new BadRequestException("Insufficient stock");
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);

        List<CartItem> items = cartItemRepository.findByUserId(userId);
        return buildCartDTO(items);
    }

    @Override
    @Transactional
    public CartDTO removeItem(Long userId, Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!item.getUser().getId().equals(userId)) {
            throw new BadRequestException("Cart item does not belong to user");
        }

        cartItemRepository.delete(item);

        List<CartItem> items = cartItemRepository.findByUserId(userId);
        return buildCartDTO(items);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    private CartDTO buildCartDTO(List<CartItem> items) {
        List<CartItemDTO> itemDTOs = items.stream().map(this::mapToDTO).toList();
        BigDecimal subtotal = itemDTOs.stream()
                .map(CartItemDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalItems = itemDTOs.stream().mapToInt(CartItemDTO::getQuantity).sum();

        CartDTO cart = new CartDTO();
        cart.setItems(itemDTOs);
        cart.setTotalItems(totalItems);
        cart.setSubtotal(subtotal);
        return cart;
    }

    private CartItemDTO mapToDTO(CartItem item) {
        CartItemDTO dto = new CartItemDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setProductImage(item.getProduct().getImages().stream()
                .filter(i -> i.isPrimary())
                .map(i -> i.getImageUrl())
                .findFirst().orElse(null));
        dto.setPrice(item.getProduct().getPrice());
        dto.setDiscountedPrice(item.getProduct().getDiscountedPrice());
        dto.setQuantity(item.getQuantity());
        dto.setSubtotal(item.getSubtotal());
        return dto;
    }
}
