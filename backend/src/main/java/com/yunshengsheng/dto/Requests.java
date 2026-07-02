package com.yunshengsheng.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class Requests {
    private Requests() {
    }

    public record ShipperRegisterRequest(String companyName, String creditCode, String contact, String phone,
                                         String smsCode, String password, String licenseUrl) {
    }

    public record CarrierRegisterRequest(String companyName, String contact, String phone, String smsCode,
                                         String password, String licenseNo) {
    }

    public record LoginRequest(String phone, String password, String smsCode, String loginType) {
    }

    public record FreightCreateRequest(String cargoName, String cargoType, BigDecimal weight, BigDecimal volume,
                                       String originAddress, String destAddress, LocalDateTime loadingTime,
                                       LocalDateTime deadline, List<String> vehicleTypes, String priceModel,
                                       BigDecimal floorPrice, boolean showBidCount, List<String> attachments,
                                       String remark) {
    }

    public record BidCreateRequest(Long freightId, BigDecimal amount, String remark, String attachment) {
    }

    public record BidUpdateRequest(BigDecimal amount, String remark, String attachment) {
    }

    public record AwardRequest(Long bidId, String remark) {
    }

    public record FailRequest(String reason) {
    }
}
