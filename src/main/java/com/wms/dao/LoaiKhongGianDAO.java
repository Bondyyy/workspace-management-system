package com.wms.dao;

import com.wms.config.DatabaseConnection;
import com.wms.model.LoaiKhongGianDTO;

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
            sql = "SELECT MaLoaiKG, TenLoaiKG, SucChua, DonGiaTheoGio "
                + "FROM LOAIKHONGGIAN ORDER BY MaLoaiKG";
        } else {
            sql = "SELECT MaLoaiKG, TenLoaiKG, SucChua, DonGiaTheoGio "
                + "FROM LOAIKHONGGIAN "
                + "WHERE UPPER(TenLoaiKG) LIKE UPPER(?) OR UPPER(MaLoaiKG) LIKE UPPER(?) "
                + "ORDER BY MaLoaiKG";
        }

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
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
        // 1. Sử dụng mã từ DTO nếu có, nếu không thì tự sinh
        if (dto.getMaLoaiKG() == null || dto.getMaLoaiKG().trim().isEmpty()) {
            dto.setMaLoaiKG(taoMaMoi());
        }

        String sql = "INSERT INTO LOAIKHONGGIAN (MaLoaiKG, TenLoaiKG, SucChua, DonGiaTheoGio) "
                   + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, dto.getMaLoaiKG());
            ps.setString(2, dto.getTenLoaiKG());
            if (dto.getSucChua() != null) {
                ps.setInt(3, dto.getSucChua());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            if (dto.getDonGiaTheoGio() != null) {
                ps.setDouble(4, dto.getDonGiaTheoGio());
            } else {
                ps.setNull(4, Types.DOUBLE);
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[LoaiKhongGianDAO] Lỗi thêm: " + e.getMessage());
            return false;
        }
    }

    public boolean capNhat(LoaiKhongGianDTO dto) {
        String sql = "UPDATE LOAIKHONGGIAN "
                   + "SET TenLoaiKG = ?, SucChua = ?, DonGiaTheoGio = ? "
                   + "WHERE MaLoaiKG = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, dto.getTenLoaiKG());
            if (dto.getSucChua() != null) {
                ps.setInt(2, dto.getSucChua());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            if (dto.getDonGiaTheoGio() != null) {
                ps.setDouble(3, dto.getDonGiaTheoGio());
            } else {
                ps.setNull(3, Types.DOUBLE);
            }
            ps.setString(4, dto.getMaLoaiKG());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[LoaiKhongGianDAO] Lỗi cập nhật: " + e.getMessage());
            return false;
        }
    }

    public boolean xoa(String maLoaiKG) {
        // Kiểm tra còn không gian nào thuộc loại này không
        String sqlCheck = "SELECT COUNT(*) FROM KHONGGIAN WHERE MaLoaiKG = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sqlCheck)) {
            ps.setString(1, maLoaiKG);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.err.println("[LoaiKhongGianDAO] Không thể xóa: có không gian đang sử dụng loại này.");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("[LoaiKhongGianDAO] Lỗi kiểm tra ràng buộc: " + e.getMessage());
            return false;
        }

        String sql = "DELETE FROM LOAIKHONGGIAN WHERE MaLoaiKG = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maLoaiKG);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[LoaiKhongGianDAO] Lỗi xóa: " + e.getMessage());
            return false;
        }
    }

    public LoaiKhongGianDTO layTheoMa(String maLoaiKG) {
        String sql = "SELECT MaLoaiKG, TenLoaiKG, SucChua, DonGiaTheoGio "
                   + "FROM LOAIKHONGGIAN WHERE MaLoaiKG = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maLoaiKG);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("[LoaiKhongGianDAO] Lỗi lấy theo mã: " + e.getMessage());
        }
        return null;
    }

    private LoaiKhongGianDTO mapRow(ResultSet rs) throws SQLException {
        LoaiKhongGianDTO dto = new LoaiKhongGianDTO();
        dto.setMaLoaiKG(rs.getString("MaLoaiKG"));
        dto.setTenLoaiKG(rs.getString("TenLoaiKG"));
        int sucChua = rs.getInt("SucChua");
        dto.setSucChua(rs.wasNull() ? null : sucChua);
        double donGia = rs.getDouble("DonGiaTheoGio");
        dto.setDonGiaTheoGio(rs.wasNull() ? null : donGia);
        return dto;
    }

    public String taoMaMoi() {
        return "LKG" + (System.currentTimeMillis() % 1000000);
    }
}
