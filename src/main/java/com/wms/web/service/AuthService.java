package com.wms.web.service;

import com.wms.util.EmailUtil;
import com.wms.util.PasswordUtil;
import com.wms.web.form.LoginForm;
import com.wms.web.form.RegisterForm;
import com.wms.web.model.PendingRegistration;
import com.wms.web.model.SessionUser;
import com.wms.web.repository.WebPortalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;

@Service
public class AuthService {

    private final WebPortalRepository repository;

    public AuthService(WebPortalRepository repository) {
        this.repository = repository;
    }

    public SessionUser login(LoginForm form) {
        WebPortalRepository.AuthRecord record = repository.findAuthRecord(form.getUsername().trim());
        if (record == null) {
            throw new IllegalArgumentException("Không tìm thấy tài khoản.");
        }
        if (!isActive(record.trangThaiND())) {
            throw new IllegalArgumentException("Tài khoản hiện không hoạt động.");
        }
        if (!PasswordUtil.verify(form.getPassword(), record.matKhauMaHoa())) {
            throw new IllegalArgumentException("Mật khẩu không đúng.");
        }

        repository.updateLastLogin(record.maND());
        return new SessionUser(
                record.maND(),
                record.maKH(),
                record.hoTen(),
                record.tenTaiKhoan(),
                record.isStaff()
        );
    }

    public PendingRegistration requestRegistrationOtp(RegisterForm form) {
        validateRegistration(form);

        String otp = EmailUtil.generateRandomOTP();
        boolean sent = EmailUtil.sendOTP(form.getEmail().trim(), otp);
        if (!sent) {
            throw new IllegalArgumentException("Không gửi được OTP. Hãy kiểm tra cấu hình email trong config.properties.");
        }

        return new PendingRegistration(
                form.getFullName().trim(),
                form.getUsername().trim(),
                form.getEmail().trim(),
                PasswordUtil.hash(form.getPassword()),
                otp,
                LocalDateTime.now().plusMinutes(5)
        );
    }

    @Transactional
    public void completeRegistration(PendingRegistration pendingRegistration, String submittedOtp) {
        if (pendingRegistration == null) {
            throw new IllegalArgumentException("Phiên đăng ký đã hết hạn. Vui lòng đăng ký lại.");
        }
        if (pendingRegistration.isExpired()) {
            throw new IllegalArgumentException("Mã OTP đã hết hạn. Vui lòng yêu cầu mã mới.");
        }
        if (!pendingRegistration.matches(submittedOtp)) {
            throw new IllegalArgumentException("Mã OTP không đúng.");
        }

        if (repository.usernameExists(pendingRegistration.getUsername())) {
            throw new IllegalArgumentException("Tên tài khoản đã tồn tại.");
        }
        if (repository.emailExists(pendingRegistration.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại.");
        }

        repository.createMember(
                pendingRegistration.getFullName(),
                pendingRegistration.getUsername(),
                pendingRegistration.getEmail(),
                pendingRegistration.getHashedPassword()
        );
    }

    private void validateRegistration(RegisterForm form) {
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu nhập lại chưa khớp.");
        }
        if (repository.usernameExists(form.getUsername().trim())) {
            throw new IllegalArgumentException("Tên tài khoản đã tồn tại.");
        }
        if (repository.emailExists(form.getEmail().trim())) {
            throw new IllegalArgumentException("Email đã tồn tại.");
        }
    }

    private boolean isActive(String status) {
        String normalized = normalize(status);
        return !normalized.isBlank()
                && !normalized.contains("khong hoat dong")
                && !normalized.contains("ngung hoat dong")
                && !normalized.contains("khoa");
    }

    private String normalize(String value) {
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
