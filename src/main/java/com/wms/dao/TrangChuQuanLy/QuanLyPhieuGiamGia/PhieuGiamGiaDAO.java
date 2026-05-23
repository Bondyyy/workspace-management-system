package com.wms.dao.TrangChuQuanLy.QuanLyPhieuGiamGia;
import com.wms.config.DatabaseConnection;
import com.wms.model.TrangChuQuanLy.QuanLyPhieuGiamGia.PhieuGiamGiaDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class PhieuGiamGiaDAO {
    
    // Gộp layThongTinVoucher vào timTheoMa để tránh trùng lặp code
    public PhieuGiamGiaDTO timTheoMa(String maPGG) {
        String sql = "SELECT * FROM PHIEUGIAMGIA WHERE MaPGG = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPGG.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[PhieuGiamGiaDAO] Lỗi tìm theo mã: " + e.getMessage());
        }
        return null;
    }

    public PhieuGiamGiaDTO timTheoMaChuSo(String maChuSoPGG) {
        String sql = "SELECT * FROM PHIEUGIAMGIA WHERE UPPER(MaChuSoPGG) = UPPER(?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maChuSoPGG.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[PhieuGiamGiaDAO] Lỗi tìm theo mã chữ số: " + e.getMessage());
        }
        return null;
    }

    public int demSoLuong() {
        String sql = "SELECT COUNT(*) FROM PHIEUGIAMGIA";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[PhieuGiamGiaDAO] Lỗi đếm số lượng: " + e.getMessage());
        }
        return 0;
    }

    public String taoMaMoi() {
        return "";
    }

    public List<PhieuGiamGiaDTO> layDanhSach() {
        List<PhieuGiamGiaDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM PHIEUGIAMGIA ORDER BY NgayTaoPGG DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[PhieuGiamGiaDAO] Lỗi lấy danh sách: " + e.getMessage());
        }
        return list;
    }

    public List<PhieuGiamGiaDTO> timKiem(String keyword) {
        List<PhieuGiamGiaDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM PHIEUGIAMGIA WHERE UPPER(MaPGG) LIKE UPPER(?) OR UPPER(MaChuSoPGG) LIKE UPPER(?) ORDER BY NgayTaoPGG DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String k = "%" + keyword.trim() + "%";
            ps.setString(1, k);
            ps.setString(2, k);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[PhieuGiamGiaDAO] Lỗi tìm kiếm: " + e.getMessage());
        }
        return list;
    }

    public boolean themMoi(PhieuGiamGiaDTO dto) {
        String sql = "INSERT INTO PHIEUGIAMGIA (MaChuSoPGG, GiaTriGiamGia, GiaTriApDungToiThieu, " +
                     "NgayBatDauApDung, NgayKetThucApDung, SLDaDung, SLToiDa, NgayTaoPGG, MaNV, TrangThai) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dto.getMaChuSoPGG());
            ps.setDouble(2, dto.getGiaTriGiamGia());
            ps.setDouble(3, dto.getGiaTriApDungToiThieu());
            ps.setTimestamp(4, new Timestamp(dto.getNgayBatDauApDung().getTime()));
            ps.setTimestamp(5, new Timestamp(dto.getNgayKetThucApDung().getTime()));
            ps.setInt(6, 0);
            ps.setInt(7, dto.getSlToiDa());
            ps.setString(8, dto.getMaNV());
            ps.setString(9, dto.getTrangThai() != null ? dto.getTrangThai() : "Đang có hiệu lực");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PhieuGiamGiaDAO] Lỗi thêm mới: " + e.getMessage());
            return false;
        }
    }

    public boolean capNhat(PhieuGiamGiaDTO dto) {
        String sql = "UPDATE PHIEUGIAMGIA SET MaChuSoPGG = ?, GiaTriGiamGia = ?, GiaTriApDungToiThieu = ?, " +
                     "NgayBatDauApDung = ?, NgayKetThucApDung = ?, SLToiDa = ?, TrangThai = ? WHERE MaPGG = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dto.getMaChuSoPGG());
            ps.setDouble(2, dto.getGiaTriGiamGia());
            ps.setDouble(3, dto.getGiaTriApDungToiThieu());
            ps.setTimestamp(4, new Timestamp(dto.getNgayBatDauApDung().getTime()));
            ps.setTimestamp(5, new Timestamp(dto.getNgayKetThucApDung().getTime()));
            ps.setInt(6, dto.getSlToiDa());
            ps.setString(7, dto.getTrangThai() != null ? dto.getTrangThai() : "Đang có hiệu lực");
            ps.setString(8, dto.getMaPGG());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PhieuGiamGiaDAO] Lỗi cập nhật: " + e.getMessage());
            return false;
        }
    }

    public boolean xoa(String maPGG) {
        String sql = "UPDATE PHIEUGIAMGIA SET TrangThai = 'Đã vô hiệu hoá' WHERE MaPGG = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPGG);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PhieuGiamGiaDAO] Lỗi ngưng hoạt động: " + e.getMessage());
            return false;
        }
    }

    public boolean tangSoLuongDaDung(String maPGG) {
        String sql = "UPDATE PHIEUGIAMGIA SET SLDaDung = SLDaDung + 1 WHERE MaPGG = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPGG);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PhieuGiamGiaDAO] Lỗi tăng số lượng đã dùng: " + e.getMessage());
            return false;
        }
    }

    private String tinhTrangThaiTuDong(PhieuGiamGiaDTO dto, String dbTrangThai) {
        if ("Đã vô hiệu hoá".equals(dbTrangThai) || "Đã ngưng".equals(dbTrangThai)) {
            return "Đã vô hiệu hoá";
        }
        java.util.Date now = new java.util.Date();
        if (dto.getNgayBatDauApDung() != null && now.before(dto.getNgayBatDauApDung())) {
            return "Chưa đến hạn bắt đầu";
        }
        if (dto.getNgayKetThucApDung() != null && now.after(dto.getNgayKetThucApDung())) {
            return "Hết hiệu lực";
        }
        if (dto.getSlToiDa() > 0 && dto.getSlDaDung() >= dto.getSlToiDa()) {
            return "Hết hiệu lực";
        }
        return "Đang có hiệu lực";
    }

    private PhieuGiamGiaDTO mapRow(ResultSet rs) throws SQLException {
        PhieuGiamGiaDTO dto = new PhieuGiamGiaDTO();
        dto.setMaPGG(rs.getString("MaPGG"));
        dto.setMaChuSoPGG(rs.getString("MaChuSoPGG"));
        dto.setGiaTriGiamGia(rs.getDouble("GiaTriGiamGia"));
        dto.setGiaTriApDungToiThieu(rs.getDouble("GiaTriApDungToiThieu"));
        dto.setNgayBatDauApDung(rs.getTimestamp("NgayBatDauApDung"));
        dto.setNgayKetThucApDung(rs.getTimestamp("NgayKetThucApDung"));
        dto.setSlDaDung(rs.getInt("SLDaDung"));
        dto.setSlToiDa(rs.getInt("SLToiDa"));
        dto.setNgayTaoPGG(rs.getTimestamp("NgayTaoPGG"));
        dto.setMaNV(rs.getString("MaNV"));
        
        String dbTrangThai = rs.getString("TrangThai");
        dto.setTrangThai(tinhTrangThaiTuDong(dto, dbTrangThai));
        return dto;
    }
}
