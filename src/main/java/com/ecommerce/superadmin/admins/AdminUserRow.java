package com.ecommerce.superadmin.admins;

import java.time.LocalDateTime;

public record AdminUserRow(
        long id,
        String name,
        String email,
        String role,
        String status,
        String password,
        LocalDateTime lastLogin,
        LocalDateTime createdAt) {
}
