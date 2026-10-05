package com.ecommerce.superadmin.admins;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminUserServiceTest {

    private final AdminUserRepository repository = mock(AdminUserRepository.class);
    private final AdminUserService service = new AdminUserService(repository, new BCryptPasswordEncoder(4));

    @Test
    void superAdminCannotDeactivateSelf() {
        when(repository.findById(1)).thenReturn(Optional.of(row(1, "super_admin", "active")));

        assertBadRequest(() -> service.update(1, 1, new UpdateAdminRequest(null, null, false, null)));
        verify(repository, never()).update(anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    void superAdminCannotDemoteSelf() {
        when(repository.findById(1)).thenReturn(Optional.of(row(1, "super_admin", "active")));

        assertBadRequest(() -> service.update(1, 1, new UpdateAdminRequest(null, "admin", null, null)));
    }

    @Test
    void superAdminCanDeactivateAnotherAdmin() {
        when(repository.findById(2)).thenReturn(Optional.of(row(2, "admin", "active")));

        service.update(1, 2, new UpdateAdminRequest(null, null, false, null));

        verify(repository).update(2, "Staff", "admin", "inactive");
    }

    @Test
    void unknownRoleIsRejected() {
        assertBadRequest(() -> service.create(new CreateAdminRequest("New", "new@flintnthread.in", "Secret@123", "boss", true)));
    }

    @Test
    void createNormalizesRoleAndPicksFreeUsername() {
        when(repository.usernameExists("new")).thenReturn(true);
        when(repository.insert(anyString(), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(5L);
        when(repository.findById(5)).thenReturn(Optional.of(row(5, "product_management", "active")));

        AdminUserResponse created = service.create(
                new CreateAdminRequest("New", "New@FlintNThread.in", "Secret@123", "Product Management", null));

        verify(repository).insert(eq("New"), eq("new@flintnthread.in"), eq("new_2"), anyString(), eq("product_management"), eq("active"));
        assertThat(created.id()).isEqualTo(5);
    }

    @Test
    void duplicateEmailIsConflict() {
        when(repository.emailExists("taken@flintnthread.in")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateAdminRequest("X", "taken@flintnthread.in", "Secret@123", "admin", true)))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    private static void assertBadRequest(Runnable call) {
        assertThatThrownBy(call::run)
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private static AdminUserRow row(long id, String role, String status) {
        return new AdminUserRow(id, "Staff", "staff@flintnthread.in", role, status, "$2a$hash", null, null);
    }
}
