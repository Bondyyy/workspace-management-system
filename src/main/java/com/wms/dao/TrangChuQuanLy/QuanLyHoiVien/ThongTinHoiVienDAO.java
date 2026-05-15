package com.wms.dao.TrangChuQuanLy.QuanLyHoiVien;

import com.wms.config.DatabaseConnection;
import com.wms.model.TrangChuQuanLy.QuanLyHoiVien.HoiVienDTO;
import com.wms.util.PasswordUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ThongTinHoiVienDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public HoiVienDTO layThongTin(String maND) {
        HoiVienDTO dto = new HoiVienDTO();
        String sql = "SELECT nd.HoTen, nd.SDT, nd.Email, nd.NgaySinh, nd.GioiTinh, nd.ThoiGianTao, nd.AnhDaiDien, h.TenHangThanhVien " +
                     "FROM NGUOIDUNG nd " +
                     "LEFT JOIN KHACHHANG kh ON nd.MaND = kh.MaND " +
                     "LEFT JOIN HANGTHANHVIEN h ON kh.MaHangThanhVien = h.MaHangThanhVien " +
                     "WHERE nd.MaND = ?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maND);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dto.setMaND(maND);
                    dto.setHoTen(rs.getString("HoTen"));
                    dto.setSdt(rs.getString("SDT"));
                    dto.setEmail(rs.getString("Email"));
                    dto.setNgaySinh(rs.getDate("NgaySinh"));
                    dto.setGioiTinh(rs.getString("GioiTinh"));
                    dto.setThoiGianTao(rs.getTimestamp("ThoiGianTao"));
                    dto.setAnhDaiDien(rs.getBytes("AnhDaiDien"));
                    String hang = rs.getString("TenHangThanhVien");
                    dto.setHangThanhVien(hang != null ? hang : "Đồng");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dto;
    }

    public boolean capNhatThongTin(HoiVienDTO dto) {
        String sqlND = "UPDATE NGUOIDUNG SET HoTen = ?, SDT = ?, NgaySinh = ?, GioiTinh = ?, AnhDaiDien = ? WHERE MaND = ?";
        
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sqlND)) {
            ps.setString(1, dto.getHoTen());
            ps.setString(2, dto.getSdt());
            ps.setDate(3, dto.getNgaySinh());
            ps.setString(4, dto.getGioiTinh());
            ps.setBytes(5, dto.getAnhDaiDien());
            ps.setString(6, dto.getMaND());
            
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean doiMatKhau(String maND, String matKhauCu, String matKhauMoi) {
        String sqlSel = "SELECT MatKhauMaHoa FROM NGUOIDUNG WHERE MaND = ?";
        String sqlUpd = "UPDATE NGUOIDUNG SET MatKhauMaHoa = ? WHERE MaND = ?";
        
        try (Connection conn = getConn()) {
            String hashCu = "";
            try (PreparedStatement ps = conn.prepareStatement(sqlSel)) {
                ps.setString(1, maND);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) hashCu = rs.getString("MatKhauMaHoa");
                }
            }
            if (!PasswordUtil.verify(matKhauCu, hashCu)) return false;
            
            try (PreparedStatement ps = conn.prepareStatement(sqlUpd)) {
                ps.setString(1, PasswordUtil.hash(matKhauMoi));
                ps.setString(2, maND);
                ps.executeUpdate();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
