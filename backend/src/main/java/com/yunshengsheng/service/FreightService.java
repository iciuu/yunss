package com.yunshengsheng.service;

import com.yunshengsheng.common.BizException;
import com.yunshengsheng.domain.Models.FreightSource;
import com.yunshengsheng.dto.Requests.FreightCreateRequest;
import com.yunshengsheng.dto.Responses.FreightView;
import com.yunshengsheng.dto.Responses.MarketFreightView;
import com.yunshengsheng.security.Role;
import com.yunshengsheng.security.TenantContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class FreightService {
    private final AppStore store;

    public FreightService(AppStore store) {
        this.store = store;
    }

    public Long create(FreightCreateRequest request) {
        requireTenantRole();
        FreightSource source = new FreightSource();
        source.id = store.nextId();
        source.tenantId = TenantContext.tenantId();
        source.publishNo = "YS" + source.id;
        source.userId = TenantContext.userId();
        source.cargoName = request.cargoName();
        source.cargoType = request.cargoType();
        source.weight = request.weight();
        source.volume = request.volume();
        source.originAddress = request.originAddress();
        source.destAddress = request.destAddress();
        source.loadingTime = request.loadingTime();
        source.deadline = request.deadline();
        source.vehicleTypes = request.vehicleTypes() == null ? List.of() : request.vehicleTypes();
        source.priceModel = request.priceModel();
        source.floorPrice = request.floorPrice();
        source.showBidCount = request.showBidCount();
        source.attachments = request.attachments() == null ? List.of() : request.attachments();
        source.remark = request.remark();
        source.status = "ACTIVE";
        source.createdAt = LocalDateTime.now();
        store.freightSources.put(source.id, source);
        return source.id;
    }

    public List<FreightView> list(String status) {
        requireTenantRole();
        return store.freightSources.values().stream()
                .filter(source -> source.tenantId.equals(TenantContext.tenantId()))
                .filter(source -> status == null || status.isBlank() || source.status.equals(status))
                .sorted(Comparator.comparing(source -> source.createdAt, Comparator.reverseOrder()))
                .map(this::toView)
                .toList();
    }

    public FreightView detail(Long id) {
        FreightSource source = findTenantSource(id);
        return toView(source);
    }

    public void close(Long id) {
        FreightSource source = findTenantSource(id);
        source.status = "CLOSED";
    }

    public Long copy(Long id) {
        FreightSource source = findTenantSource(id);
        return create(new FreightCreateRequest(source.cargoName, source.cargoType, source.weight, source.volume,
                source.originAddress, source.destAddress, source.loadingTime, source.deadline, source.vehicleTypes,
                source.priceModel, source.floorPrice, source.showBidCount, source.attachments, source.remark));
    }

    public List<MarketFreightView> market(String origin, String dest, String vehicleType) {
        requireCarrierRole();
        return store.freightSources.values().stream()
                .filter(source -> "ACTIVE".equals(source.status))
                .filter(source -> source.deadline.isAfter(LocalDateTime.now()))
                .filter(source -> origin == null || source.originAddress.contains(origin))
                .filter(source -> dest == null || source.destAddress.contains(dest))
                .filter(source -> vehicleType == null || source.vehicleTypes.contains(vehicleType))
                .sorted(Comparator.comparing(source -> source.deadline))
                .map(this::toMarketView)
                .toList();
    }

    public MarketFreightView carrierDetail(Long id) {
        requireCarrierRole();
        FreightSource source = store.freightSources.get(id);
        if (source == null || !"ACTIVE".equals(source.status)) {
            throw new BizException(404, "货源不存在");
        }
        return toMarketView(source);
    }

    public FreightSource findTenantSource(Long id) {
        FreightSource source = store.freightSources.get(id);
        if (source == null || !source.tenantId.equals(TenantContext.tenantId())) {
            throw new BizException(404, "货源不存在");
        }
        return source;
    }

    FreightView toView(FreightSource source) {
        int bidCount = (int) store.bids.values().stream().filter(bid -> bid.sourceId.equals(source.id)).count();
        return new FreightView(source.id, source.publishNo, source.cargoName, source.cargoType, source.weight,
                source.volume, source.originAddress, source.destAddress, source.loadingTime, source.deadline,
                source.vehicleTypes, source.priceModel, source.floorPrice, source.showBidCount, bidCount,
                source.status, source.attachments, source.remark);
    }

    private MarketFreightView toMarketView(FreightSource source) {
        int bidCount = (int) store.bids.values().stream().filter(bid -> bid.sourceId.equals(source.id)).count();
        String tenantName = store.tenants.get(source.tenantId).tenantName();
        return new MarketFreightView(source.id, desensitize(tenantName), source.cargoName, source.cargoType,
                source.originAddress, source.destAddress, source.loadingTime, source.deadline, source.vehicleTypes,
                source.priceModel, source.showBidCount ? bidCount : null, true, source.status);
    }

    private String desensitize(String name) {
        if (name == null || name.length() <= 2) {
            return "****";
        }
        return name.charAt(0) + "****" + name.substring(name.length() - 1);
    }

    private void requireTenantRole() {
        if (TenantContext.tenantId() == null || (TenantContext.role() != Role.SHIPPER_ADMIN && TenantContext.role() != Role.SHIPPER_STAFF)) {
            throw new BizException(403, "无货主权限");
        }
    }

    private void requireCarrierRole() {
        if (TenantContext.role() != Role.CARRIER_ADMIN && TenantContext.role() != Role.CARRIER_DRIVER) {
            throw new BizException(403, "无承运商权限");
        }
    }
}
