package com.ecommerce.superadmin.auth;

import com.ecommerce.superadmin.admins.AdminUserResponse;

public record LoginResponse(String accessToken, long expiresInSeconds, AdminUserResponse admin) {
}
