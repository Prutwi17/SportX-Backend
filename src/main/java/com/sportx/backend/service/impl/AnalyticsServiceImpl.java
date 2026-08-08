package com.sportx.backend.service.impl;

import com.sportx.backend.dto.*;
import com.sportx.backend.entity.*;
import com.sportx.backend.enums.OrderStatus;
import com.sportx.backend.enums.UserRole;
import com.sportx.backend.repository.OrderRepository;
import com.sportx.backend.repository.ProductRepository;
import com.sportx.backend.repository.UserRepository;
import com.sportx.backend.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;


@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final int LOW_STOCK_THRESHOLD = 10;

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("MMM yy", Locale.ENGLISH);
    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public ReportsDTO getReports() {
        List<Order> orders = orderRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime startOfYear = now.withDayOfYear(1).toLocalDate().atStartOfDay();

        long totalOrders = orders.size();
        long completedOrders = countByStatus(orders, OrderStatus.DELIVERED);
        long pendingOrders = countByStatus(orders, OrderStatus.PENDING);
        long cancelledOrders = countByStatus(orders, OrderStatus.CANCELLED);

        BigDecimal totalRevenue = sumDelivered(orders);
        BigDecimal todayRevenue = sumDeliveredSince(orders, startOfToday);
        BigDecimal monthlyRevenue = sumDeliveredSince(orders, startOfMonth);
        BigDecimal yearlyRevenue = sumDeliveredSince(orders, startOfYear);

        long todayOrders = countSince(orders, startOfToday);
        long monthlyOrders = countSince(orders, startOfMonth);
        long yearlyOrders = countSince(orders, startOfYear);

        long totalCustomers = userRepository.countByRole(UserRole.ROLE_CUSTOMER);
        long totalProducts = productRepository.count();
        long lowStockProducts = productRepository.findByStockQuantityLessThanAndActiveTrue(LOW_STOCK_THRESHOLD).size();
        long outOfStockProducts = productRepository.countByStockQuantityAndActiveTrue(0);

        BigDecimal averageOrderValue = completedOrders > 0
                ? totalRevenue.divide(BigDecimal.valueOf(completedOrders), 2, RoundingMode.HALF_UP)
                : ZERO;

        SalesAggregation sales = aggregateSales(orders);

        List<ChartPointDTO> revenueByDay = buildDailyTrend(orders, 30, true);
        List<ChartPointDTO> orderByDay = buildDailyTrend(orders, 30, false);
        List<ChartPointDTO> revenueByMonth = buildMonthlyTrend(orders, 12, true);
        List<ChartPointDTO> orderByMonth = buildMonthlyTrend(orders, 12, false);

        List<NameValueDTO> categoryDistribution = productRepository.countProductsByCategory().stream()
                .map(r -> new NameValueDTO((String) r[0], ((Number) r[1]).doubleValue()))
                .toList();

        List<NameValueDTO> topCategories = sales.categoryRevenue.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(5)
                .map(e -> new NameValueDTO(e.getKey(), e.getValue().doubleValue()))
                .toList();

        List<TopProductDTO> topProducts = sales.topProducts.values().stream()
                .sorted(Comparator.comparing(TopProductDTO::getQuantitySold).reversed())
                .limit(5)
                .toList();

        List<OrderDTO> recentSales = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .sorted(Comparator.comparing(Order::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .map(this::mapRecentSale)
                .toList();

        return ReportsDTO.builder()
                .todayRevenue(todayRevenue)
                .todayOrders(todayOrders)
                .monthlyRevenue(monthlyRevenue)
                .monthlyOrders(monthlyOrders)
                .yearlyRevenue(yearlyRevenue)
                .yearlyOrders(yearlyOrders)
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .completedOrders(completedOrders)
                .pendingOrders(pendingOrders)
                .cancelledOrders(cancelledOrders)
                .totalCustomers(totalCustomers)
                .totalProducts(totalProducts)
                .lowStockProducts(lowStockProducts)
                .outOfStockProducts(outOfStockProducts)
                .averageOrderValue(averageOrderValue)
                .bestSellingBrand(sales.bestBrand)
                .revenueByDay(revenueByDay)
                .orderByDay(orderByDay)
                .revenueByMonth(revenueByMonth)
                .orderByMonth(orderByMonth)
                .categoryDistribution(categoryDistribution)
                .topCategories(topCategories)
                .topProducts(topProducts)
                .recentSales(recentSales)
                .build();
    }

    private long countByStatus(List<Order> orders, OrderStatus status) {
        return orders.stream().filter(o -> o.getStatus() == status).count();
    }

    private BigDecimal sumDelivered(List<Order> orders) {
        return orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .map(Order::getTotal)
                .reduce(ZERO, BigDecimal::add);
    }

    private BigDecimal sumDeliveredSince(List<Order> orders, LocalDateTime from) {
        return orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .filter(o -> o.getCreatedAt() != null && !o.getCreatedAt().isBefore(from))
                .map(Order::getTotal)
                .reduce(ZERO, BigDecimal::add);
    }

    private long countSince(List<Order> orders, LocalDateTime from) {
        return orders.stream()
                .filter(o -> o.getCreatedAt() != null && !o.getCreatedAt().isBefore(from))
                .count();
    }

    private List<ChartPointDTO> buildDailyTrend(List<Order> orders, int days, boolean revenue) {
        Map<LocalDate, BigDecimal> revByDay = revenue ? new HashMap<>() : null;
        Map<LocalDate, Long> countByDay = revenue ? null : new HashMap<>();
        for (Order o : orders) {
            if (o.getCreatedAt() == null) continue;
            LocalDate day = o.getCreatedAt().toLocalDate();
            if (revenue) {
                if (o.getStatus() == OrderStatus.DELIVERED) {
                    revByDay.merge(day, o.getTotal(), BigDecimal::add);
                }
            } else {
                countByDay.merge(day, 1L, Long::sum);
            }
        }
        LocalDate today = LocalDate.now();
        List<ChartPointDTO> points = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            String label = day.format(DAY_FMT);
            double value = revenue
                    ? revByDay.getOrDefault(day, ZERO).doubleValue()
                    : countByDay.getOrDefault(day, 0L);
            points.add(new ChartPointDTO(label, value));
        }
        return points;
    }

    private List<ChartPointDTO> buildMonthlyTrend(List<Order> orders, int months, boolean revenue) {
        Map<YearMonth, BigDecimal> revByMonth = revenue ? new HashMap<>() : null;
        Map<YearMonth, Long> countByMonth = revenue ? null : new HashMap<>();
        for (Order o : orders) {
            if (o.getCreatedAt() == null) continue;
            YearMonth ym = YearMonth.from(o.getCreatedAt());
            if (revenue) {
                if (o.getStatus() == OrderStatus.DELIVERED) {
                    revByMonth.merge(ym, o.getTotal(), BigDecimal::add);
                }
            } else {
                countByMonth.merge(ym, 1L, Long::sum);
            }
        }
        YearMonth current = YearMonth.now();
        List<ChartPointDTO> points = new ArrayList<>();
        for (int i = months - 1; i >= 0; i--) {
            YearMonth ym = current.minusMonths(i);
            String label = ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " +
                    String.valueOf(ym.getYear()).substring(2);
            double value = revenue
                    ? revByMonth.getOrDefault(ym, ZERO).doubleValue()
                    : countByMonth.getOrDefault(ym, 0L);
            points.add(new ChartPointDTO(label, value));
        }
        return points;
    }

    private SalesAggregation aggregateSales(List<Order> orders) {
        Map<Long, TopProductDTO> productMap = new LinkedHashMap<>();
        Map<String, BigDecimal> categoryRevenue = new HashMap<>();
        Map<String, BigDecimal> brandRevenue = new HashMap<>();

        for (Order order : orders) {
            if (order.getStatus() == OrderStatus.CANCELLED) continue;
            for (OrderItem item : order.getOrderItems()) {
                Product product = item.getProduct();
                if (product == null) continue;

                TopProductDTO acc = productMap.get(product.getId());
                long qty = item.getQuantity();
                BigDecimal revenue = item.getSubtotal() != null ? item.getSubtotal() : ZERO;
                if (acc == null) {
                    String image = item.getProductImage();
                    if ((image == null || image.isBlank()) && product.getImages() != null) {
                        image = product.getImages().stream()
                                .filter(ProductImage::isPrimary)
                                .map(ProductImage::getImageUrl)
                                .findFirst().orElse(null);
                    }
                    acc = TopProductDTO.builder()
                            .productId(product.getId())
                            .name(item.getProductName() != null ? item.getProductName() : product.getName())
                            .image(image)
                            .quantitySold(qty)
                            .revenue(revenue)
                            .build();
                    productMap.put(product.getId(), acc);
                } else {
                    acc.setQuantitySold(acc.getQuantitySold() + qty);
                    acc.setRevenue(acc.getRevenue().add(revenue));
                }

                if (product.getCategory() != null) {
                    categoryRevenue.merge(product.getCategory().getName(), revenue, BigDecimal::add);
                }
                if (product.getBrand() != null) {
                    brandRevenue.merge(product.getBrand().getName(), revenue, BigDecimal::add);
                }
            }
        }

        String bestBrand = brandRevenue.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("");

        return new SalesAggregation(productMap, categoryRevenue, bestBrand);
    }

    private OrderDTO mapRecentSale(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        if (order.getUser() != null) {
            dto.setUserId(order.getUser().getId());
        }
        dto.setTotal(order.getTotal());
        dto.setStatus(order.getStatus().name());
        dto.setCreatedAt(order.getCreatedAt());
        if (order.getAddress() != null) {
            AddressDTO address = new AddressDTO();
            address.setId(order.getAddress().getId());
            address.setFullName(order.getAddress().getFullName());
            dto.setAddress(address);
        }
        return dto;
    }

    private record SalesAggregation(
            Map<Long, TopProductDTO> topProducts,
            Map<String, BigDecimal> categoryRevenue,
            String bestBrand) {}
}
