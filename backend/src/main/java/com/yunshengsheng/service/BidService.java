package com.yunshengsheng.service;

import com.yunshengsheng.common.BizException;
import com.yunshengsheng.domain.Models.Bid;
import com.yunshengsheng.domain.Models.Carrier;
import com.yunshengsheng.domain.Models.FreightSource;
import com.yunshengsheng.dto.Requests.BidCreateRequest;
import com.yunshengsheng.dto.Requests.BidUpdateRequest;
import com.yunshengsheng.dto.Responses.BidView;
import com.yunshengsheng.security.Role;
import com.yunshengsheng.security.TenantContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class BidService {
    private final AppStore store;

    public BidService(AppStore store) {
        this.store = store;
    }

    public Long create(BidCreateRequest request) {
        requireCarrier();
        FreightSource source = activeSource(request.freightId());
        store.bids.values().stream()
                .filter(bid -> bid.sourceId.equals(request.freightId()))
                .filter(bid -> bid.carrierId.equals(TenantContext.carrierId()))
                .findFirst()
                .ifPresent(bid -> {
                    throw new BizException(400, "已报价，请修改原报价");
                });
        Bid bid = new Bid();
        bid.id = store.nextId();
        bid.sourceId = request.freightId();
        bid.tenantId = source.tenantId;
        bid.carrierId = TenantContext.carrierId();
        bid.amount = request.amount();
        bid.remark = request.remark();
        bid.attachment = request.attachment();
        bid.bidTime = LocalDateTime.now();
        bid.lastModified = bid.bidTime;
        bid.status = "VALID";
        store.bids.put(bid.id, bid);
        return bid.id;
    }

    public void update(Long id, BidUpdateRequest request) {
        requireCarrier();
        Bid bid = ownBid(id);
        activeSource(bid.sourceId);
        bid.amount = request.amount();
        bid.remark = request.remark();
        bid.attachment = request.attachment();
        bid.lastModified = LocalDateTime.now();
    }

    public List<BidView> myBids() {
        requireCarrier();
        return store.bids.values().stream()
                .filter(bid -> bid.carrierId.equals(TenantContext.carrierId()))
                .sorted(Comparator.comparing(bid -> bid.bidTime, Comparator.reverseOrder()))
                .map(this::toView)
                .toList();
    }

    public List<BidView> bidsForTenantSource(Long freightId) {
        if (TenantContext.tenantId() == null) {
            throw new BizException(403, "无货主权限");
        }
        FreightSource source = store.freightSources.get(freightId);
        if (source == null || !source.tenantId.equals(TenantContext.tenantId())) {
            throw new BizException(404, "货源不存在");
        }
        return store.bids.values().stream()
                .filter(bid -> bid.sourceId.equals(freightId))
                .sorted(Comparator.comparing(bid -> bid.amount))
                .map(this::toView)
                .toList();
    }

    private FreightSource activeSource(Long id) {
        FreightSource source = store.freightSources.get(id);
        if (source == null || !"ACTIVE".equals(source.status)) {
            throw new BizException(404, "货源不存在或已关闭");
        }
        if (!source.deadline.isAfter(LocalDateTime.now())) {
            throw new BizException(400, "已截止报价");
        }
        return source;
    }

    private Bid ownBid(Long id) {
        Bid bid = store.bids.get(id);
        if (bid == null || !bid.carrierId.equals(TenantContext.carrierId())) {
            throw new BizException(404, "报价不存在");
        }
        return bid;
    }

    private BidView toView(Bid bid) {
        Carrier carrier = store.carriers.get(bid.carrierId);
        return new BidView(bid.id, bid.sourceId, bid.carrierId, carrier.companyName(), bid.amount, bid.remark,
                bid.attachment, bid.bidTime, bid.status, carrier.score(), 0.75);
    }

    private void requireCarrier() {
        if (TenantContext.role() != Role.CARRIER_ADMIN || TenantContext.carrierId() == null) {
            throw new BizException(403, "无承运商权限");
        }
    }
}
