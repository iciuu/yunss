package com.yunshengsheng.service;

import com.yunshengsheng.common.BizException;
import com.yunshengsheng.domain.Models.Order;
import com.yunshengsheng.security.Role;
import com.yunshengsheng.security.TenantContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class OrderService {
    private final AppStore store;

    public OrderService(AppStore store) {
        this.store = store;
    }

    public List<Order> list() {
        return store.orders.values().stream()
                .filter(this::visible)
                .sorted(Comparator.comparing(Order::createdAt, Comparator.reverseOrder()))
                .toList();
    }

    public Order detail(Long id) {
        Order order = store.orders.get(id);
        if (order == null || !visible(order)) {
            throw new BizException(404, "订单不存在");
        }
        return order;
    }

    public Order confirm(Long id) {
        Order order = detail(id);
        if (TenantContext.role() != Role.CARRIER_ADMIN || !order.carrierId().equals(TenantContext.carrierId())) {
            throw new BizException(403, "无承运商权限");
        }
        if (!"PENDING_CONFIRM".equals(order.status())) {
            throw new BizException(400, "订单状态不可确认");
        }
        Order updated = new Order(order.id(), order.orderNo(), order.tenantId(), order.sourceId(), order.bidId(), order.carrierId(),
                order.price(), "TRANSPORTING", LocalDateTime.now(), order.finishedTime(), order.createdAt());
        store.orders.put(id, updated);
        return updated;
    }

    public Order cancel(Long id) {
        Order order = detail(id);
        Order updated = new Order(order.id(), order.orderNo(), order.tenantId(), order.sourceId(), order.bidId(), order.carrierId(),
                order.price(), "CLOSED", order.confirmTime(), order.finishedTime(), order.createdAt());
        store.orders.put(id, updated);
        return updated;
    }

    private boolean visible(Order order) {
        if (TenantContext.tenantId() != null) {
            return order.tenantId().equals(TenantContext.tenantId());
        }
        if (TenantContext.carrierId() != null) {
            return order.carrierId().equals(TenantContext.carrierId());
        }
        return TenantContext.role() == Role.PLATFORM_ADMIN;
    }
}
