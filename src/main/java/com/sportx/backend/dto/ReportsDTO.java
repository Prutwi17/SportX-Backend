package com.sportx.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportsDTO {
    private BigDecimal todayRevenue;
    private long todayOrders;
    private BigDecimal monthlyRevenue;
    private long monthlyOrders;
    private BigDecimal yearlyRevenue;
    private long yearlyOrders;
    private BigDecimal totalRevenue;
    private long totalOrders;
    private long completedOrders;
    private long pendingOrders;
    private long cancelledOrders;
    private long totalCustomers;
    private long totalProducts;
    private long lowStockProducts;
    private long outOfStockProducts;
    private BigDecimal averageOrderValue;
    private String bestSellingBrand;
    private List<ChartPointDTO> revenueByDay;
    private List<ChartPointDTO> revenueByMonth;
    private List<ChartPointDTO> orderByDay;
    private List<ChartPointDTO> orderByMonth;
    private List<NameValueDTO> categoryDistribution;
    private List<NameValueDTO> topCategories;
    private List<TopProductDTO> topProducts;
    private List<OrderDTO> recentSales;

    public static ReportsDTO empty() {
        return ReportsDTO.builder()
                .todayRevenue(BigDecimal.ZERO)
                .monthlyRevenue(BigDecimal.ZERO)
                .yearlyRevenue(BigDecimal.ZERO)
                .totalRevenue(BigDecimal.ZERO)
                .averageOrderValue(BigDecimal.ZERO)
                .bestSellingBrand("")
                .revenueByDay(new ArrayList<>())
                .revenueByMonth(new ArrayList<>())
                .orderByDay(new ArrayList<>())
                .orderByMonth(new ArrayList<>())
                .categoryDistribution(new ArrayList<>())
                .topCategories(new ArrayList<>())
                .topProducts(new ArrayList<>())
                .recentSales(new ArrayList<>())
                .build();
    }
}
