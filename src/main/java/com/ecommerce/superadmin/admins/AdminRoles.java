package com.ecommerce.superadmin.admins;

import java.util.List;
import java.util.Locale;

/** Role and status values stored in admin_users; must match admin-service's AdminRole / AdminAccountStatus enums. */
public final class AdminRoles {

    public static final String SUPER_ADMIN = "super_admin";
    public static final String ACTIVE = "active";
    public static final String INACTIVE = "inactive";

    public static final List<String> ROLES = List.of(
            SUPER_ADMIN,
            "admin",
            "product_management",
            "order_management",
            "sellers_management",
            "category_management",
            "finance_management");

    private AdminRoles() {
    }

    /** Lowercases and maps spaces/dashes to underscores; returns null when the value is not a known role. */
    public static String normalize(String role) {
        if (role == null) {
            return null;
        }
        String value = role.trim().toLowerCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
        return ROLES.contains(value) ? value : null;
    }

    public static boolean isActiveSuperAdmin(AdminUserRow admin) {
        return SUPER_ADMIN.equals(admin.role()) && ACTIVE.equals(admin.status());
    }
}
