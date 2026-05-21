package com.wms.web.service;

import com.wms.util.EmailUtil;
import com.wms.util.PasswordUtil;
import com.wms.web.form.DangNhapWebForm;
import com.wms.web.form.DangKyWebForm;
import com.wms.web.model.DangKyChoXacThuc;
import com.wms.web.model.NguoiDungPhien;
import com.wms.web.repository.CongThongTinWebRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;

@Service
public class XacThucWebService {

    private final CongThongTinWebRepository khoDuLieu;

    public XacThucWebService(CongThongTinWebRepository khoDuLieu) {
        this.khoDuLieu = khoDuLieu;
    }

    public NguoiDungPhien dangNhap(DangNhapWebForm form) {
        CongThongTinWebRepository.BanGhiXacThuc record = khoDuLieu.timThongTinXacThuc(form.getTenTaiKhoan().trim());
        if (record == null) {
            throw new IllegalArgumentException("Không tìm thấy tài khoản.");
        }
        if (!isActive(record.trangThaiND())) {
            throw new IllegalArgumentException("Tài khoản hiện không hoạt động.");
        }
        if (!PasswordUtil.verify(form.getMatKhau(), record.matKhauMaHoa())) {
            throw new IllegalArgumentException("Mật khẩu không đúng.");
        }

        khoDuLieu.capNhatLanDangNhapCuoi(record.maND());
        return new NguoiDungPhien(
                record.maND(),
                record.maKH(),
                record.hoTen(),
                record.tenTaiKhoan(),
                record.laNhanVien()
        );
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
