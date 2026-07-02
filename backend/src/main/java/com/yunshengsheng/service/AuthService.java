package com.yunshengsheng.service;

import com.yunshengsheng.common.BizException;
import com.yunshengsheng.domain.Models.Carrier;
import com.yunshengsheng.domain.Models.Tenant;
import com.yunshengsheng.domain.Models.User;
import com.yunshengsheng.dto.Requests.*;
import com.yunshengsheng.dto.Responses.*;
import com.yunshengsheng.security.JwtUser;
import com.yunshengsheng.security.Role;
import com.yunshengsheng.security.TokenService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {
    private final AppStore store;
    private final TokenService tokenService;

    public AuthService(AppStore store, TokenService tokenService) {
        this.store = store;
        this.tokenService = tokenService;
    }

    public RegisterResponse registerShipper(ShipperRegisterRequest request) {
        ensurePhoneAvailable(request.phone());
        long tenantId = store.nextId();
        long userId = store.nextId();
        store.tenants.put(tenantId, new Tenant(tenantId, request.companyName(), userId, "PENDING_REVIEW", LocalDateTime.now()));
        store.users.put(userId, new User(userId, tenantId, null, request.contact(), request.phone(), store.passwordEncoder.encode(request.password()), Role.SHIPPER_ADMIN, "PENDING_REVIEW"));
        return new RegisterResponse(tenantId, null, "PENDING_REVIEW");
    }

    public RegisterResponse registerCarrier(CarrierRegisterRequest request) {
        ensurePhoneAvailable(request.phone());
        long carrierId = store.nextId();
        store.carriers.put(carrierId, new Carrier(carrierId, request.companyName(), request.licenseNo(), request.contact(), request.phone(), 5.0, "ACTIVE"));
        long userId = store.nextId();
        store.users.put(userId, new User(userId, null, carrierId, request.contact(), request.phone(), store.passwordEncoder.encode(request.password()), Role.CARRIER_ADMIN, "ACTIVE"));
        return new RegisterResponse(null, carrierId, "ACTIVE");
    }

    public LoginResponse login(LoginRequest request) {
        User user = store.users.values().stream()
                .filter(candidate -> candidate.phone().equals(request.phone()))
                .findFirst()
                .orElseThrow(() -> new BizException(401, "手机号或密码错误"));
        boolean valid = "SMS".equalsIgnoreCase(request.loginType()) || store.passwordEncoder.matches(request.password(), user.passwordHash());
        if (!valid) {
            throw new BizException(401, "手机号或密码错误");
        }
        String token = tokenService.create(new JwtUser(user.id(), user.tenantId(), user.role(), user.carrierId()));
        return new LoginResponse(token, user.role().name(), user.tenantId(), user.carrierId());
    }

    private void ensurePhoneAvailable(String phone) {
        boolean exists = store.users.values().stream().anyMatch(user -> user.phone().equals(phone));
        if (exists) {
            throw new BizException(400, "手机号已注册");
        }
    }
}
