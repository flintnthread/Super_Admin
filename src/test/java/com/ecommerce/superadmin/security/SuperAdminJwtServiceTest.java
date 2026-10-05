package com.ecommerce.superadmin.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SuperAdminJwtServiceTest {

    private static final String SECRET = "test-secret-that-is-at-least-32-characters-long";

    @Test
    void issuedTokenParsesBackToSameId() {
        SuperAdminJwtService service = new SuperAdminJwtService(SECRET, 1);
        String token = service.generateAccessToken(42L, "owner@flintnthread.in");

        assertThat(service.parseSuperAdminId(token)).contains(42L);
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        String token = new SuperAdminJwtService(SECRET, 1).generateAccessToken(1L, "a@b.c");
        SuperAdminJwtService other = new SuperAdminJwtService(SECRET + "-other", 1);

        assertThat(other.parseSuperAdminId(token)).isEmpty();
    }

    @Test
    void shortSecretFailsFast() {
        assertThatThrownBy(() -> new SuperAdminJwtService("too-short", 1))
                .isInstanceOf(IllegalStateException.class);
    }
}
