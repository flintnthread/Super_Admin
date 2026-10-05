package com.ecommerce.superadmin.security;

import org.springframework.security.crypto.password.PasswordEncoder;

/** admin_users still holds some legacy plain-text passwords next to BCrypt hashes (same rule as admin-service). */
public final class PasswordSupport {

    private PasswordSupport() {
    }

    public static boolean isHash(String stored) {
        return stored != null && (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$"));
    }

    public static boolean matches(PasswordEncoder encoder, String raw, String stored) {
        if (raw == null || stored == null || stored.isEmpty()) {
            return false;
        }
        return isHash(stored) ? encoder.matches(raw, stored) : stored.equals(raw);
    }
}
