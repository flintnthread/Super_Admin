package com.ecommerce.superadmin.sellers;

import java.time.LocalDateTime;

/** Bank, PAN and Aadhaar fields are deliberately left out. */
public record SellerDetail(
        long id,
        String sellerUniqueId,
        String name,
        String businessName,
        String businessType,
        String sellerCategory,
        String email,
        String mobile,
        String city,
        String state,
        String status,
        boolean emailVerified,
        boolean mobileVerified,
        boolean profileCompleted,
        boolean kycCompleted,
        boolean kycVerified,
        LocalDateTime kycVerifiedAt,
        String adminRemarks,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
