package com.yunshengsheng.security;

public record JwtUser(Long userId, Long tenantId, Role role, Long carrierId) {
}
