package com.wms.dao;

import com.wms.config.DatabaseConnection;
import com.wms.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public User findByUsername(String username) throws SQLException {
        String sql = """
            SELECT u.user_id, u.username, u.password_hash, u.email,
                   u.status, u.full_name, u.phone_number, u.avatar_url,
                   u.last_login_at, r.role_name
            FROM Users u
            LEFT JOIN UserRoles ur ON u.user_id = ur.user_id
            LEFT JOIN Roles r      ON ur.role_id = r.role_id
                                   AND r.is_active = 1 AND r.is_deleted = 0
            WHERE u.username = ? 
              AND u.is_deleted = 0
        """;

        User user = null;
        List<String> roles = new ArrayList<>();

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                if (user == null) {
                    user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setPasswordHash(rs.getString("password_hash"));
                    user.setEmail(rs.getString("email"));
                    user.setStatus(rs.getString("status"));
                    user.setFullName(rs.getString("full_name"));
                    user.setPhoneNumber(rs.getString("phone_number"));
                    user.setAvatarUrl(rs.getString("avatar_url"));
                    user.setLastLoginAt(rs.getTimestamp("last_login_at"));
                }
                // Thêm role vào list (có thể null nếu user chưa được gán role)
                String roleName = rs.getString("role_name");
                if (roleName != null) roles.add(roleName);
            }
        }

        if (user != null) user.setRoles(roles);
        return user;
    }

    //Cập nhật last_login_at sau khi đăng nhập thành công

    public void updateLastLogin(int userId) throws SQLException {
        String sql = "UPDATE Users SET last_login_at = CURRENT_TIMESTAMP WHERE user_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }
}