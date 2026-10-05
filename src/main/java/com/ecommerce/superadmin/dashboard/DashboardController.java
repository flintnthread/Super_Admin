package com.ecommerce.superadmin.dashboard;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/super-admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final JdbcTemplate jdbc;

    /** A count that fails (for example a missing table) comes back as null instead of failing the whole dashboard. */
    @GetMapping
    public Map<String, Long> stats() {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("totalAdmins", count("SELECT COUNT(*) FROM admin_users"));
        stats.put("activeAdmins", count("SELECT COUNT(*) FROM admin_users WHERE status = 'active'"));
        stats.put("superAdmins", count("SELECT COUNT(*) FROM admin_users WHERE role = 'super_admin' AND status = 'active'"));
        stats.put("totalSellers", count("SELECT COUNT(*) FROM sellers"));
        stats.put("activeSellers", count("SELECT COUNT(*) FROM sellers WHERE status = 'active'"));
        stats.put("pendingSellers", count(
                "SELECT COUNT(*) FROM sellers WHERE status IS NULL OR status IN ('', 'pending', 'email_pending')"));
        stats.put("totalProducts", count("SELECT COUNT(*) FROM products"));
        stats.put("pendingProducts", count("SELECT COUNT(*) FROM products WHERE LOWER(status) = 'pending'"));
        stats.put("totalOrders", count("SELECT COUNT(*) FROM orders"));
        stats.put("totalCustomers", count("SELECT COUNT(*) FROM users"));
        return stats;
    }

    private Long count(String sql) {
        try {
            return jdbc.queryForObject(sql, Long.class);
        } catch (Exception ex) {
            log.warn("Dashboard count failed for [{}]: {}", sql, ex.getMessage());
            return null;
        }
    }
}
