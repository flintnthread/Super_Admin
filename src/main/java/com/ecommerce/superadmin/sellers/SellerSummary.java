package com.ecommerce.superadmin.sellers;

import java.time.LocalDateTime;

public record SellerSummary(
        long id,
        String sellerUniqueId,
        String name,
        String businessName,
        String email,
        String mobile,
        String status,
        boolean kycVerified,
        LocalDateTime createdAt) {
}
