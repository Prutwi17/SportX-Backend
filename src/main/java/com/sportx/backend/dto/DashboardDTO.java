package com.sportx.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DashboardDTO {
    private long totalUsers;
    private long totalProducts;
    private long totalOrders;
    private BigDecimal totalRevenue;
    private long pendingOrders;
    private long lowStockProducts;
    private List<OrderDTO> recentOrders;
    private BigDecimal todayRevenue;
    private BigDecimal monthlyRevenue;
    private BigDecimal yearlyRevenue;
    private long completedOrders;
    private long cancelledOrders;
    private long totalCustomers;
    private long outOfStockProducts;
    private BigDecimal averageOrderValue;
    private List<TopProductDTO> topSellingProducts;
    private List<NameValueDTO> categoryDistribution;
    private List<ChartPointDTO> orderTrend;
    private List<ChartPointDTO> revenueTrend;
}
