package com.ecommerce.superadmin.security;

import com.ecommerce.superadmin.admins.AdminRoles;
import com.ecommerce.superadmin.admins.AdminUserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SuperAdminJwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String API_PREFIX = "/api/super-admin/";
    public static final List<String> PUBLIC_PATHS = List.of(
            "/api/super-admin/health",
            "/api/super-admin/auth/login"
    );

    private final SuperAdminJwtService jwtService;
    private final AdminUserRepository adminUserRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        return !path.startsWith(API_PREFIX) || PUBLIC_PATHS.contains(path);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeUnauthorized(response, "Super admin session required. Please log in.");
            return;
        }

        Optional<Long> superAdminId = jwtService.parseSuperAdminId(authHeader.substring(7));
        if (superAdminId.isEmpty()) {
            writeUnauthorized(response, "Invalid or expired super admin session.");
            return;
        }
        boolean stillSuperAdmin = adminUserRepository.findById(superAdminId.get())
                .filter(AdminRoles::isActiveSuperAdmin)
                .isPresent();
        if (!stillSuperAdmin) {
            writeUnauthorized(response, "Super admin access has been removed for this account.");
            return;
        }

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        superAdminId.get(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"))));
        filterChain.doFilter(request, response);
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), Map.of("message", message));
    }
}
