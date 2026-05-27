package com.wms.service.TrangChuQuanLy.QuanLyNguoiDung;

import com.wms.config.AppConstants;
import com.wms.dao.TrangChuQuanLy.QuanLyNguoiDung.NguoiDungDAO;
import com.wms.model.TrangChuQuanLy.QuanLyNguoiDung.NguoiDungDTO;

import java.sql.SQLException;
import java.text.Normalizer;
import java.util.List;

public class NguoiDungService {
    private final NguoiDungDAO nguoiDungDAO;

    public NguoiDungService() {
        this.nguoiDungDAO = new NguoiDungDAO();
    }

    public List<NguoiDungDTO> getAllUsers() throws SQLException {
        return nguoiDungDAO.getAllNguoiDung();
    }

    public List<NguoiDungDTO> searchUsers(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllUsers();
        }
        return nguoiDungDAO.searchNguoiDung(keyword.trim());
    }

    public void updateUser(NguoiDungDTO user) throws SQLException {
        updateUser(user, null);
    }

    public void updateUser(NguoiDungDTO user, NguoiDungDTO actor) throws SQLException {
        // Validation logic
        if (user.getHoTen() == null || user.getHoTen().trim().isEmpty()) {
            throw new IllegalArgumentException("Họ tên không được để trống!");
        }
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            throw new IllegalArgumentException("Email không hợp lệ!");
        }
        if (user.getSdt() == null || user.getSdt().length() < 10) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ!");
        }

        NguoiDungDTO current = nguoiDungDAO.layNguoiDungTheoMa(user.getMaND());
        if (current == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng cần cập nhật!");
        }
        kiemTraQuyenCapNhat(user, current, actor);
        
        nguoiDungDAO.updateNguoiDung(user);
    }

    public void addUser(NguoiDungDTO user) throws SQLException {
        if (nguoiDungDAO.kiemTraTaiKhoanTonTai(user.getTenTaiKhoan())) {
            throw new IllegalArgumentException("Tên tài khoản đã tồn tại!");
        }
        if (nguoiDungDAO.kiemTraEmailTonTai(user.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại!");
        }
        if (nguoiDungDAO.kiemTraSdtTonTai(user.getSdt())) {
            throw new IllegalArgumentException("Số điện thoại đã tồn tại!");
        }
        
        // Mặc định mật khẩu là 123456 nếu không set
        if (user.getMatKhauMaHoa() == null) {
            user.setMatKhauMaHoa(com.wms.util.PasswordUtil.hash("123456"));
        }
        
        nguoiDungDAO.themNguoiDung(user, user.getHoTen());
    }

    public String generateNextMaND() throws SQLException {
        return nguoiDungDAO.generateNextMaND();
    }

    public List<com.wms.model.TrangChuQuanLy.QuanLyVaiTro.VaiTroDTO> layTatCaVaiTro() {
        return new com.wms.dao.TrangChuQuanLy.QuanLyVaiTro.VaiTroDAO().layTatCaVaiTro();
    }

    private void kiemTraQuyenCapNhat(NguoiDungDTO incoming, NguoiDungDTO current, NguoiDungDTO actor) {
        if (actor == null || actor.getMaND() == null || actor.getMaND().equals(current.getMaND())) {
            return;
        }

        boolean doiMatKhau = incoming.getMatKhauMaHoa() != null && !incoming.getMatKhauMaHoa().isBlank();
        boolean doiTrangThai = !normalize(current.getTrangThaiND()).equals(normalize(incoming.getTrangThaiND()));
        boolean khoaTaiKhoan = !isLocked(current.getTrangThaiND()) && isLocked(incoming.getTrangThaiND());
        boolean doiVaiTro = !firstRoleCode(incoming).equals(firstRoleCode(current));

        if (isAdmin(actor)) {
            if (isAdmin(current) && (doiMatKhau || khoaTaiKhoan || doiVaiTro)) {
                throw new IllegalArgumentException("Quản trị viên hệ thống không được sửa mật khẩu, khóa hoặc đổi quyền người dùng cùng cấp.");
            }
            return;
        }

        if (isManager(actor)) {
            if (doiMatKhau) {
                throw new IllegalArgumentException("Quản lý chi nhánh không được sửa mật khẩu người dùng.");
            }
            if (doiVaiTro) {
                throw new IllegalArgumentException("Quản lý chi nhánh không được thay đổi nhóm quyền người dùng.");
            }
            if (doiTrangThai && !duocQuanLyKhoa(current, actor)) {
                throw new IllegalArgumentException("Bạn không có quyền thay đổi trạng thái tài khoản này.");
            }
            return;
        }

        if (doiMatKhau || doiTrangThai || doiVaiTro) {
            throw new IllegalArgumentException("Bạn không có quyền sửa mật khẩu, khóa tài khoản hoặc thay đổi nhóm quyền người dùng.");
        }
    }

    private boolean duocQuanLyKhoa(NguoiDungDTO target, NguoiDungDTO actor) {
        if (isAdmin(target) || isManager(target)) {
            return false;
        }
        if (isCustomer(target)) {
            return true;
        }
        return target.getMaCN() != null && actor.getMaCN() != null
                && target.getMaCN().equalsIgnoreCase(actor.getMaCN());
    }

    private boolean isAdmin(NguoiDungDTO user) {
        return hasRole(user, AppConstants.ROLE_ADMIN_CODE, AppConstants.ROLE_ADMIN_NAME);
    }

    private boolean isManager(NguoiDungDTO user) {
        return hasRole(user, AppConstants.ROLE_MANAGER_CODE, AppConstants.ROLE_MANAGER_NAME);
    }

    private boolean isCustomer(NguoiDungDTO user) {
        return hasRole(user, AppConstants.ROLE_CUSTOMER_CODE, AppConstants.ROLE_CUSTOMER_NAME);
    }

    private boolean hasRole(NguoiDungDTO user, String code, String name) {
        if (user == null || user.getVaiTro() == null) {
            return false;
        }
        String normalizedName = normalize(name);
        for (String role : user.getVaiTro()) {
            if (role == null) {
                continue;
            }
            if (role.equalsIgnoreCase(code) || normalize(role).equals(normalizedName)) {
                return true;
            }
        }
        return false;
    }

    private String firstRoleCode(NguoiDungDTO user) {
        if (user == null || user.getVaiTro() == null || user.getVaiTro().isEmpty()) {
            return AppConstants.ROLE_CUSTOMER_CODE;
        }
        return user.getVaiTro().get(0) == null ? AppConstants.ROLE_CUSTOMER_CODE : user.getVaiTro().get(0);
    }

    private boolean isLocked(String status) {
        String value = normalize(status);
        return value.contains("khoa") || value.contains("khong hoat dong") || value.contains("ngung hoat dong");
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String noDiacritics = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return noDiacritics.toLowerCase().trim();
    }
}
