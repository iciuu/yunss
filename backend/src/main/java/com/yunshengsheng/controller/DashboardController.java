package com.yunshengsheng.controller;

import com.yunshengsheng.common.ApiResponse;
import com.yunshengsheng.service.DashboardService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/shipper")
    public ApiResponse<Map<String, Object>> shipper() {
        return ApiResponse.success(dashboardService.shipper());
    }

    @GetMapping("/carrier")
    public ApiResponse<Map<String, Object>> carrier() {
        return ApiResponse.success(dashboardService.carrier());
    }

    @GetMapping("/platform")
    public ApiResponse<Map<String, Object>> platform() {
        return ApiResponse.success(dashboardService.platform());
    }
}
