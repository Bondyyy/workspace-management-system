package com.wms.service;

import com.wms.dao.NguoiDungDAO;
import com.wms.model.NguoiDungDTO;
import com.wms.util.PasswordUtil;

import java.sql.SQLException;

public class NguoiDungService {

    private final NguoiDungDAO nguoiDungDAO = new NguoiDungDAO();
    
    // ĐĂNG NHẬP
    public enum ketQuaDangNhap {
        THANH_CONG,
        SAI_MAT_KHAU,
        KHONG_THAY_TAI_KHOAN,
        TAI_KHOAN_KHONG_HOAT_DONG,
        LOI_CSDL
    }
    public static class AuthResponse {
        private final ketQuaDangNhap result;
        private final NguoiDungDTO user;

        public AuthResponse(ketQuaDangNhap result, NguoiDungDTO user) {
            this.result = result;
            this.user = user;
        }

        public ketQuaDangNhap getResult() { return result; }
        public NguoiDungDTO getUser() { return user; }
    }

    public AuthResponse authenticate(String tenTaiKhoan, String matKhau) {
        try {
            NguoiDungDTO user = nguoiDungDAO.timTheoTenTaiKhoan(tenTaiKhoan);

            if (user == null) {
                return new AuthResponse(ketQuaDangNhap.KHONG_THAY_TAI_KHOAN, null);
            }

            if (!"Đang hoạt động".equalsIgnoreCase(user.getTrangThaiND())) {
                return new AuthResponse(ketQuaDangNhap.TAI_KHOAN_KHONG_HOAT_DONG, null);
            }

            if (!PasswordUtil.verify(matKhau, user.getMatKhauMaHoa())) {
                return new AuthResponse(ketQuaDangNhap.SAI_MAT_KHAU, null);
            }

            nguoiDungDAO.updateLastLogin(user.getMaND());
            
            return new AuthResponse(ketQuaDangNhap.THANH_CONG, user);

        } catch (SQLException e) {
            System.err.println("[Service] Lỗi SQL: " + e.getMessage());
            return new AuthResponse(ketQuaDangNhap.LOI_CSDL, null);
        }
    }
    
    // ĐĂNG KÝ
    public enum ketQuaDangKy {
        THANH_CONG,
        TAI_KHOAN_DA_TON_TAI,
        DU_LIEU_KHONG_HOP_LE,
        LOI_CSDL
    }

    public ketQuaDangKy register(String tenTaiKhoan, String hoTen, String email, String matKhau) {
        if (tenTaiKhoan == null || tenTaiKhoan.trim().isEmpty() ||
            matKhau == null || matKhau.trim().isEmpty() ||
            email == null || email.trim().isEmpty()) {
            return ketQuaDangKy.DU_LIEU_KHONG_HOP_LE;
        }

        try {
            if (nguoiDungDAO.kiemTraTaiKhoanTonTai(tenTaiKhoan)) {
                return ketQuaDangKy.TAI_KHOAN_DA_TON_TAI;
            }

            NguoiDungDTO newUser = new NguoiDungDTO();
            newUser.setTenTaiKhoan(tenTaiKhoan.trim());
            newUser.setEmail(email.trim());
            
            String matKhauMaHoa = PasswordUtil.hash(matKhau);
            newUser.setMatKhauMaHoa(matKhauMaHoa);
            
            nguoiDungDAO.themNguoiDung(newUser, hoTen.trim());
            return ketQuaDangKy.THANH_CONG;

        } catch (SQLException e) {
            System.err.println("[Service] Lỗi SQL đăng ký: " + e.getMessage());
            return ketQuaDangKy.LOI_CSDL;
        }
    }
}