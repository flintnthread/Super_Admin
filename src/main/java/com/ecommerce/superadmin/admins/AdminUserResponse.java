package com.ecommerce.superadmin.admins;

import java.time.LocalDateTime;

public record AdminUserResponse(
        long id,
        String name,
        String email,
        String role,
        boolean active,
        LocalDateTime lastLogin,
        LocalDateTime createdAt) {

    public static AdminUserResponse from(AdminUserRow row) {
        return new AdminUserResponse(
                row.id(),
                row.name(),
                row.email(),
                row.role(),
                AdminRoles.ACTIVE.equals(row.status()),
                row.lastLogin(),
                row.createdAt());
    }
}
