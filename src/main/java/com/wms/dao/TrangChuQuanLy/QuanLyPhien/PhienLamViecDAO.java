package com.wms.dao.TrangChuQuanLy.QuanLyPhien;

import com.wms.config.DatabaseConnection;
import com.wms.model.TrangChuQuanLy.QuanLyPhien.DichVuTrongPhienDTO;
import com.wms.model.TrangChuQuanLy.QuanLyPhien.PhienLamViecFullDTO;
import com.wms.model.TrangChuQuanLy.QuanLyPhien.PhienLamViecDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PhienLamViecDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public boolean taoPhienLamViecMoi(PhienLamViecDTO phien) {
        String sql = "INSERT INTO PHIENLAMVIEC (MaPhien, ThoiGianBatDau, ThoiGianDuKienKetThuc, TrangThaiPhien, MaKG, MaKH, MaDatCho, CapNhatLanCuoi) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phien.getMaPhien());
            pstmt.setTimestamp(2, phien.getThoiGianBatDau());
            pstmt.setTimestamp(3, phien.getThoiGianDuKienKetThuc());
            pstmt.setString(4, phien.getTrangThaiPhien());
            pstmt.setString(5, phien.getMaKG());
            pstmt.setString(6, phien.getMaKH());
            pstmt.setString(7, phien.getMaDatCho());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PhienLamViecDAO] Lỗi khi tạo phiên mới: " + e.getMessage());
            return false;
        }
    }

    public List<PhienLamViecFullDTO> layDanhSachPhien(String keyword, String maCN) {
        List<PhienLamViecFullDTO> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT p.*, kg.TenKG, kh.HoTenKH, dc.TrangThaiDatTruoc, h.TrangThaiThanhToan, lkg.DonGiaTheoGio " +
                "FROM PHIENLAMVIEC p " +
                "JOIN KHONGGIAN kg ON p.MaKG = kg.MaKG " +
                "JOIN LOAIKHONGGIAN lkg ON kg.MaLoaiKG = lkg.MaLoaiKG " +
                "JOIN KHACHHANG kh ON p.MaKH = kh.MaKH " +
                "LEFT JOIN DATCHO dc ON p.MaDatCho = dc.MaDatCho " +
                "LEFT JOIN HOADON h ON p.MaPhien = h.MaPhien " +
                "WHERE (p.MaPhien LIKE ? OR kh.HoTenKH LIKE ? OR kg.TenKG LIKE ?)");

        if (maCN != null && !maCN.isEmpty()) {
            sql.append(" AND kg.MaCN = ?");
        }
        sql.append(" ORDER BY p.ThoiGianBatDau DESC");

        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            String search = "%" + (keyword == null ? "" : keyword) + "%";
            pstmt.setString(1, search);
            pstmt.setString(2, search);
            pstmt.setString(3, search);
            if (maCN != null && !maCN.isEmpty()) {
                pstmt.setString(4, maCN);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    PhienLamViecFullDTO dto = new PhienLamViecFullDTO();
                    dto.setMaPhien(rs.getString("MaPhien"));
                    dto.setThoiGianBatDau(rs.getTimestamp("ThoiGianBatDau"));
                    dto.setThoiGianDuKienKetThuc(rs.getTimestamp("ThoiGianDuKienKetThuc"));
                    dto.setThoiGianKetThuc(rs.getTimestamp("ThoiGianKetThuc"));
                    dto.setTrangThaiPhien(rs.getString("TrangThaiPhien"));
                    dto.setMaKG(rs.getString("MaKG"));
                    dto.setMaKH(rs.getString("MaKH"));
                    dto.setMaDatCho(rs.getString("MaDatCho"));
                    dto.setDonGiaTheoGio(rs.getDouble("DonGiaTheoGio"));
                    dto.setTenKhongGian(rs.getString("TenKG"));
                    dto.setTenKhachHang(rs.getString("HoTenKH"));
                    dto.setTrangThaiDatCho(rs.getString("TrangThaiDatTruoc"));
                    dto.setTrangThaiThanhToan(rs.getString("TrangThaiThanhToan"));
                    list.add(dto);
                }
            }
        } catch (SQLException e) {
            System.err.println("[PhienLamViecDAO] Lỗi lấy danh sách: " + e.getMessage());
        }
        return list;
    }

    public boolean capNhatPhien(String maPhien, String trangThai, String tenKH) {
        String sqlPhien = "UPDATE PHIENLAMVIEC SET TrangThaiPhien = ? WHERE MaPhien = ?";
        String sqlKH = "UPDATE KHACHHANG kh SET HoTenKH = ? WHERE MaKH = (SELECT MaKH FROM PHIENLAMVIEC WHERE MaPhien = ?)";

        try (Connection conn = getConn()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement pstmt = conn.prepareStatement(sqlPhien)) {
                    pstmt.setString(1, trangThai);
                    pstmt.setString(2, maPhien);
                    pstmt.executeUpdate();
                }
                if (tenKH != null && !tenKH.isEmpty()) {
                    try (PreparedStatement pstmt = conn.prepareStatement(sqlKH)) {
                        pstmt.setString(1, tenKH);
                        pstmt.setString(2, maPhien);
                        pstmt.executeUpdate();
                    }
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("[PhienLamViecDAO] Lỗi cập nhật phiên: " + e.getMessage());
            return false;
        }
    }

    public boolean ketThucPhien(String maPhien) {
        String sqlPhien = "UPDATE PHIENLAMVIEC SET ThoiGianKetThuc = CURRENT_TIMESTAMP, TrangThaiPhien = 'Đã kết thúc', CapNhatLanCuoi = CURRENT_TIMESTAMP WHERE MaPhien = ?";
        
        try (Connection conn = getConn()) {
            conn.setAutoCommit(false);
            try {
                // 1. Cập nhật phiên (Trigger sẽ tự động đổi trạng thái Không gian thành Trống)
                try (PreparedStatement pstmtPhien = conn.prepareStatement(sqlPhien)) {
                    pstmtPhien.setString(1, maPhien);
                    if (pstmtPhien.executeUpdate() == 0) {
                        conn.rollback();
                        return false;
                    }
                }
                
                // 2. Tính toán và cập nhật hóa đơn thủ công (tránh lỗi thiếu hàm/procedure trong Oracle)
                double tongTien = tinhTongTienPhien(conn, maPhien);
                
                String sqlHoaDon = "UPDATE HOADON SET TongTien = ?, ThanhTien = ?, NgayLapHoaDon = CURRENT_TIMESTAMP WHERE MaPhien = ?";
                try (PreparedStatement pstmtHoaDon = conn.prepareStatement(sqlHoaDon)) {
                    pstmtHoaDon.setDouble(1, tongTien);
                    pstmtHoaDon.setDouble(2, tongTien);
                    pstmtHoaDon.setString(3, maPhien);
                    pstmtHoaDon.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("[PhienLamViecDAO] Lỗi kết thúc phiên: " + e.getMessage());
            return false;
        }
    }

    private double tinhTongTienPhien(Connection conn, String maPhien) throws SQLException {
        double tienKhongGian = 0;
        double tienDichVu = 0;

        String sqlKg = "SELECT LKG.DonGiaTheoGio, PLV.ThoiGianBatDau, PLV.ThoiGianKetThuc " +
                       "FROM PHIENLAMVIEC PLV " +
                       "JOIN KHONGGIAN KG ON PLV.MaKG = KG.MaKG " +
                       "JOIN LOAIKHONGGIAN LKG ON KG.MaLoaiKG = LKG.MaLoaiKG " +
                       "WHERE PLV.MaPhien = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlKg)) {
            ps.setString(1, maPhien);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double donGia = rs.getDouble("DonGiaTheoGio");
                    Timestamp batDau = rs.getTimestamp("ThoiGianBatDau");
                    Timestamp ketThuc = rs.getTimestamp("ThoiGianKetThuc");
                    if (ketThuc == null) ketThuc = new Timestamp(System.currentTimeMillis());
                    
                    if (batDau != null) {
                        long diffMillis = ketThuc.getTime() - batDau.getTime();
                        long totalMinutes = diffMillis / 60000;
                        long hours = totalMinutes / 60;
                        long minutes = totalMinutes % 60;
                        long roundedHours = (minutes < 15) ? hours : hours + 1;
                        
                        tienKhongGian = donGia * roundedHours;
                    }
                }
            }
        }

        String sqlDv = "SELECT SUM(CT.SoLuong * DV.DonGia) AS TienDV " +
                       "FROM CHITIETDICHVU CT " +
                       "JOIN DICHVU DV ON CT.MaDV = DV.MaDV " +
                       "WHERE CT.MaPhien = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlDv)) {
            ps.setString(1, maPhien);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tienDichVu = rs.getDouble("TienDV");
                }
            }
        }

        return Math.round((tienKhongGian + tienDichVu) * 100.0) / 100.0;
    }

    public List<DichVuTrongPhienDTO> layDichVuCuaPhien(String maPhien) {
        List<DichVuTrongPhienDTO> list = new ArrayList<>();
        String sql = "SELECT dv.TenDV, ct.SoLuong, dv.DonGia " +
                "FROM CHITIETDICHVU ct " +
                "JOIN DICHVU dv ON ct.MaDV = dv.MaDV " +
                "WHERE ct.MaPhien = ?";

        try (Connection conn = getConn();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maPhien);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String ten = rs.getString("TenDV");
                    int sl = rs.getInt("SoLuong");
                    double dg = rs.getDouble("DonGia");
                    list.add(new DichVuTrongPhienDTO(ten, sl, dg, sl * dg));
                }
            }
        } catch (SQLException e) {
            System.err.println("[PhienLamViecDAO] Lỗi lấy dịch vụ: " + e.getMessage());
        }
        return list;
    }

    public int demSoLuong() {
        String sql = "SELECT COUNT(*) FROM PHIENLAMVIEC";
        try (Connection conn = getConn();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[PhienLamViecDAO] Lỗi đếm số lượng: " + e.getMessage());
        }
        return 0;
    }

    public boolean xoaPhien(String maPhien) {
        String sqlChiTiet = "DELETE FROM CHITIETDICHVU WHERE MaPhien = ?";
        String sqlHoaDon = "DELETE FROM HOADON WHERE MaPhien = ?";
        String sqlPhien = "DELETE FROM PHIENLAMVIEC WHERE MaPhien = ?";

        try (Connection conn = getConn()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(sqlChiTiet)) {
                    ps.setString(1, maPhien);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(sqlHoaDon)) {
                    ps.setString(1, maPhien);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(sqlPhien)) {
                    ps.setString(1, maPhien);
                    if (ps.executeUpdate() == 0) {
                        conn.rollback();
                        return false;
                    }
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("[PhienLamViecDAO] Lỗi khi xóa phiên: " + e.getMessage());
            return false;
        }
    }
}
