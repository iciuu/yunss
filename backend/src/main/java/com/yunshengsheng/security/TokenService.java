package com.yunshengsheng.security;

import com.yunshengsheng.common.BizException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class TokenService {
    public String create(JwtUser user) {
        String payload = user.userId() + ":" + nullToZero(user.tenantId()) + ":" + user.role() + ":" + nullToZero(user.carrierId());
        return Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

    public JwtUser parse(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BizException(401, "未登录");
        }
        try {
            String raw = new String(Base64.getUrlDecoder().decode(authorization.substring(7)), StandardCharsets.UTF_8);
            String[] parts = raw.split(":");
            return new JwtUser(Long.valueOf(parts[0]), zeroToNull(parts[1]), Role.valueOf(parts[2]), zeroToNull(parts[3]));
        } catch (RuntimeException exception) {
            throw new BizException(401, "Token无效");
        }
    }

    private long nullToZero(Long value) {
        return value == null ? 0 : value;
    }

    private Long zeroToNull(String value) {
        long parsed = Long.parseLong(value);
        return parsed == 0 ? null : parsed;
    }
}
