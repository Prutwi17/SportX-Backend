package com.sportx.backend.service.impl;

import com.sportx.backend.dto.*;
import com.sportx.backend.entity.*;
import com.sportx.backend.enums.OrderStatus;
import com.sportx.backend.enums.PaymentMethod;
import com.sportx.backend.enums.PaymentStatus;
import com.sportx.backend.exception.BadRequestException;
import com.sportx.backend.exception.ResourceNotFoundException;
import com.sportx.backend.repository.*;
import com.sportx.backend.service.CouponService;
import com.sportx.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final BigDecimal SHIPPING_COST = new BigDecimal("49.00");
    private static final BigDecimal TAX_RATE = new BigDecimal("0.18");

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final CouponService couponService;

    @Override
    @Transactional
    public OrderDTO placeOrder(Long userId, OrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        if (!address.getUser().getId().equals(userId)) {
            throw new BadRequestException("Address does not belong to user");
        }

        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            if (item.getQuantity() > product.getStockQuantity()) {
                throw new BadRequestException("Insufficient stock for: " + product.getName());
            }
            BigDecimal price = product.getDiscountedPrice() != null
                    ? product.getDiscountedPrice() : product.getPrice();
            subtotal = subtotal.add(price.multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        BigDecimal discount = BigDecimal.ZERO;
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            discount = couponService.calculateDiscount(request.getCouponCode(), subtotal);
        }

        BigDecimal tax = subtotal.multiply(TAX_RATE);
        BigDecimal total = subtotal.add(SHIPPING_COST).add(tax).subtract(discount);

        String orderNumber = "SPORTX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .user(user)
                .address(address)
                .subtotal(subtotal)
                .shippingCost(SHIPPING_COST)
                .tax(tax)
                .discount(discount)
                .total(total)
                .status(OrderStatus.PENDING)
                .paymentMethod(request.getPaymentMethod() != null
                        ? PaymentMethod.valueOf(request.getPaymentMethod()) : PaymentMethod.COD)
                .couponCode(request.getCouponCode())
                .notes(request.getNotes())
                .build();

        order = orderRepository.save(order);

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            BigDecimal price = product.getDiscountedPrice() != null
                    ? product.getDiscountedPrice() : product.getPrice();

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .productName(product.getName())
                    .productImage(product.getImages().stream()
                            .filter(i -> i.isPrimary())
                            .map(i -> i.getImageUrl())
                            .findFirst().orElse(null))
                    .quantity(cartItem.getQuantity())
                    .price(price)
                    .subtotal(price.multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                    .build();

            orderItemRepository.save(orderItem);

            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }

        if (PaymentMethod.COD.equals(order.getPaymentMethod())) {
            Payment payment = Payment.builder()
                    .order(order)
                    .paymentMethod(PaymentMethod.COD)
                    .status(PaymentStatus.PENDING)
                    .amount(total)
                    .build();
            paymentRepository.save(payment);
        }

        cartItemRepository.deleteByUserId(userId);

        return mapToDTO(order);
    }

    @Override
    public OrderDTO getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return mapToDTO(order);
    }

    @Override
    public OrderDTO getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return mapToDTO(order);
    }

    @Override
    public PagedResponse<OrderDTO> getUserOrders(Long userId, Pageable pageable) {
        Page<Order> page = orderRepository.findByUserId(userId, pageable);
        return mapToPagedResponse(page);
    }

    @Override
    public PagedResponse<OrderDTO> getAllOrders(Pageable pageable) {
        Page<Order> page = orderRepository.findAll(pageable);
        return mapToPagedResponse(page);
    }

    @Override
    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.setStatus(OrderStatus.valueOf(status));
        order = orderRepository.save(order);

        if (OrderStatus.DELIVERED.equals(order.getStatus())) {
            Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
            if (payment != null) {
                payment.setStatus(PaymentStatus.PAID);
                payment.setPaidAt(LocalDateTime.now());
                paymentRepository.save(payment);
            }
        }

        return mapToDTO(order);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("Cannot cancel delivered order");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Order already cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);

        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        orderRepository.save(order);
    }

    private PagedResponse<OrderDTO> mapToPagedResponse(Page<Order> page) {
        List<OrderDTO> orders = page.getContent().stream()
                .map(this::mapToDTO)
                .toList();
        return new PagedResponse<>(orders, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isLast());
    }

    private OrderDTO mapToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setSubtotal(order.getSubtotal());
        dto.setShippingCost(order.getShippingCost());
        dto.setTax(order.getTax());
        dto.setDiscount(order.getDiscount());
        dto.setTotal(order.getTotal());
        dto.setStatus(order.getStatus().name());
        dto.setPaymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null);
        dto.setCouponCode(order.getCouponCode());
        dto.setNotes(order.getNotes());
        dto.setCreatedAt(order.getCreatedAt());

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setId(order.getAddress().getId());
        addressDTO.setFullName(order.getAddress().getFullName());
        addressDTO.setPhone(order.getAddress().getPhone());
        addressDTO.setAddressLine1(order.getAddress().getAddressLine1());
        addressDTO.setAddressLine2(order.getAddress().getAddressLine2());
        addressDTO.setCity(order.getAddress().getCity());
        addressDTO.setState(order.getAddress().getState());
        addressDTO.setZipCode(order.getAddress().getZipCode());
        addressDTO.setCountry(order.getAddress().getCountry());
        dto.setAddress(addressDTO);

        List<OrderItemDTO> itemDTOs = order.getOrderItems().stream().map(item -> {
            OrderItemDTO itemDTO = new OrderItemDTO();
            itemDTO.setId(item.getId());
            itemDTO.setProductId(item.getProduct().getId());
            itemDTO.setProductName(item.getProductName());
            itemDTO.setProductImage(item.getProductImage());
            itemDTO.setQuantity(item.getQuantity());
            itemDTO.setPrice(item.getPrice());
            itemDTO.setSubtotal(item.getSubtotal());
            return itemDTO;
        }).toList();
        dto.setItems(itemDTOs);

        return dto;
    }
}
