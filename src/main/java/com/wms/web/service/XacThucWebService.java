package com.wms.web.service;

import com.wms.util.EmailUtil;
import com.wms.util.PasswordUtil;
import com.wms.web.form.DangNhapWebForm;
import com.wms.web.form.DangKyWebForm;
import com.wms.web.form.DatLaiMatKhauWebForm;
import com.wms.web.form.QuenMatKhauWebForm;
import com.wms.web.model.DangKyChoXacThuc;
import com.wms.web.model.NguoiDungPhien;
import com.wms.web.model.YeuCauDatLaiMatKhau;
import com.wms.web.repository.CongThongTinWebRepository;
import com.wms.web.util.WebErrorMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;

@Service
public class XacThucWebService {

    private static final Logger LOGGER = LoggerFactory.getLogger(XacThucWebService.class);

    private final CongThongTinWebRepository khoDuLieu;

    public XacThucWebService(CongThongTinWebRepository khoDuLieu) {
        this.khoDuLieu = khoDuLieu;
    }

    public NguoiDungPhien dangNhap(DangNhapWebForm form) {
        String tenTaiKhoan = form == null || form.getTenTaiKhoan() == null ? "" : form.getTenTaiKhoan().trim();
        String matKhau = form == null ? null : form.getMatKhau();
        if (tenTaiKhoan.isBlank() || matKhau == null || matKhau.isBlank()) {
            logLoginFailure("LOGIN_FAILED_BAD_CREDENTIALS");
            throw new IllegalArgumentException(WebErrorMessages.LOGIN_FAILED_MESSAGE);
        }

        try {
            CongThongTinWebRepository.BanGhiXacThuc record = khoDuLieu.timThongTinXacThuc(tenTaiKhoan);
            if (record == null) {
                logLoginFailure("LOGIN_FAILED_USER_NOT_FOUND");
                throw new IllegalArgumentException(WebErrorMessages.LOGIN_FAILED_MESSAGE);
            }
            if (!isActive(record.trangThaiND())) {
                logLoginFailure("LOGIN_FAILED_INACTIVE");
                throw new IllegalArgumentException(WebErrorMessages.LOGIN_INACTIVE_MESSAGE);
            }
            if (!PasswordUtil.verify(matKhau, record.matKhauMaHoa())) {
                logLoginFailure("LOGIN_FAILED_BAD_PASSWORD");
                throw new IllegalArgumentException(WebErrorMessages.LOGIN_FAILED_MESSAGE);
            }

            khoDuLieu.capNhatLanDangNhapCuoi(record.maND());
            return new NguoiDungPhien(
                    record.maND(),
                    record.maKH(),
                    record.hoTen(),
                    record.tenTaiKhoan(),
                    record.laNhanVien()
            );
        } catch (DataAccessException ex) {
            LOGGER.error("LOGIN_FAILED_SYSTEM_ERROR: {}", ex.getClass().getSimpleName());
            throw new IllegalStateException(WebErrorMessages.LOGIN_SYSTEM_ERROR_MESSAGE, ex);
        }
    }

    private void logLoginFailure(String reason) {
        LOGGER.warn(reason);
    }

    public DangKyChoXacThuc yeuCauOtpDangKy(DangKyWebForm form) {
        validateRegistration(form);

        String otp = EmailUtil.generateRandomOTP();
        boolean sent = EmailUtil.sendOTP(form.getEmail().trim(), otp);
        if (!sent) {
            throw new IllegalArgumentException("Không gửi được OTP. Hãy kiểm tra cấu hình email trong config.properties.");
        }

        return new DangKyChoXacThuc(
                form.getHoTen().trim(),
                form.getTenTaiKhoan().trim(),
                form.getEmail().trim(),
                PasswordUtil.hash(form.getMatKhau()),
                otp,
                LocalDateTime.now().plusMinutes(5)
        );
    }

    public YeuCauDatLaiMatKhau yeuCauOtpDatLaiMatKhau(QuenMatKhauWebForm form) {
        String dinhDanh = form == null || form.getDinhDanh() == null ? "" : form.getDinhDanh().trim();
        if (dinhDanh.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập email hoặc tên tài khoản.");
        }

        try {
            CongThongTinWebRepository.BanGhiXacThuc record = khoDuLieu.timThongTinXacThuc(dinhDanh);
            if (record == null) {
                throw new IllegalArgumentException("Không tìm thấy tài khoản phù hợp.");
            }
            if (!isActive(record.trangThaiND())) {
                throw new IllegalArgumentException(WebErrorMessages.LOGIN_INACTIVE_MESSAGE);
            }
            if (record.email() == null || record.email().isBlank()) {
                throw new IllegalArgumentException("Tài khoản chưa có email để nhận OTP.");
            }

            String otp = EmailUtil.generateRandomOTP();
            boolean sent = EmailUtil.sendPasswordResetOTP(record.email().trim(), otp);
            if (!sent) {
                throw new IllegalArgumentException("Không gửi được OTP. Hãy kiểm tra cấu hình email trong config.properties.");
            }
            return new YeuCauDatLaiMatKhau(
                    record.maND(),
                    record.email().trim(),
                    otp,
                    LocalDateTime.now().plusMinutes(5)
            );
        } catch (DataAccessException ex) {
            LOGGER.error("PASSWORD_RESET_OTP_FAILED_SYSTEM_ERROR: {}", ex.getClass().getSimpleName());
            throw new IllegalStateException("Không thể gửi OTP đặt lại mật khẩu lúc này. Vui lòng thử lại sau.", ex);
        }
    }

    @Transactional
    public void datLaiMatKhau(YeuCauDatLaiMatKhau yeuCau, DatLaiMatKhauWebForm form) {
        if (yeuCau == null) {
            throw new IllegalArgumentException("Phiên đặt lại mật khẩu đã hết hạn. Vui lòng yêu cầu OTP mới.");
        }
        if (yeuCau.daHetHan()) {
            throw new IllegalArgumentException("Mã OTP đã hết hạn. Vui lòng yêu cầu mã mới.");
        }
        if (form == null || !yeuCau.khopMa(form.getOtp())) {
            throw new IllegalArgumentException("Mã OTP không đúng.");
        }
        String matKhauMoi = form.getMatKhauMoi() == null ? "" : form.getMatKhauMoi();
        String xacNhan = form.getXacNhanMatKhauMoi() == null ? "" : form.getXacNhanMatKhauMoi();
        if (matKhauMoi.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập mật khẩu mới.");
        }
        if (matKhauMoi.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu cần ít nhất 6 ký tự.");
        }
        if (!matKhauMoi.equals(xacNhan)) {
            throw new IllegalArgumentException("Mật khẩu nhập lại chưa khớp.");
        }

        try {
            String matKhauCu = khoDuLieu.layMatKhauMaHoaTheoMaND(yeuCau.getMaND());
            if (matKhauCu != null && !matKhauCu.isBlank() && matKhauKhop(matKhauMoi, matKhauCu)) {
                throw new IllegalArgumentException("Mật khẩu mới không được trùng mật khẩu cũ.");
            }
            khoDuLieu.capNhatMatKhau(yeuCau.getMaND(), PasswordUtil.hash(matKhauMoi));
        } catch (DataAccessException ex) {
            LOGGER.error("PASSWORD_RESET_FAILED_SYSTEM_ERROR: {}", ex.getClass().getSimpleName());
            throw new IllegalStateException("Không thể cập nhật mật khẩu lúc này. Vui lòng thử lại sau.", ex);
        }
    }

    @Transactional
    public void hoanTatDangKy(DangKyChoXacThuc DangKyChoXacThuc, String submittedOtp) {
        if (DangKyChoXacThuc == null) {
            throw new IllegalArgumentException("Phiên đăng ký đã hết hạn. Vui lòng đăng ký lại.");
        }
        if (DangKyChoXacThuc.daHetHan()) {
            throw new IllegalArgumentException("Mã OTP đã hết hạn. Vui lòng yêu cầu mã mới.");
        }
        if (!DangKyChoXacThuc.khopMa(submittedOtp)) {
            throw new IllegalArgumentException("Mã OTP không đúng.");
        }

        if (khoDuLieu.tonTaiTenTaiKhoan(DangKyChoXacThuc.getTenTaiKhoan())) {
            throw new IllegalArgumentException("Tên tài khoản đã tồn tại.");
        }
        if (khoDuLieu.tonTaiEmail(DangKyChoXacThuc.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại.");
        }

        khoDuLieu.taoHoiVien(
                DangKyChoXacThuc.getHoTen(),
                DangKyChoXacThuc.getTenTaiKhoan(),
                DangKyChoXacThuc.getEmail(),
                DangKyChoXacThuc.getMatKhauMaHoa()
        );
    }

    private void validateRegistration(DangKyWebForm form) {
        if (!form.getMatKhau().equals(form.getXacNhanMatKhau())) {
            throw new IllegalArgumentException("Mật khẩu nhập lại chưa khớp.");
        }
        if (khoDuLieu.tonTaiTenTaiKhoan(form.getTenTaiKhoan().trim())) {
            throw new IllegalArgumentException("Tên tài khoản đã tồn tại.");
        }
        if (khoDuLieu.tonTaiEmail(form.getEmail().trim())) {
            throw new IllegalArgumentException("Email đã tồn tại.");
        }
    }

    private boolean isActive(String status) {
        String normalized = chuanHoa(status);
        return !normalized.isBlank()
                && !normalized.contains("khong hoat dong")
                && !normalized.contains("ngung hoat dong")
                && !normalized.contains("khoa");
    }

    private boolean matKhauKhop(String plainPassword, String hashedPassword) {
        try {
            return PasswordUtil.verify(plainPassword, hashedPassword);
        } catch (RuntimeException ex) {
            LOGGER.warn("PASSWORD_RESET_OLD_HASH_VERIFY_SKIPPED: {}", ex.getClass().getSimpleName());
            return false;
        }
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
