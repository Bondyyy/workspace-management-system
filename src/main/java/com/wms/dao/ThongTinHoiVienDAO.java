package com.wms.dao;

import com.wms.config.DatabaseConnection;
import com.wms.model.HoiVienDTO;
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
        String sql = "SELECT kh.HoTenKH, nd.SDT, nd.Email, nd.NgaySinh, nd.GioiTinh, nd.ThoiGianTao, h.TenHangThanhVien " +
                     "FROM NGUOIDUNG nd " +
                     "JOIN KHACHHANG kh ON nd.MaND = kh.MaND " +
                     "LEFT JOIN HANGTHANHVIEN h ON kh.MaHangThanhVien = h.MaHangThanhVien " +
                     "WHERE nd.MaND = ?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maND);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                dto.setMaND(maND);
                dto.setHoTen(rs.getString("HoTenKH"));
                dto.setSdt(rs.getString("SDT"));
                dto.setEmail(rs.getString("Email"));
                dto.setNgaySinh(rs.getDate("NgaySinh"));
                dto.setGioiTinh(rs.getString("GioiTinh"));
                dto.setThoiGianTao(rs.getTimestamp("ThoiGianTao"));
                
                String hang = rs.getString("TenHangThanhVien");
                dto.setHangThanhVien(hang != null ? hang : "Thành viên Mới");
            }
        } catch (Exception e) {
            System.err.println("Lỗi lấy thông tin hội viên: " + e.getMessage());
        }
        return dto;
    }

    public boolean capNhatThongTin(HoiVienDTO dto) {
        String sqlKhachHang = "UPDATE KHACHHANG SET HoTenKH = ? WHERE MaND = ?";
        String sqlNguoiDung = "UPDATE NGUOIDUNG SET SDT = ?, NgaySinh = ?, GioiTinh = ? WHERE MaND = ?";
        
        try (Connection conn = getConn()) {
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try (PreparedStatement psKH = conn.prepareStatement(sqlKhachHang);
                 PreparedStatement psND = conn.prepareStatement(sqlNguoiDung)) {
                 
                psKH.setString(1, dto.getHoTen());
                psKH.setString(2, dto.getMaND());
                psKH.executeUpdate();
                
                psND.setString(1, dto.getSdt());
                if (dto.getNgaySinh() != null) {
                    psND.setDate(2, dto.getNgaySinh());
                } else {
                    psND.setNull(2, java.sql.Types.DATE);
                }
                psND.setString(3, dto.getGioiTinh());
                psND.setString(4, dto.getMaND());
                psND.executeUpdate();
                
                conn.commit();
                return true;
            } catch (Exception ex) {
                conn.rollback();
                System.err.println("Lỗi cập nhật: " + ex.getMessage());
            } finally {
                conn.setAutoCommit(autoCommit);
            }
        } catch (Exception e) {
            System.err.println("Lỗi kết nối khi cập nhật thông tin: " + e.getMessage());
        }
        return false;
    }

    public boolean doiMatKhau(String maND, String matKhauCu, String matKhauMoi) {
        String sqlSelect = "SELECT MatKhauMaHoa FROM NGUOIDUNG WHERE MaND = ?";
        String sqlUpdate = "UPDATE NGUOIDUNG SET MatKhauMaHoa = ? WHERE MaND = ?";
        
        try (Connection conn = getConn()) {
            String hashCu = "";
            try (PreparedStatement psSel = conn.prepareStatement(sqlSelect)) {
                psSel.setString(1, maND);
                ResultSet rs = psSel.executeQuery();
                if (rs.next()) {
                    hashCu = rs.getString("MatKhauMaHoa");
                }
            }
            
            if (!PasswordUtil.verify(matKhauCu, hashCu)) {
                return false; 
            }
            
            try (PreparedStatement psUpd = conn.prepareStatement(sqlUpdate)) {
                psUpd.setString(1, PasswordUtil.hash(matKhauMoi));
                psUpd.setString(2, maND);
                psUpd.executeUpdate();
                return true;
            }
        } catch (Exception e) {
            System.err.println("Lỗi đổi mật khẩu: " + e.getMessage());
        }
        return false;
    }
}
