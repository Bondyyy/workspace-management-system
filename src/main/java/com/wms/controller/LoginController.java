package com.wms.controller;

import com.wms.dao.UserDAO;
import com.wms.model.User;
import com.wms.util.PasswordUtil;

import java.sql.SQLException;

public class LoginController {

    private final UserDAO userDAO = new UserDAO();

    public enum LoginResult {
        SUCCESS,
        WRONG_PASSWORD,
        USER_NOT_FOUND,
        ACCOUNT_INACTIVE,
        DB_ERROR
    }

    // Giữ user hiện tại sau khi đăng nhập 
    private static User currentUser;

    public LoginResult login(String username, String password) {
        try {
            User user = userDAO.findByUsername(username);

            // 1. Không tìm thấy user
            if (user == null) return LoginResult.USER_NOT_FOUND;

            // 2. Tài khoản bị vô hiệu hóa
            if (!"ACTIVE".equals(user.getStatus())) return LoginResult.ACCOUNT_INACTIVE;

            // 3. Sai mật khẩu
            if (!PasswordUtil.verify(password, user.getPasswordHash())) {
                return LoginResult.WRONG_PASSWORD;
            }

            // 4. Đăng nhập thành công
            userDAO.updateLastLogin(user.getUserId()); // cập nhật last_login_at
            currentUser = user;
            return LoginResult.SUCCESS;

        } catch (SQLException e) {
            e.printStackTrace();
            return LoginResult.DB_ERROR;
        }
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void logout() {
        currentUser = null;
    }
}