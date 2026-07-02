package com.yunshengsheng.service;

import com.yunshengsheng.security.TenantContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
public class DashboardService {
    private final AppStore store;

    public DashboardService(AppStore store) {
        this.store = store;
    }

    public Map<String, Object> shipper() {
        long freight = store.freightSources.values().stream().filter(item -> item.tenantId.equals(TenantContext.tenantId())).count();
        BigDecimal totalCost = store.orders.values().stream()
                .filter(order -> order.tenantId().equals(TenantContext.tenantId()))
                .map(order -> order.price())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return Map.of("totalFreight", freight, "totalCost", totalCost, "avgPrice", freight == 0 ? 0 : totalCost.divide(BigDecimal.valueOf(Math.max(1, freight)), 2, RoundingMode.HALF_UP));
    }

    public Map<String, Object> carrier() {
        long bidCount = store.bids.values().stream().filter(bid -> bid.carrierId.equals(TenantContext.carrierId())).count();
        long winCount = store.bids.values().stream().filter(bid -> bid.carrierId.equals(TenantContext.carrierId())).filter(bid -> "AWARDED".equals(bid.status)).count();
        return Map.of("bidCount", bidCount, "winCount", winCount, "winRate", bidCount == 0 ? 0 : (double) winCount / bidCount);
    }

    public Map<String, Object> platform() {
        return Map.of("tenantCount", store.tenants.size(), "carrierCount", store.carriers.size(), "freightCount", store.freightSources.size(), "orderCount", store.orders.size());
    }
}

