package com.wms.web.service;

import com.wms.util.PasswordUtil;
import com.wms.web.form.DangNhapWebForm;
import com.wms.web.model.NguoiDungPhien;
import com.wms.web.repository.CongThongTinWebRepository;
import com.wms.web.util.WebErrorMessages;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class XacThucWebServiceTest {

    @Test
    void dangNhapKhongTonTaiTraVeThongBaoChung() {
        CongThongTinWebRepository repository = mock(CongThongTinWebRepository.class);
        when(repository.timThongTinXacThuc("missing")).thenReturn(null);

        XacThucWebService service = new XacThucWebService(repository);

        assertThatThrownBy(() -> service.dangNhap(form("missing", "any-password")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(WebErrorMessages.LOGIN_FAILED_MESSAGE);
        verify(repository, never()).capNhatLanDangNhapCuoi(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void dangNhapSaiMatKhauTraVeThongBaoChung() {
        CongThongTinWebRepository repository = mock(CongThongTinWebRepository.class);
        when(repository.timThongTinXacThuc("user")).thenReturn(record("ND01", PasswordUtil.hash("correct"), "Đang hoạt động"));

        XacThucWebService service = new XacThucWebService(repository);

        assertThatThrownBy(() -> service.dangNhap(form("user", "wrong-password")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(WebErrorMessages.LOGIN_FAILED_MESSAGE);
        verify(repository, never()).capNhatLanDangNhapCuoi(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void dangNhapTaiKhoanKhongHoatDongTraVeThongBaoRieng() {
        CongThongTinWebRepository repository = mock(CongThongTinWebRepository.class);
        when(repository.timThongTinXacThuc("locked")).thenReturn(record("ND02", PasswordUtil.hash("correct"), "Tài khoản khóa"));

        XacThucWebService service = new XacThucWebService(repository);

        assertThatThrownBy(() -> service.dangNhap(form("locked", "correct")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(WebErrorMessages.LOGIN_INACTIVE_MESSAGE);
        verify(repository, never()).capNhatLanDangNhapCuoi(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void dangNhapThanhCongCapNhatLanDangNhapCuoi() {
        CongThongTinWebRepository repository = mock(CongThongTinWebRepository.class);
        when(repository.timThongTinXacThuc("user")).thenReturn(record("ND03", PasswordUtil.hash("correct"), "Đang hoạt động"));

        XacThucWebService service = new XacThucWebService(repository);

        NguoiDungPhien user = service.dangNhap(form("user", "correct"));

        assertThat(user.getMaND()).isEqualTo("ND03");
        verify(repository).capNhatLanDangNhapCuoi("ND03");
    }

    @Test
    void dangNhapLoiDbTraVeThongBaoHeThongThanThien() {
        CongThongTinWebRepository repository = mock(CongThongTinWebRepository.class);
        when(repository.timThongTinXacThuc("user"))
                .thenThrow(new DataAccessResourceFailureException("ORA-12541: cannot connect"));

        XacThucWebService service = new XacThucWebService(repository);

        assertThatThrownBy(() -> service.dangNhap(form("user", "correct")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(WebErrorMessages.LOGIN_SYSTEM_ERROR_MESSAGE);
    }

    private DangNhapWebForm form(String username, String password) {
        DangNhapWebForm form = new DangNhapWebForm();
        form.setTenTaiKhoan(username);
        form.setMatKhau(password);
        return form;
    }

    private CongThongTinWebRepository.BanGhiXacThuc record(String maND, String hashedPassword, String status) {
        return new CongThongTinWebRepository.BanGhiXacThuc(
                maND,
                "Người dùng",
                "user",
                "user@example.com",
                hashedPassword,
                status,
                "KH01",
                null,
                null,
                null
        );
    }
}
