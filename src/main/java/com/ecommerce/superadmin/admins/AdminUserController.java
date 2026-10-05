package com.ecommerce.superadmin.admins;

import com.ecommerce.superadmin.common.PageRequest;
import com.ecommerce.superadmin.common.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/super-admin/admins")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public PageResponse<AdminUserResponse> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return adminUserService.list(search, role, status, PageRequest.of(page, size));
    }

    @GetMapping("/roles")
    public List<String> roles() {
        return AdminRoles.ROLES;
    }

    @GetMapping("/{id}")
    public AdminUserResponse get(@PathVariable long id) {
        return adminUserService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminUserResponse create(@Valid @RequestBody CreateAdminRequest request) {
        return adminUserService.create(request);
    }

    @PutMapping("/{id}")
    public AdminUserResponse update(
            @AuthenticationPrincipal Long superAdminId,
            @PathVariable long id,
            @Valid @RequestBody UpdateAdminRequest request) {
        return adminUserService.update(superAdminId, id, request);
    }
}
