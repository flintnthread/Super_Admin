package com.ecommerce.superadmin.auth;

import com.ecommerce.superadmin.admins.AdminUserRepository;
import com.ecommerce.superadmin.admins.AdminUserRow;
import com.ecommerce.superadmin.security.SuperAdminJwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final AdminUserRepository repository = mock(AdminUserRepository.class);
    private AuthService authService;

    @BeforeEach
    void setUp() {
        SuperAdminJwtService jwt = new SuperAdminJwtService("test-secret-that-is-at-least-32-characters-long", 1);
        authService = new AuthService(repository, encoder, jwt);
    }

    @Test
    void activeSuperAdminGetsToken() {
        when(repository.findByEmail("owner@flintnthread.in"))
                .thenReturn(Optional.of(row(1, "super_admin", "active", encoder.encode("Secret@123"))));

        LoginResponse response = authService.login(new LoginRequest("owner@flintnthread.in", "Secret@123"));

        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.admin().role()).isEqualTo("super_admin");
        verify(repository).touchLastLogin(1);
        verify(repository, never()).updatePassword(anyLong(), anyString());
    }

    @Test
    void wrongPasswordIsUnauthorized() {
        when(repository.findByEmail("owner@flintnthread.in"))
                .thenReturn(Optional.of(row(1, "super_admin", "active", encoder.encode("Secret@123"))));

        assertThatThrownBy(() -> authService.login(new LoginRequest("owner@flintnthread.in", "wrong")))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void regularAdminIsForbidden() {
        when(repository.findByEmail("staff@flintnthread.in"))
                .thenReturn(Optional.of(row(2, "admin", "active", encoder.encode("Secret@123"))));

        assertThatThrownBy(() -> authService.login(new LoginRequest("staff@flintnthread.in", "Secret@123")))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
        verify(repository, never()).touchLastLogin(anyLong());
    }

    @Test
    void legacyPlainPasswordIsUpgradedToBcrypt() {
        when(repository.findByEmail("owner@flintnthread.in"))
                .thenReturn(Optional.of(row(1, "super_admin", "active", "Admin@12345")));

        authService.login(new LoginRequest("owner@flintnthread.in", "Admin@12345"));

        verify(repository).updatePassword(eq(1L), argThat(hash -> encoder.matches("Admin@12345", hash)));
    }

    private static AdminUserRow row(long id, String role, String status, String password) {
        return new AdminUserRow(id, "Owner", "owner@flintnthread.in", role, status, password, null, null);
    }
}
