package com.yunshengsheng.controller;

import com.yunshengsheng.common.ApiResponse;
import com.yunshengsheng.dto.Requests.*;
import com.yunshengsheng.dto.Responses.*;
import com.yunshengsheng.service.AwardService;
import com.yunshengsheng.service.BidService;
import com.yunshengsheng.service.FreightService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/freight")
public class FreightController {
    private final FreightService freightService;
    private final BidService bidService;
    private final AwardService awardService;

    public FreightController(FreightService freightService, BidService bidService, AwardService awardService) {
        this.freightService = freightService;
        this.bidService = bidService;
        this.awardService = awardService;
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody FreightCreateRequest request) {
        return ApiResponse.success(freightService.create(request));
    }

    @GetMapping("/list")
    public ApiResponse<List<FreightView>> list(@RequestParam(required = false) String status) {
        return ApiResponse.success(freightService.list(status));
    }

    @GetMapping("/{id}")
    public ApiResponse<FreightView> detail(@PathVariable Long id) {
        return ApiResponse.success(freightService.detail(id));
    }

    @PostMapping("/{id}/close")
    public ApiResponse<Void> close(@PathVariable Long id) {
        freightService.close(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/copy")
    public ApiResponse<Long> copy(@PathVariable Long id) {
        return ApiResponse.success(freightService.copy(id));
    }

    @GetMapping("/{id}/bids")
    public ApiResponse<List<BidView>> bids(@PathVariable Long id) {
        return ApiResponse.success(bidService.bidsForTenantSource(id));
    }

    @PostMapping("/{id}/award")
    public ApiResponse<Long> award(@PathVariable Long id, @RequestBody AwardRequest request) {
        return ApiResponse.success(awardService.award(id, request));
    }

    @PostMapping("/{id}/fail")
    public ApiResponse<Void> fail(@PathVariable Long id, @RequestBody FailRequest request) {
        awardService.fail(id, request.reason());
        return ApiResponse.success(null);
    }
}
