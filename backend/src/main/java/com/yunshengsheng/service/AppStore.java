package com.yunshengsheng.service;

import com.yunshengsheng.domain.Models.*;
import com.yunshengsheng.security.Role;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class AppStore {
    public final AtomicLong ids = new AtomicLong(1000);
    public final Map<Long, Tenant> tenants = new ConcurrentHashMap<>();
    public final Map<Long, User> users = new ConcurrentHashMap<>();
    public final Map<Long, Carrier> carriers = new ConcurrentHashMap<>();
    public final Map<Long, FreightSource> freightSources = new ConcurrentHashMap<>();
    public final Map<Long, Bid> bids = new ConcurrentHashMap<>();
    public final Map<Long, Order> orders = new ConcurrentHashMap<>();
    public final Map<Long, Message> messages = new ConcurrentHashMap<>();
    public final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public long nextId() {
        return ids.incrementAndGet();
    }

    @PostConstruct
    void seed() {
        long platformUserId = nextId();
        users.put(platformUserId, new User(platformUserId, null, null, "平台管理员", "13000000000", passwordEncoder.encode("123456"), Role.PLATFORM_ADMIN, "ACTIVE"));

        long tenantId = nextId();
        long shipperUserId = nextId();
        tenants.put(tenantId, new Tenant(tenantId, "示例工厂", shipperUserId, "ACTIVE", LocalDateTime.now()));
        users.put(shipperUserId, new User(shipperUserId, tenantId, null, "货主管理员", "13800000000", passwordEncoder.encode("123456"), Role.SHIPPER_ADMIN, "ACTIVE"));

        long carrierId = nextId();
        carriers.put(carrierId, new Carrier(carrierId, "顺安车队", "道路运输证-A001", "李四", "13900000000", 4.8, "ACTIVE"));
        long carrierUserId = nextId();
        users.put(carrierUserId, new User(carrierUserId, null, carrierId, "承运商管理员", "13900000000", passwordEncoder.encode("123456"), Role.CARRIER_ADMIN, "ACTIVE"));

        FreightSource source = new FreightSource();
        source.id = nextId();
        source.tenantId = tenantId;
        source.publishNo = "YS" + source.id;
        source.userId = shipperUserId;
        source.cargoName = "钢材";
        source.cargoType = "GENERAL";
        source.weight = new BigDecimal("10");
        source.volume = new BigDecimal("12");
        source.originAddress = "上海市浦东新区";
        source.destAddress = "江苏省苏州市";
        source.loadingTime = LocalDateTime.now().plusDays(1);
        source.deadline = LocalDateTime.now().plusDays(1).plusHours(8);
        source.vehicleTypes = List.of("13M", "9.6M");
        source.priceModel = "TOTAL_PRICE";
        source.floorPrice = new BigDecimal("5000");
        source.showBidCount = true;
        source.status = "ACTIVE";
        source.createdAt = LocalDateTime.now();
        freightSources.put(source.id, source);
    }
}
