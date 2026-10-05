package com.ecommerce.superadmin.auth;

import com.ecommerce.superadmin.admins.AdminRoles;
import com.ecommerce.superadmin.admins.AdminUserRepository;
import com.ecommerce.superadmin.admins.AdminUserResponse;
import com.ecommerce.superadmin.admins.AdminUserRow;
import com.ecommerce.superadmin.security.PasswordSupport;
import com.ecommerce.superadmin.security.SuperAdminJwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AdminUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final SuperAdminJwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        AdminUserRow admin = repository.findByEmail(request.email().trim())
                .filter(row -> PasswordSupport.matches(passwordEncoder, request.password(), row.password()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password."));

        if (!AdminRoles.ACTIVE.equals(admin.status())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This account is disabled.");
        }
        if (!AdminRoles.SUPER_ADMIN.equals(admin.role())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This account does not have super admin access.");
        }

        if (!PasswordSupport.isHash(admin.password())) {
            repository.updatePassword(admin.id(), passwordEncoder.encode(request.password()));
        }
        repository.touchLastLogin(admin.id());

        return new LoginResponse(
                jwtService.generateAccessToken(admin.id(), admin.email()),
                jwtService.getExpirySeconds(),
                AdminUserResponse.from(admin));
    }

    public AdminUserResponse me(long superAdminId) {
        return repository.findById(superAdminId)
                .map(AdminUserResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session is no longer valid."));
    }
}
