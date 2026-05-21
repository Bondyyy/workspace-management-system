package com.wms.service.TrangChuGioiThieu;

import com.wms.dao.TrangChuQuanLy.QuanLyNguoiDung.NguoiDungDAO;
import com.wms.model.TrangChuQuanLy.QuanLyNguoiDung.NguoiDungDTO;
import com.wms.util.EmailUtil;
import com.wms.util.PasswordUtil;

import java.sql.SQLException;
import java.text.Normalizer;

public class NguoiDungService {

    private final NguoiDungDAO nguoiDungDAO = new NguoiDungDAO();

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

        public ketQuaDangNhap getResult() {
            return result;
        }

        public NguoiDungDTO getUser() {
            return user;
        }
    }

    public AuthResponse authenticate(String tenTaiKhoan, String matKhau) {
        try {
            NguoiDungDTO user = nguoiDungDAO.timTheoTenTaiKhoan(tenTaiKhoan);

            if (user == null) {
                return new AuthResponse(ketQuaDangNhap.KHONG_THAY_TAI_KHOAN, null);
            }

            if (!isActive(user.getTrangThaiND())) {
                return new AuthResponse(ketQuaDangNhap.TAI_KHOAN_KHONG_HOAT_DONG, null);
            }

            if (!PasswordUtil.verify(matKhau, user.getMatKhauMaHoa())) {
                return new AuthResponse(ketQuaDangNhap.SAI_MAT_KHAU, null);
            }

            nguoiDungDAO.capNhatLanDangNhapCuoi(user.getMaND());
            return new AuthResponse(ketQuaDangNhap.THANH_CONG, user);
        } catch (SQLException e) {
            System.err.println("[Service] Lỗi SQL khi đăng nhập: " + e.getMessage());
            return new AuthResponse(ketQuaDangNhap.LOI_CSDL, null);
        }
    }

    public enum ketQuaDangKy {
        THANH_CONG,
        TAI_KHOAN_DA_TON_TAI,
        DU_LIEU_KHONG_HOP_LE,
        LOI_CSDL,
        YEU_CAU_OTP_THANH_CONG,
        EMAIL_DA_TON_TAI,
        LOI_GUI_MAIL
    }

    public static class OtpResponse {
        private final ketQuaDangKy result;
        private final String otp;

        public OtpResponse(ketQuaDangKy result, String otp) {
            this.result = result;
            this.otp = otp;
        }

        public ketQuaDangKy getResult() {
            return result;
        }

        public String getOtp() {
            return otp;
        }
    }

    public OtpResponse yeuCauOTP(String tenTaiKhoan, String email) {
        if (isBlank(tenTaiKhoan) || isBlank(email)) {
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
            return EmailUtil.sendOTP(email.trim(), otp)
                    ? new OtpResponse(ketQuaDangKy.YEU_CAU_OTP_THANH_CONG, otp)
                    : new OtpResponse(ketQuaDangKy.LOI_GUI_MAIL, null);
        } catch (SQLException e) {
            System.err.println("[Service] Lỗi SQL khi yêu cầu OTP: " + e.getMessage());
            return new OtpResponse(ketQuaDangKy.LOI_CSDL, null);
        }
    }

    public ketQuaDangKy register(String tenTaiKhoan, String hoTen, String email, String matKhau) {
        if (isBlank(tenTaiKhoan) || isBlank(hoTen) || isBlank(email) || isBlank(matKhau)) {
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
            newUser.setMatKhauMaHoa(PasswordUtil.hash(matKhau));

            nguoiDungDAO.themNguoiDung(newUser, hoTen.trim());
            return ketQuaDangKy.THANH_CONG;
        } catch (SQLException e) {
            System.err.println("[Service] Lỗi SQL khi đăng ký: " + e.getMessage());
            return ketQuaDangKy.LOI_CSDL;
        }
    }

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

        public ketQuaQuenMatKhau getResult() {
            return result;
        }

        public String getOtp() {
            return otp;
        }
    }

    public OtpQuenPassResponse yeuCauOtpQuenMatKhau(String email) {
        if (isBlank(email)) {
            return new OtpQuenPassResponse(ketQuaQuenMatKhau.DU_LIEU_KHONG_HOP_LE, null);
        }

        try {
            if (!nguoiDungDAO.kiemTraEmailTonTai(email.trim())) {
                return new OtpQuenPassResponse(ketQuaQuenMatKhau.EMAIL_KHONG_TON_TAI, null);
            }

            String otp = EmailUtil.generateRandomOTP();
            return EmailUtil.sendOTP(email.trim(), otp)
                    ? new OtpQuenPassResponse(ketQuaQuenMatKhau.THANH_CONG, otp)
                    : new OtpQuenPassResponse(ketQuaQuenMatKhau.LOI_GUI_MAIL, null);
        } catch (SQLException e) {
            System.err.println("[Service] Lỗi SQL khi yêu cầu OTP quên mật khẩu: " + e.getMessage());
            return new OtpQuenPassResponse(ketQuaQuenMatKhau.LOI_CSDL, null);
        }
    }

    public ketQuaQuenMatKhau datLaiMatKhau(String email, String matKhauMoi) {
        if (isBlank(email) || isBlank(matKhauMoi)) {
            return ketQuaQuenMatKhau.DU_LIEU_KHONG_HOP_LE;
        }

        try {
            if (!nguoiDungDAO.kiemTraEmailTonTai(email.trim())) {
                return ketQuaQuenMatKhau.EMAIL_KHONG_TON_TAI;
            }

            nguoiDungDAO.capNhatMatKhauTheoEmail(email.trim(), PasswordUtil.hash(matKhauMoi));
            return ketQuaQuenMatKhau.THANH_CONG;
        } catch (SQLException e) {
            System.err.println("[Service] Lỗi SQL khi đặt lại mật khẩu: " + e.getMessage());
            return ketQuaQuenMatKhau.LOI_CSDL;
        }
    }

    private boolean isActive(String status) {
        String normalized = chuanHoa(status);
        return !normalized.isBlank()
                && !normalized.contains("khong hoat dong")
                && !normalized.contains("ngung hoat dong")
                && !normalized.contains("khoa");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String chuanHoa(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase()
                .replace('đ', 'd')
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
