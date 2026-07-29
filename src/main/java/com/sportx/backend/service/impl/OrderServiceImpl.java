package com.sportx.backend.service.impl;

import com.sportx.backend.dto.request.PlaceOrderRequest;
import com.sportx.backend.dto.request.UpdateOrderStatusRequest;
import com.sportx.backend.dto.response.OrderResponse;
import com.sportx.backend.dto.response.PageResponse;
import com.sportx.backend.entity.*;
import com.sportx.backend.enums.OrderStatus;
import com.sportx.backend.enums.PaymentMethod;
import com.sportx.backend.enums.PaymentStatus;
import com.sportx.backend.exception.BadRequestException;
import com.sportx.backend.exception.ResourceNotFoundException;
import com.sportx.backend.mapper.EntityMapper;
import com.sportx.backend.repository.AddressRepository;
import com.sportx.backend.repository.CartRepository;
import com.sportx.backend.repository.OrderRepository;
import com.sportx.backend.repository.ProductRepository;
import com.sportx.backend.repository.UserRepository;
import com.sportx.backend.service.OrderService;
import com.sportx.backend.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.05");
    private static final BigDecimal SHIPPING_FEE = new BigDecimal("99.00");
    private static final Set<OrderStatus> CANCELLABLE = EnumSet.of(
            OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.PACKED);

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        User user = getCurrentUser();
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        Address address = addressRepository.findByIdAndUserId(request.getAddressId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new BadRequestException("Insufficient stock for: " + product.getName());
            }

            BigDecimal unitPrice = product.getEffectivePrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            subtotal = subtotal.add(lineTotal);

            orderItems.add(OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .unitPrice(unitPrice)
                    .quantity(cartItem.getQuantity())
                    .lineTotal(lineTotal)
                    .build());

            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }

        BigDecimal tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal total = subtotal.add(tax).add(SHIPPING_FEE).subtract(discount);

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .user(user)
                .status(OrderStatus.PENDING)
                .paymentMethod(request.getPaymentMethod())
                .subtotal(subtotal)
                .tax(tax)
                .shipping(SHIPPING_FEE)
                .discount(discount)
                .totalAmount(total)
                .shippingStreet(address.getStreet())
                .shippingCity(address.getCity())
                .shippingState(address.getState())
                .shippingZipCode(address.getZipCode())
                .shippingCountry(address.getCountry())
                .shippingPhone(user.getPhone())
                .build();

        for (OrderItem item : orderItems) {
            item.setOrder(order);
            order.getItems().add(item);
        }

        Payment payment = Payment.builder()
                .order(order)
                .amount(total)
                .method(request.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .build();
        order.setPayment(payment);

        Order saved = orderRepository.save(order);
        cart.getItems().clear();
        cartRepository.save(cart);

        return EntityMapper.toOrderResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getMyOrders(int page, int size) {
        User user = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderResponse> result = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(EntityMapper::toOrderResponse);
        return EntityMapper.toPageResponse(result);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        Order order = findOrder(orderId);
        User user = getCurrentUser();
        if (!order.getUser().getId().equals(user.getId()) && !EntityMapper.isAdmin(user)) {
            throw new BadRequestException("Access denied to this order");
        }
        return EntityMapper.toOrderResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        Order order = findOrder(orderId);
        User user = getCurrentUser();

        if (!order.getUser().getId().equals(user.getId()) && !EntityMapper.isAdmin(user)) {
            throw new BadRequestException("Access denied to this order");
        }

        if (!CANCELLABLE.contains(order.getStatus())) {
            throw new BadRequestException("Order cannot be cancelled at current status");
        }

        order.setStatus(OrderStatus.CANCELLED);
        restoreStock(order);
        return EntityMapper.toOrderResponse(orderRepository.save(order));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderResponse> result = orderRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(EntityMapper::toOrderResponse);
        return EntityMapper.toPageResponse(result);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getOrdersByStatus(OrderStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderResponse> result = orderRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                .map(EntityMapper::toOrderResponse);
        return EntityMapper.toPageResponse(result);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        Order order = findOrder(orderId);

        if (request.getStatus() == OrderStatus.CANCELLED && order.getStatus() != OrderStatus.CANCELLED) {
            restoreStock(order);
        }

        order.setStatus(request.getStatus());
        return EntityMapper.toOrderResponse(orderRepository.save(order));
    }

    private User getCurrentUser() {
        return userRepository.findByEmail(AuthUtil.currentUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    private String generateOrderNumber() {
        return "SX-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "-" + (int) (Math.random() * 900 + 100);
    }

    private void restoreStock(Order order) {
        for (OrderItem item : order.getItems()) {
            productRepository.findById(item.getProductId()).ifPresent(product -> {
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productRepository.save(product);
            });
        }
    }
}
