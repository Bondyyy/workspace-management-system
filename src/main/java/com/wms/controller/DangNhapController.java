package com.wms.controller;

import com.wms.dao.NguoiDungDAO;
import com.wms.model.NguoiDung;
import com.wms.util.PasswordUtil;

import java.sql.SQLException;

public class DangNhapController {

    private final NguoiDungDAO nguoiDungDAO = new NguoiDungDAO();

    public enum LoginResult {
        SUCCESS,
        WRONG_PASSWORD,
        USER_NOT_FOUND,
        ACCOUNT_INACTIVE,
        DB_ERROR
    }

    private static NguoiDung currentUser;

    public LoginResult login(String tenTaiKhoan, String matKhau) {
        try {
            NguoiDung user = nguoiDungDAO.findByUsername(tenTaiKhoan);

            if (user == null) {
                return LoginResult.USER_NOT_FOUND;
            }

            if (!"Đang hoạt động".equalsIgnoreCase(user.getTrangThaiND())) {
                return LoginResult.ACCOUNT_INACTIVE;
            }

            if (!PasswordUtil.verify(matKhau, user.getMatKhauMaHoa())) {
                return LoginResult.WRONG_PASSWORD;
            }

            nguoiDungDAO.updateLastLogin(user.getMaND());
            
            currentUser = user;
            return LoginResult.SUCCESS;

        } catch (SQLException e) {
            System.err.println("[Controller] Lỗi SQL: " + e.getMessage());
            return LoginResult.DB_ERROR;
        }
    }

    public static NguoiDung getCurrentUser() {
        return currentUser;
    }

    public static void logout() {
        currentUser = null;
    }
}