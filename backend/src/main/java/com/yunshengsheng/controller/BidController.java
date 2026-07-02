package com.yunshengsheng.controller;

import com.yunshengsheng.common.ApiResponse;
import com.yunshengsheng.dto.Requests.*;
import com.yunshengsheng.dto.Responses.*;
import com.yunshengsheng.service.BidService;
import com.yunshengsheng.service.FreightService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bid")
public class BidController {
    private final BidService bidService;
    private final FreightService freightService;

    public BidController(BidService bidService, FreightService freightService) {
        this.bidService = bidService;
        this.freightService = freightService;
    }

    @GetMapping("/market")
    public ApiResponse<List<MarketFreightView>> market(@RequestParam(required = false) String origin,
                                                       @RequestParam(required = false) String dest,
                                                       @RequestParam(required = false) String vehicleType) {
        return ApiResponse.success(freightService.market(origin, dest, vehicleType));
    }

    @GetMapping("/freight/{id}")
    public ApiResponse<MarketFreightView> freight(@PathVariable Long id) {
        return ApiResponse.success(freightService.carrierDetail(id));
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody BidCreateRequest request) {
        return ApiResponse.success(bidService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody BidUpdateRequest request) {
        bidService.update(id, request);
        return ApiResponse.success(null);
    }

    @GetMapping("/my")
    public ApiResponse<List<BidView>> myBids() {
        return ApiResponse.success(bidService.myBids());
    }
}
