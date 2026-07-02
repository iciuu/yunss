package com.yunshengsheng.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class Responses {
    private Responses() {
    }

    public record RegisterResponse(Long tenantId, Long carrierId, String status) {
    }

    public record LoginResponse(String token, String role, Long tenantId, Long carrierId) {
    }

    public record FreightView(Long id, String publishNo, String cargoName, String cargoType, BigDecimal weight,
                              BigDecimal volume, String originAddress, String destAddress, LocalDateTime loadingTime,
                              LocalDateTime deadline, List<String> vehicleTypes, String priceModel,
                              BigDecimal floorPrice, boolean showBidCount, int bidCount, String status,
                              List<String> attachments, String remark) {
    }

    public record MarketFreightView(Long id, String companyName, String cargoName, String cargoType,
                                    String originAddress, String destAddress, LocalDateTime loadingTime,
                                    LocalDateTime deadline, List<String> vehicleTypes, String priceModel,
                                    Integer bidCount, boolean canBid, String status) {
    }

    public record BidView(Long id, Long freightId, Long carrierId, String carrierName, BigDecimal amount,
                          String remark, String attachment, LocalDateTime bidTime, String status,
                          Double rating, Double dealRate) {
    }
}
