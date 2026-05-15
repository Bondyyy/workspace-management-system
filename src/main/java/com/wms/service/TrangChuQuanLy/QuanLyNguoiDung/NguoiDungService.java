package com.wms.service.TrangChuQuanLy.QuanLyNguoiDung;

import com.wms.dao.TrangChuQuanLy.QuanLyNguoiDung.NguoiDungDAO;
import com.wms.model.TrangChuQuanLy.QuanLyNguoiDung.NguoiDungDTO;

import java.sql.SQLException;
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
}
