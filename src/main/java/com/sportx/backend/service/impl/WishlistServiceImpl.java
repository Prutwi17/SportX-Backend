package com.sportx.backend.service.impl;

import com.sportx.backend.dto.WishlistItemDTO;
import com.sportx.backend.entity.Product;
import com.sportx.backend.entity.User;
import com.sportx.backend.entity.WishlistItem;
import com.sportx.backend.exception.DuplicateResourceException;
import com.sportx.backend.exception.ResourceNotFoundException;
import com.sportx.backend.repository.ProductRepository;
import com.sportx.backend.repository.UserRepository;
import com.sportx.backend.repository.WishlistItemRepository;
import com.sportx.backend.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public List<WishlistItemDTO> getWishlist(Long userId) {
        return wishlistItemRepository.findByUserId(userId).stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    @Transactional
    public WishlistItemDTO addItem(Long userId, Long productId) {
        if (wishlistItemRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new DuplicateResourceException("Product already in wishlist");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        WishlistItem item = WishlistItem.builder()
                .user(user)
                .product(product)
                .build();

        item = wishlistItemRepository.save(item);
        return mapToDTO(item);
    }

    @Override
    @Transactional
    public void removeItem(Long userId, Long productId) {
        WishlistItem item = wishlistItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Wishlist item not found"));
        wishlistItemRepository.delete(item);
    }

    @Override
    public boolean isInWishlist(Long userId, Long productId) {
        return wishlistItemRepository.existsByUserIdAndProductId(userId, productId);
    }

    private WishlistItemDTO mapToDTO(WishlistItem item) {
        WishlistItemDTO dto = new WishlistItemDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setProductImage(item.getProduct().getImages().stream()
                .filter(i -> i.isPrimary())
                .map(i -> i.getImageUrl())
                .findFirst().orElse(null));
        dto.setPrice(item.getProduct().getPrice());
        dto.setDiscountedPrice(item.getProduct().getDiscountedPrice());
        dto.setStockQuantity(item.getProduct().getStockQuantity());
        return dto;
    }
}
