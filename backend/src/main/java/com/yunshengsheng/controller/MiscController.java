package com.yunshengsheng.controller;

import com.yunshengsheng.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class MiscController {
    @GetMapping("/shipper/profile")
    public ApiResponse<Map<String, Object>> shipperProfile() {
        return ApiResponse.success(Map.of("companyName", "示例工厂", "address", "上海市浦东新区"));
    }

    @PutMapping("/shipper/profile")
    public ApiResponse<Void> updateProfile(@RequestBody Map<String, Object> ignored) {
        return ApiResponse.success(null);
    }

    @GetMapping("/shipper/member/list")
    public ApiResponse<Object> members() {
        return ApiResponse.success(java.util.List.of());
    }

    @PostMapping("/shipper/member/invite")
    public ApiResponse<Void> invite(@RequestBody Map<String, Object> ignored) {
        return ApiResponse.success(null);
    }

    @PostMapping("/shipper/carrier/blacklist")
    public ApiResponse<Void> blacklist(@RequestBody Map<String, Object> ignored) {
        return ApiResponse.success(null);
    }

    @PostMapping("/file/upload")
    public ApiResponse<Map<String, String>> upload() {
        return ApiResponse.success(Map.of("url", "https://oss.local/mock-file"));
    }
}
