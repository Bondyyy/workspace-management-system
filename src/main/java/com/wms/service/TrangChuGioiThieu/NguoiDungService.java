package com.wms.service.TrangChuGioiThieu;

import com.wms.dao.TrangChuQuanLy.QuanLyNhanVien.NguoiDungDAO;

import com.wms.util.PasswordUtil;
import com.wms.util.EmailUtil;
import com.wms.model.TrangChuGioiThieu.NguoiDungDTO;


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
        LOI_CSDL,
        YEU_CAU_OTP_THANH_CONG,
        EMAIL_DA_TON_TAI,
        LOI_GUI_MAIL,
    }
    
    public static class OtpResponse {
        private final ketQuaDangKy result;
        private final String otp;

        public OtpResponse(ketQuaDangKy result, String otp) {
            this.result = result;
            this.otp = otp;
        }

        public ketQuaDangKy getResult() { return result; }
        public String getOtp() { return otp; }
    }
    
    public OtpResponse yeuCauOTP(String tenTaiKhoan, String email) {
        if (tenTaiKhoan == null || tenTaiKhoan.trim().isEmpty() ||
            email == null || email.trim().isEmpty()) {
            return new OtpResponse(ketQuaDangKy.DU_LIEU_KHONG_HOP_LE, null);
        }

        try {
            if (nguoiDungDAO.kiemTraTaiKhoanTonTai(tenTaiKhoan.trim())) {
                return new OtpResponse(ketQuaDangKy.TAI_KHOAN_DA_TON_TAI, null);
            }
            if (nguoiDungDAO.kiemTraEmailTonTai(email.trim())) {
                return new OtpResponse(ketQuaDangKy.EMAIL_DA_TON_TAI, null);
            }

            String otp = EmailUtil.generateRandomOTP();
            boolean isSent = EmailUtil.sendOTP(email.trim(), otp);

            if (isSent) {
                return new OtpResponse(ketQuaDangKy.YEU_CAU_OTP_THANH_CONG, otp);
            } else {
                return new OtpResponse(ketQuaDangKy.LOI_GUI_MAIL, null);
            }

        } catch (SQLException e) {
            System.err.println("[Service] Lỗi SQL khi yêu cầu OTP: " + e.getMessage());
            return new OtpResponse(ketQuaDangKy.LOI_CSDL, null);
        }
    }
    
    public ketQuaDangKy register(String tenTaiKhoan, String hoTen, String email, String matKhau) {
        if (tenTaiKhoan == null || tenTaiKhoan.trim().isEmpty() ||
            matKhau == null || matKhau.trim().isEmpty() ||
            email == null || email.trim().isEmpty()) {
            return ketQuaDangKy.DU_LIEU_KHONG_HOP_LE;
        }

        try {
            if (nguoiDungDAO.kiemTraTaiKhoanTonTai(tenTaiKhoan.trim())) {
                return ketQuaDangKy.TAI_KHOAN_DA_TON_TAI;
            }
            if (nguoiDungDAO.kiemTraEmailTonTai(email.trim())) {
                return ketQuaDangKy.EMAIL_DA_TON_TAI;
            }

            NguoiDungDTO newUser = new NguoiDungDTO();
            newUser.setTenTaiKhoan(tenTaiKhoan.trim());
            newUser.setEmail(email.trim());
            
            String matKhauMaHoa = PasswordUtil.hash(matKhau);
            newUser.setMatKhauMaHoa(matKhauMaHoa);
            
            nguoiDungDAO.themNguoiDung(newUser, hoTen.trim());
            return ketQuaDangKy.THANH_CONG;

        } catch (SQLException e) {
            System.err.println("[Service] Lỗi SQL: " + e.getMessage());
            return ketQuaDangKy.LOI_CSDL;
        }
    }

    // QUÊN MẬT KHẨU
    public enum ketQuaQuenMatKhau {
        THANH_CONG,
        EMAIL_KHONG_TON_TAI,
        LOI_GUI_MAIL,
        LOI_CSDL,
        DU_LIEU_KHONG_HOP_LE
    }

    public static class OtpQuenPassResponse {
        private final ketQuaQuenMatKhau result;
        private final String otp;

        public OtpQuenPassResponse(ketQuaQuenMatKhau result, String otp) {
            this.result = result;
            this.otp = otp;
        }

        public ketQuaQuenMatKhau getResult() { return result; }
        public String getOtp() { return otp; }
    }

    public OtpQuenPassResponse yeuCauOtpQuenMatKhau(String email) {
        if (email == null || email.trim().isEmpty()) {
            return new OtpQuenPassResponse(ketQuaQuenMatKhau.DU_LIEU_KHONG_HOP_LE, null);
        }

        try {
            if (!nguoiDungDAO.kiemTraEmailTonTai(email.trim())) {
                return new OtpQuenPassResponse(ketQuaQuenMatKhau.EMAIL_KHONG_TON_TAI, null);
            }

            String otp = EmailUtil.generateRandomOTP();
            boolean isSent = EmailUtil.sendOTP(email.trim(), otp);

            if (isSent) {
                return new OtpQuenPassResponse(ketQuaQuenMatKhau.THANH_CONG, otp);
            } else {
                return new OtpQuenPassResponse(ketQuaQuenMatKhau.LOI_GUI_MAIL, null);
            }

        } catch (SQLException e) {
            System.err.println("[Service] Lỗi SQL khi yêu cầu OTP quên mật khẩu: " + e.getMessage());
            return new OtpQuenPassResponse(ketQuaQuenMatKhau.LOI_CSDL, null);
        }
    }

    public ketQuaQuenMatKhau datLaiMatKhau(String email, String matKhauMoi) {
        if (email == null || email.trim().isEmpty() || matKhauMoi == null || matKhauMoi.trim().isEmpty()) {
            return ketQuaQuenMatKhau.DU_LIEU_KHONG_HOP_LE;
        }

        try {
            if (!nguoiDungDAO.kiemTraEmailTonTai(email.trim())) {
                return ketQuaQuenMatKhau.EMAIL_KHONG_TON_TAI;
            }

            String matKhauMaHoa = PasswordUtil.hash(matKhauMoi);
            nguoiDungDAO.capNhatMatKhauTheoEmail(email.trim(), matKhauMaHoa);
            return ketQuaQuenMatKhau.THANH_CONG;

        } catch (SQLException e) {
            System.err.println("[Service] Lỗi SQL khi đặt lại mật khẩu: " + e.getMessage());
            return ketQuaQuenMatKhau.LOI_CSDL;
        }
    }
}




