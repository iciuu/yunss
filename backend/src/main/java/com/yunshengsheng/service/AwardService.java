package com.yunshengsheng.service;

import com.yunshengsheng.common.BizException;
import com.yunshengsheng.domain.Models.Bid;
import com.yunshengsheng.domain.Models.FreightSource;
import com.yunshengsheng.domain.Models.Message;
import com.yunshengsheng.domain.Models.Order;
import com.yunshengsheng.dto.Requests.AwardRequest;
import com.yunshengsheng.security.TenantContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AwardService {
    private final AppStore store;
    private final FreightService freightService;

    public AwardService(AppStore store, FreightService freightService) {
        this.store = store;
        this.freightService = freightService;
    }

    public Long award(Long freightId, AwardRequest request) {
        FreightSource source = freightService.findTenantSource(freightId);
        if (!"ACTIVE".equals(source.status)) {
            throw new BizException(400, "货源不可开标");
        }
        Bid bid = store.bids.get(request.bidId());
        if (bid == null || !bid.sourceId.equals(freightId)) {
            throw new BizException(404, "报价不存在");
        }
        source.status = "AWARDED";
        bid.status = "AWARDED";
        long orderId = store.nextId();
        String orderNo = "DD" + orderId;
        store.orders.put(orderId, new Order(orderId, orderNo, TenantContext.tenantId(), freightId, bid.id, bid.carrierId,
                bid.amount, "PENDING_CONFIRM", null, null, LocalDateTime.now()));
        addMessage(bid.carrierId, "ORDER_CREATED", "您已中标", "订单已生成，请确认接单", orderId);
        return orderId;
    }

    public void fail(Long freightId, String reason) {
        FreightSource source = freightService.findTenantSource(freightId);
        source.status = "FAILED";
        store.bids.values().stream()
                .filter(bid -> bid.sourceId.equals(freightId))
                .forEach(bid -> addMessage(bid.carrierId, "BID_FAILED", "竞价已流标", reason, freightId));
    }

    private void addMessage(Long receiverId, String type, String title, String content, Long relatedId) {
        long id = store.nextId();
        store.messages.put(id, new Message(id, receiverId, type, title, content, relatedId, false, LocalDateTime.now()));
    }
}
