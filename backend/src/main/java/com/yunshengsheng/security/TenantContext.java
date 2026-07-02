package com.yunshengsheng.security;

public final class TenantContext {
    private static final ThreadLocal<JwtUser> CURRENT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void set(JwtUser user) {
        CURRENT.set(user);
    }

    public static JwtUser current() {
        return CURRENT.get();
    }

    public static Long userId() {
        return current() == null ? null : current().userId();
    }

    public static Long tenantId() {
        return current() == null ? null : current().tenantId();
    }

    public static Long carrierId() {
        return current() == null ? null : current().carrierId();
    }

    public static Role role() {
        return current() == null ? null : current().role();
    }

    public static void clear() {
        CURRENT.remove();
    }
}
