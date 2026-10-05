package com.ecommerce.superadmin.admins;

import com.ecommerce.superadmin.common.PageRequest;
import com.ecommerce.superadmin.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AdminUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public PageResponse<AdminUserResponse> list(String search, String role, String status, PageRequest page) {
        String roleFilter = role == null || role.isBlank() ? null : requireRole(role);
        String statusFilter = status == null || status.isBlank() ? null : requireStatus(status);
        return PageResponse.of(
                repository.search(search, roleFilter, statusFilter, page).stream().map(AdminUserResponse::from).toList(),
                page,
                repository.count(search, roleFilter, statusFilter));
    }

    public AdminUserResponse get(long id) {
        return AdminUserResponse.from(require(id));
    }

    public AdminUserResponse create(CreateAdminRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (repository.emailExists(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An admin with this email already exists.");
        }
        String role = requireRole(request.role());
        String status = Boolean.FALSE.equals(request.active()) ? AdminRoles.INACTIVE : AdminRoles.ACTIVE;
        long id = repository.insert(
                request.name().trim(),
                email,
                uniqueUsername(email),
                passwordEncoder.encode(request.password()),
                role,
                status);
        return get(id);
    }

    public AdminUserResponse update(long actingSuperAdminId, long id, UpdateAdminRequest request) {
        AdminUserRow current = require(id);
        String name = request.name() != null ? request.name().trim() : current.name();
        String role = request.role() != null ? requireRole(request.role()) : current.role();
        String status = request.active() == null
                ? current.status()
                : (request.active() ? AdminRoles.ACTIVE : AdminRoles.INACTIVE);

        if (id == actingSuperAdminId && !(AdminRoles.SUPER_ADMIN.equals(role) && AdminRoles.ACTIVE.equals(status))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "You can't remove your own super admin access. Ask another super admin to do it.");
        }

        repository.update(id, name, role, status);
        if (request.password() != null) {
            repository.updatePassword(id, passwordEncoder.encode(request.password()));
        }
        return get(id);
    }

    private AdminUserRow require(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found."));
    }

    private static String requireRole(String role) {
        String normalized = AdminRoles.normalize(role);
        if (normalized == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Unknown role. Use one of: " + String.join(", ", AdminRoles.ROLES) + ".");
        }
        return normalized;
    }

    private static String requireStatus(String status) {
        String value = status.trim().toLowerCase(Locale.ROOT);
        if (!AdminRoles.ACTIVE.equals(value) && !AdminRoles.INACTIVE.equals(value)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must be active or inactive.");
        }
        return value;
    }

    /** admin_users.username is unique; admin-service derives it from the email prefix, so suffix on collision. */
    private String uniqueUsername(String email) {
        String base = email.substring(0, email.indexOf('@'));
        String candidate = base;
        for (int suffix = 2; repository.usernameExists(candidate); suffix++) {
            candidate = base + "_" + suffix;
        }
        return candidate;
    }
}
