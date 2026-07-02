package com.yunshengsheng.controller;

import com.yunshengsheng.common.ApiResponse;
import com.yunshengsheng.domain.Models.Carrier;
import com.yunshengsheng.domain.Models.Tenant;
import com.yunshengsheng.security.Role;
import com.yunshengsheng.security.TenantContext;
import com.yunshengsheng.service.AppStore;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/v1/platform")
public class PlatformController {
    private final AppStore store;

    public PlatformController(AppStore store) {
        this.store = store;
    }

    @GetMapping("/tenants")
    public ApiResponse<List<Tenant>> tenants() {
        requirePlatform();
        return ApiResponse.success(store.tenants.values().stream()
                .sorted(Comparator.comparing(Tenant::id))
                .toList());
    }

    @GetMapping("/carriers")
    public ApiResponse<List<Carrier>> carriers() {
        requirePlatform();
        return ApiResponse.success(store.carriers.values().stream()
                .sorted(Comparator.comparing(Carrier::id))
                .toList());
    }

    private void requirePlatform() {
        if (TenantContext.role() != Role.PLATFORM_ADMIN) {
            throw new com.yunshengsheng.common.BizException(403, "无平台管理员权限");
        }
    }
}
