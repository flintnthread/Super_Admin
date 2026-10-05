package com.ecommerce.superadmin.admins;

import jakarta.validation.constraints.Size;

/** Every field is optional; only the ones sent are changed. */
public record UpdateAdminRequest(
        @Size(min = 1, max = 120) String name,
        String role,
        Boolean active,
        @Size(min = 8, max = 100) String password) {
}
