package com.ecommerce.superadmin.sellers;

import com.ecommerce.superadmin.common.PageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SellerRepository {

    /** Same values as admin-service's SellerAccountStatus. */
    public static final List<String> STATUSES = List.of(
            "active", "inactive", "pending", "email_pending", "suspended", "rejected", "deact_req", "act_req");

    private final JdbcTemplate jdbc;

    public List<SellerSummary> search(String search, String status, PageRequest page) {
        Filter filter = filter(search, status);
        List<Object> args = new ArrayList<>(filter.args());
        args.add(page.size());
        args.add(page.offset());
        return jdbc.query(
                "SELECT id, seller_unique_id, first_name, last_name, business_name, email, mobile, status, kyc_verified, created_at"
                        + " FROM sellers" + filter.where() + " ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?",
                (rs, rowNum) -> new SellerSummary(
                        rs.getLong("id"),
                        rs.getString("seller_unique_id"),
                        fullName(rs),
                        rs.getString("business_name"),
                        rs.getString("email"),
                        rs.getString("mobile"),
                        rs.getString("status"),
                        rs.getBoolean("kyc_verified"),
                        rs.getObject("created_at", LocalDateTime.class)),
                args.toArray());
    }

    public long count(String search, String status) {
        Filter filter = filter(search, status);
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM sellers" + filter.where(), Long.class, filter.args().toArray());
        return total == null ? 0 : total;
    }

    public Optional<SellerDetail> findById(long id) {
        return jdbc.query(
                "SELECT id, seller_unique_id, first_name, last_name, business_name, business_type, seller_category, email,"
                        + " mobile, city, state, status, email_verified, mobile_verified, profile_completed, kyc_completed,"
                        + " kyc_verified, kyc_verified_at, admin_remarks, last_login_at, created_at, updated_at"
                        + " FROM sellers WHERE id = ?",
                (rs, rowNum) -> new SellerDetail(
                        rs.getLong("id"),
                        rs.getString("seller_unique_id"),
                        fullName(rs),
                        rs.getString("business_name"),
                        rs.getString("business_type"),
                        rs.getString("seller_category"),
                        rs.getString("email"),
                        rs.getString("mobile"),
                        rs.getString("city"),
                        rs.getString("state"),
                        rs.getString("status"),
                        rs.getBoolean("email_verified"),
                        rs.getBoolean("mobile_verified"),
                        rs.getBoolean("profile_completed"),
                        rs.getBoolean("kyc_completed"),
                        rs.getBoolean("kyc_verified"),
                        rs.getObject("kyc_verified_at", LocalDateTime.class),
                        rs.getString("admin_remarks"),
                        rs.getObject("last_login_at", LocalDateTime.class),
                        rs.getObject("created_at", LocalDateTime.class),
                        rs.getObject("updated_at", LocalDateTime.class)),
                id).stream().findFirst();
    }

    private static String fullName(ResultSet rs) throws SQLException {
        String first = rs.getString("first_name");
        String last = rs.getString("last_name");
        String name = ((first == null ? "" : first) + " " + (last == null ? "" : last)).trim();
        return name.isEmpty() ? "Seller" : name;
    }

    private Filter filter(String search, String status) {
        StringBuilder where = new StringBuilder();
        List<Object> args = new ArrayList<>();
        if (search != null && !search.isBlank()) {
            String like = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
            where.append(" AND (LOWER(email) LIKE ? OR LOWER(first_name) LIKE ? OR LOWER(last_name) LIKE ?"
                    + " OR LOWER(business_name) LIKE ? OR LOWER(seller_unique_id) LIKE ? OR mobile LIKE ?)");
            for (int i = 0; i < 6; i++) {
                args.add(like);
            }
        }
        if (status != null) {
            where.append(" AND status = ?");
            args.add(status);
        }
        String clause = where.isEmpty() ? "" : " WHERE" + where.substring(" AND".length());
        return new Filter(clause, args);
    }

    private record Filter(String where, List<Object> args) {
    }
}
