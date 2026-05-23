package com.wms.dao.TrangChuQuanLy.QuanLyKhongGian;

import com.wms.config.DatabaseConnection;
import com.wms.model.TrangChuQuanLy.QuanLyKhongGian.LoaiKhongGianDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoaiKhongGianDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<LoaiKhongGianDTO> layTatCaLoaiKhongGian() {
        return timKiemLoaiKhongGian(null);
    }

    public List<LoaiKhongGianDTO> timKiemLoaiKhongGian(String tuKhoa) {
        List<LoaiKhongGianDTO> list = new ArrayList<>();
        String sql;
        if (tuKhoa == null || tuKhoa.isBlank()) {
            sql = "SELECT MaLoaiKG, TenLoaiKG, SucChua, DonGiaTheoGio, TrangThai "
                + "FROM LOAIKHONGGIAN ORDER BY MaLoaiKG";
        } else {
            sql = "SELECT MaLoaiKG, TenLoaiKG, SucChua, DonGiaTheoGio, TrangThai "
                + "FROM LOAIKHONGGIAN "
                + "WHERE UPPER(TenLoaiKG) LIKE UPPER(?) OR UPPER(MaLoaiKG) LIKE UPPER(?) "
                + "ORDER BY MaLoaiKG";
        }

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (tuKhoa != null && !tuKhoa.isBlank()) {
                String pattern = "%" + tuKhoa + "%";
                ps.setString(1, pattern);
                ps.setString(2, pattern);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[LoaiKhongGianDAO] Lỗi tìm kiếm: " + e.getMessage());
        }
        return list;
    }

    public boolean them(LoaiKhongGianDTO dto) {
        String sql = "INSERT INTO LOAIKHONGGIAN (TenLoaiKG, SucChua, DonGiaTheoGio, TrangThai) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dto.getTenLoaiKG());
            if (dto.getSucChua() != null) ps.setInt(2, dto.getSucChua());
            else ps.setNull(2, Types.INTEGER);
            if (dto.getDonGiaTheoGio() != null) ps.setDouble(3, dto.getDonGiaTheoGio());
            else ps.setNull(3, Types.DOUBLE);
            ps.setString(4, dto.getTrangThai() != null ? dto.getTrangThai() : "Đang hoạt động");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[LoaiKhongGianDAO] Lỗi thêm: " + e.getMessage());
            return false;
        }
    }

    public boolean capNhat(LoaiKhongGianDTO dto) {
        String sql = "UPDATE LOAIKHONGGIAN SET TenLoaiKG = ?, SucChua = ?, DonGiaTheoGio = ?, TrangThai = ? WHERE MaLoaiKG = ?";
        boolean autoCommit = true;
        try (Connection conn = getConn()) {
            autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, dto.getTenLoaiKG());
                if (dto.getSucChua() != null) ps.setInt(2, dto.getSucChua());
                else ps.setNull(2, Types.INTEGER);
                if (dto.getDonGiaTheoGio() != null) ps.setDouble(3, dto.getDonGiaTheoGio());
                else ps.setNull(3, Types.DOUBLE);
                
                String trangThai = dto.getTrangThai() != null ? dto.getTrangThai() : "Đang hoạt động";
                ps.setString(4, trangThai);
                ps.setString(5, dto.getMaLoaiKG());
                boolean success = ps.executeUpdate() > 0;
                
                // Nếu trạng thái là Ngừng hoạt động, gỡ tất cả các không gian của loại này khỏi sơ đồ
                if (success && "Ngừng hoạt động".equals(trangThai)) {
                    String sqlRemoveSpace = "UPDATE KHONGGIAN SET ToaDoX = NULL, ToaDoY = NULL WHERE MaLoaiKG = ?";
                    try (PreparedStatement psRemove = conn.prepareStatement(sqlRemoveSpace)) {
                        psRemove.setString(1, dto.getMaLoaiKG());
                        psRemove.executeUpdate();
                    }
                }
                
                conn.commit();
                return success;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autoCommit);
            }
        } catch (SQLException e) {
            System.err.println("[LoaiKhongGianDAO] Lỗi cập nhật: " + e.getMessage());
            return false;
        }
    }

    public boolean xoa(String maLoaiKG) {
        String sql = "DELETE FROM LOAIKHONGGIAN WHERE MaLoaiKG = ?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maLoaiKG);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[LoaiKhongGianDAO] Lỗi xóa: " + e.getMessage());
            return false;
        }
    }

    public LoaiKhongGianDTO layTheoMa(String maLoaiKG) {
        String sql = "SELECT MaLoaiKG, TenLoaiKG, SucChua, DonGiaTheoGio, TrangThai FROM LOAIKHONGGIAN WHERE MaLoaiKG = ?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maLoaiKG);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("[LoaiKhongGianDAO] Lỗi lấy theo mã: " + e.getMessage());
        }
        return null;
    }

    public int demSoLuong() {
        String sql = "SELECT COUNT(*) FROM LOAIKHONGGIAN";
        try (Connection conn = getConn();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[LoaiKhongGianDAO] Lỗi đếm số lượng: " + e.getMessage());
        }
        return 0;
    }

    public String taoMaMoi() {
        return "";
    }

    private LoaiKhongGianDTO mapRow(ResultSet rs) throws SQLException {
        LoaiKhongGianDTO dto = new LoaiKhongGianDTO();
        dto.setMaLoaiKG(rs.getString("MaLoaiKG"));
        dto.setTenLoaiKG(rs.getString("TenLoaiKG"));
        int sucChua = rs.getInt("SucChua");
        dto.setSucChua(rs.wasNull() ? null : sucChua);
        double donGia = rs.getDouble("DonGiaTheoGio");
        dto.setDonGiaTheoGio(rs.wasNull() ? null : donGia);
        dto.setTrangThai(rs.getString("TrangThai"));
        return dto;
    }
}
