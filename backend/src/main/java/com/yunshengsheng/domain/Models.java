package com.yunshengsheng.domain;

import com.yunshengsheng.security.Role;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class Models {
    private Models() {
    }

    public record Tenant(Long id, String tenantName, Long adminUserId, String status, LocalDateTime createdAt) {
    }

    public record User(Long id, Long tenantId, Long carrierId, String name, String phone, String passwordHash, Role role, String status) {
    }

    public record Carrier(Long id, String companyName, String licenseNo, String contact, String phone, double score, String status) {
    }

    public static class FreightSource {
        public Long id;
        public Long tenantId;
        public String publishNo;
        public Long userId;
        public String cargoName;
        public String cargoType;
        public BigDecimal weight;
        public BigDecimal volume;
        public String originAddress;
        public String destAddress;
        public LocalDateTime loadingTime;
        public LocalDateTime deadline;
        public List<String> vehicleTypes = new ArrayList<>();
        public String priceModel;
        public BigDecimal floorPrice;
        public boolean showBidCount;
        public List<String> attachments = new ArrayList<>();
        public String remark;
        public String status;
        public LocalDateTime createdAt;
    }

    public static class Bid {
        public Long id;
        public Long sourceId;
        public Long tenantId;
        public Long carrierId;
        public BigDecimal amount;
        public String remark;
        public String attachment;
        public LocalDateTime bidTime;
        public LocalDateTime lastModified;
        public String status;
    }

    public record Order(Long id, String orderNo, Long tenantId, Long sourceId, Long bidId, Long carrierId, BigDecimal price, String status,
                        LocalDateTime confirmTime, LocalDateTime finishedTime, LocalDateTime createdAt) {
    }

    public record Message(Long id, Long receiverId, String type, String title, String content, Long relatedId,
                          boolean read, LocalDateTime createdAt) {
    }
}
