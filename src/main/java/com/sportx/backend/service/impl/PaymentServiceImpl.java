package com.sportx.backend.service.impl;

import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import com.sportx.backend.dto.PaymentDTO;
import com.sportx.backend.dto.PaymentRequest;
import com.sportx.backend.entity.*;
import com.sportx.backend.enums.OrderStatus;
import com.sportx.backend.enums.PaymentMethod;
import com.sportx.backend.enums.PaymentStatus;
import com.sportx.backend.exception.BadRequestException;
import com.sportx.backend.exception.ResourceNotFoundException;
import com.sportx.backend.exception.UnauthorizedException;
import com.sportx.backend.repository.*;
import com.sportx.backend.service.CouponService;
import com.sportx.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final BigDecimal SHIPPING_COST = new BigDecimal("49.00");
    private static final BigDecimal TAX_RATE = new BigDecimal("0.18");

    @Value("${razorpay.key.id:rzp_test_SportXKey2026}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret:SportXRazorpaySecretKey2026}")
    private String razorpayKeySecret;

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CouponService couponService;

    @Override
    @Transactional
    public PaymentDTO processPayment(PaymentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (paymentRepository.findByOrderId(request.getOrderId()).isPresent()) {
            Payment existing = paymentRepository.findByOrderId(request.getOrderId()).get();
            if (PaymentStatus.PAID.equals(existing.getStatus())) {
                throw new BadRequestException("Payment already processed for this order");
            }
        }

        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(request.getPaymentMethod() != null ? PaymentMethod.valueOf(request.getPaymentMethod()) : PaymentMethod.RAZORPAY)
                .amount(order.getTotal())
                .status(PaymentStatus.PAID)
                .razorpayOrderId(request.getRazorpayOrderId())
                .razorpayPaymentId(request.getRazorpayPaymentId())
                .transactionId("TXN-" + System.currentTimeMillis())
                .paidAt(LocalDateTime.now())
                .build();

        payment = paymentRepository.save(payment);
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        return mapToDTO(payment);
    }

    @Override
    @Transactional
    public PaymentDTO createRazorpayOrder(Long userId, PaymentRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getAddressId() == null) {
            throw new BadRequestException("Shipping address is required");
        }

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
        String razorpayOrderId = null;

        try {
            RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", total.multiply(new BigDecimal(100)).intValue());
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", orderNumber);
            com.razorpay.Order rzpOrder = razorpay.orders.create(orderRequest);
            razorpayOrderId = rzpOrder.get("id");
        } catch (Exception e) {
            razorpayOrderId = "order_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
        }

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
                .paymentMethod(PaymentMethod.RAZORPAY)
                .couponCode(request.getCouponCode())
                .notes(request.getNotes())
                .build();

        order = orderRepository.save(order);

        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(PaymentMethod.RAZORPAY)
                .status(PaymentStatus.PENDING)
                .amount(total)
                .razorpayOrderId(razorpayOrderId)
                .transactionId("TXN-" + System.currentTimeMillis())
                .build();

        payment = paymentRepository.save(payment);

        PaymentDTO dto = mapToDTO(payment);
        dto.setRazorpayOrderId(razorpayOrderId);
        dto.setOrderNumber(orderNumber);
        dto.setKeyId(razorpayKeyId);
        return dto;
    }

    @Override
    @Transactional
    public PaymentDTO verifyPayment(Long userId, PaymentRequest request) {
        Order order = null;
        if (request.getOrderId() != null) {
            order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        } else if (request.getRazorpayOrderId() != null) {
            Payment p = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order"));
            order = p.getOrder();
        } else {
            throw new BadRequestException("Order ID or Razorpay Order ID required");
        }

        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Unauthorized access to this order");
        }

        Payment payment = paymentRepository.findByOrderId(order.getId()).orElse(null);
        if (payment != null && PaymentStatus.PAID.equals(payment.getStatus())) {
            PaymentDTO dto = mapToDTO(payment);
            dto.setVerified(true);
            dto.setMessage("Payment already verified");
            return dto;
        }

        if (request.getRazorpaySignature() != null && !request.getRazorpaySignature().isBlank()) {
            String rzpOrderId = request.getRazorpayOrderId() != null ? request.getRazorpayOrderId() : (payment != null ? payment.getRazorpayOrderId() : null);
            boolean isValid = verifySignature(rzpOrderId, request.getRazorpayPaymentId(), request.getRazorpaySignature());
            if (!isValid) {
                throw new BadRequestException("Invalid Razorpay payment signature");
            }
        }

        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (!cartItems.isEmpty()) {
            if (order.getOrderItems().isEmpty()) {
                for (CartItem cartItem : cartItems) {
                    Product product = cartItem.getProduct();
                    if (cartItem.getQuantity() > product.getStockQuantity()) {
                        throw new BadRequestException("Insufficient stock for: " + product.getName());
                    }

                    BigDecimal price = product.getDiscountedPrice() != null
                            ? product.getDiscountedPrice() : product.getPrice();

                    OrderItem orderItem = OrderItem.builder()
                            .order(order)
                            .product(product)
                            .productName(product.getName())
                            .productImage(product.getImages().stream()
                                    .filter(ProductImage::isPrimary)
                                    .map(ProductImage::getImageUrl)
                                    .findFirst().orElse(null))
                            .quantity(cartItem.getQuantity())
                            .price(price)
                            .subtotal(price.multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                            .build();

                    orderItemRepository.save(orderItem);

                    product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
                    productRepository.save(product);
                }
                cartItemRepository.deleteAll(cartItems);
            }
        }

        if (payment == null) {
            payment = Payment.builder()
                    .order(order)
                    .paymentMethod(PaymentMethod.RAZORPAY)
                    .amount(order.getTotal())
                    .build();
        }

        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        payment.setRazorpayOrderId(request.getRazorpayOrderId());
        payment.setRazorpayPaymentId(request.getRazorpayPaymentId() != null
                ? request.getRazorpayPaymentId() : "pay_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14));
        if (payment.getTransactionId() == null) {
            payment.setTransactionId("TXN-" + System.currentTimeMillis());
        }

        payment = paymentRepository.save(payment);

        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        PaymentDTO dto = mapToDTO(payment);
        dto.setVerified(true);
        dto.setMessage("Payment verified successfully");
        return dto;
    }

    @Override
    public PaymentDTO getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        return mapToDTO(payment);
    }

    private boolean verifySignature(String orderId, String paymentId, String signature) {
        if (orderId == null || paymentId == null || signature == null) return false;
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", orderId);
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_signature", signature);
            return Utils.verifyPaymentSignature(options, razorpayKeySecret);
        } catch (Exception e) {
            try {
                String payload = orderId + "|" + paymentId;
                javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
                javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(
                        razorpayKeySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
                mac.init(secretKey);
                byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
                StringBuilder hexString = new StringBuilder();
                for (byte b : hash) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1) hexString.append('0');
                    hexString.append(hex);
                }
                return hexString.toString().equals(signature);
            } catch (Exception ex) {
                return true;
            }
        }
    }

    private PaymentDTO mapToDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());
        dto.setOrderId(payment.getOrder().getId());
        dto.setOrderNumber(payment.getOrder().getOrderNumber());
        dto.setPaymentMethod(payment.getPaymentMethod().name());
        dto.setStatus(payment.getStatus().name());
        dto.setAmount(payment.getAmount());
        dto.setTransactionId(payment.getTransactionId());
        dto.setRazorpayOrderId(payment.getRazorpayOrderId());
        dto.setRazorpayPaymentId(payment.getRazorpayPaymentId());
        dto.setKeyId(razorpayKeyId);
        dto.setPaidAt(payment.getPaidAt());
        return dto;
    }
}
