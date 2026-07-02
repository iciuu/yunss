package com.yunshengsheng.controller;

import com.yunshengsheng.common.ApiResponse;
import com.yunshengsheng.dto.Requests.*;
import com.yunshengsheng.dto.Responses.*;
import com.yunshengsheng.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/shipper/register")
    public ApiResponse<RegisterResponse> registerShipper(@RequestBody ShipperRegisterRequest request) {
        return ApiResponse.success(authService.registerShipper(request));
    }

    @PostMapping("/carrier/register")
    public ApiResponse<RegisterResponse> registerCarrier(@RequestBody CarrierRegisterRequest request) {
        return ApiResponse.success(authService.registerCarrier(request));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/password/reset")
    public ApiResponse<Void> resetPassword() {
        return ApiResponse.success(null);
    }
}
