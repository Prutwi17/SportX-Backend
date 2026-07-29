package com.sportx.backend.service.impl;

import com.sportx.backend.dto.request.CartItemRequest;
import com.sportx.backend.dto.response.CartResponse;
import com.sportx.backend.entity.Cart;
import com.sportx.backend.entity.CartItem;
import com.sportx.backend.entity.Product;
import com.sportx.backend.entity.User;
import com.sportx.backend.exception.BadRequestException;
import com.sportx.backend.exception.ResourceNotFoundException;
import com.sportx.backend.mapper.EntityMapper;
import com.sportx.backend.repository.CartItemRepository;
import com.sportx.backend.repository.CartRepository;
import com.sportx.backend.repository.ProductRepository;
import com.sportx.backend.repository.UserRepository;
import com.sportx.backend.service.CartService;
import com.sportx.backend.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart() {
        return EntityMapper.toCartResponse(getOrCreateCart());
    }

    @Override
    @Transactional
    public CartResponse addItem(CartItemRequest request) {
        Cart cart = getOrCreateCart();
        Product product = findActiveProduct(request.getProductId());
        validateStock(product, request.getQuantity());

        CartItem existing = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(null);

        if (existing != null) {
            int newQty = existing.getQuantity() + request.getQuantity();
            validateStock(product, newQty);
            existing.setQuantity(newQty);
            cartItemRepository.save(existing);
        } else {
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cart.getItems().add(item);
            cartItemRepository.save(item);
        }

        return EntityMapper.toCartResponse(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public CartResponse updateItem(Long productId, CartItemRequest request) {
        Cart cart = getOrCreateCart();
        Product product = findActiveProduct(productId);
        validateStock(product, request.getQuantity());

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);
        return EntityMapper.toCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse removeItem(Long productId) {
        Cart cart = getOrCreateCart();
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        return EntityMapper.toCartResponse(cart);
    }

    @Override
    @Transactional
    public void clearCart() {
        Cart cart = getOrCreateCart();
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private Cart getOrCreateCart() {
        User user = userRepository.findByEmail(AuthUtil.currentUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
    }

    private Product findActiveProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!product.isActive()) {
            throw new BadRequestException("Product is not available");
        }
        return product;
    }

    private void validateStock(Product product, int quantity) {
        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be at least 1");
        }
        if (product.getStockQuantity() < quantity) {
            throw new BadRequestException("Insufficient stock for product: " + product.getName());
        }
    }
}
