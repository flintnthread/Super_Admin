package com.ecommerce.superadmin.admins;

import com.ecommerce.superadmin.common.PageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AdminUserRepository {

    private static final String COLUMNS = "id, name, email, role, status, password, last_login, created_at";

    private static final RowMapper<AdminUserRow> MAPPER = (rs, rowNum) -> new AdminUserRow(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("role"),
            rs.getString("status"),
            rs.getString("password"),
            rs.getObject("last_login", LocalDateTime.class),
            rs.getObject("created_at", LocalDateTime.class));

    private final JdbcTemplate jdbc;

    public Optional<AdminUserRow> findById(long id) {
        return jdbc.query("SELECT " + COLUMNS + " FROM admin_users WHERE id = ?", MAPPER, id)
                .stream().findFirst();
    }

    public Optional<AdminUserRow> findByEmail(String email) {
        return jdbc.query("SELECT " + COLUMNS + " FROM admin_users WHERE LOWER(email) = LOWER(?)", MAPPER, email)
                .stream().findFirst();
    }

    public boolean emailExists(String email) {
        return exists("SELECT COUNT(*) FROM admin_users WHERE LOWER(email) = LOWER(?)", email);
    }

    public boolean usernameExists(String username) {
        return exists("SELECT COUNT(*) FROM admin_users WHERE LOWER(username) = LOWER(?)", username);
    }

    public List<AdminUserRow> search(String search, String role, String status, PageRequest page) {
        Filter filter = filter(search, role, status);
        List<Object> args = new ArrayList<>(filter.args());
        args.add(page.size());
        args.add(page.offset());
        return jdbc.query(
                "SELECT " + COLUMNS + " FROM admin_users" + filter.where() + " ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?",
                MAPPER, args.toArray());
    }

    public long count(String search, String role, String status) {
        Filter filter = filter(search, role, status);
        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM admin_users" + filter.where(), Long.class, filter.args().toArray());
        return total == null ? 0 : total;
    }

    public long insert(String name, String email, String username, String passwordHash, String role, String status) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO admin_users (name, email, username, password, role, status) VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, username);
            ps.setString(4, passwordHash);
            ps.setString(5, role);
            ps.setString(6, status);
            return ps;
        }, keys);
        Number id = keys.getKey();
        if (id == null) {
            throw new IllegalStateException("Admin insert did not return an id.");
        }
        return id.longValue();
    }

    public void update(long id, String name, String role, String status) {
        jdbc.update("UPDATE admin_users SET name = ?, role = ?, status = ? WHERE id = ?", name, role, status, id);
    }

    public void updatePassword(long id, String passwordHash) {
        jdbc.update("UPDATE admin_users SET password = ? WHERE id = ?", passwordHash, id);
    }

    public void touchLastLogin(long id) {
        jdbc.update("UPDATE admin_users SET last_login = NOW() WHERE id = ?", id);
    }

    private boolean exists(String sql, Object arg) {
        Long count = jdbc.queryForObject(sql, Long.class, arg);
        return count != null && count > 0;
    }

    private Filter filter(String search, String role, String status) {
        StringBuilder where = new StringBuilder();
        List<Object> args = new ArrayList<>();
        if (search != null && !search.isBlank()) {
            String like = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
            where.append(" AND (LOWER(name) LIKE ? OR LOWER(email) LIKE ?)");
            args.add(like);
            args.add(like);
        }
        if (role != null) {
            where.append(" AND role = ?");
            args.add(role);
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
