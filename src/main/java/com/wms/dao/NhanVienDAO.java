package com.wms.dao;

import com.wms.config.DatabaseConnection;
import com.wms.model.NhanVienDTO;
import com.wms.model.NguoiDungDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.wms.util.PasswordUtil;

public class NhanVienDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<Object[]> layDanhSachNhanVien() {
        return timKiemNhanVien(null);
    }

    public List<Object[]> timKiemNhanVien(String tuKhoa) {
        List<Object[]> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
            "SELECT nv.MaNV, kh.HoTenKH, nd.SDT, nv.LoaiNV, nv.CaLamViec, " +
            "       cn.TenCN, vt.TenVaiTro, nv.TrangThaiLamViec, " +
            "       nd.GioiTinh, nd.Email, nv.LuongCoBan, " +
            "       nv.MaCN, cvt.MaVaiTro, nv.MaND, nd.AnhDaiDien " +
            "FROM NHANVIEN nv " +
            "JOIN NGUOIDUNG nd ON nv.MaND = nd.MaND " +
            "LEFT JOIN KHACHHANG kh ON kh.MaND = nd.MaND " +
            "LEFT JOIN CHINHANH cn ON nv.MaCN = cn.MaCN " +
            "LEFT JOIN CHITIETVAITRO cvt ON nd.MaND = cvt.MaND " +
            "LEFT JOIN VAITRO vt ON cvt.MaVaiTro = vt.MaVaiTro"
        );

        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            sql.append(" WHERE LOWER(kh.HoTenKH) LIKE ? OR nd.SDT LIKE ?");
        }

        sql.append(" ORDER BY nv.MaNV");

        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
            if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
                String pattern = "%" + tuKhoa.trim().toLowerCase() + "%";
                ps.setString(1, pattern);
                ps.setString(2, "%" + tuKhoa.trim() + "%");
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] row = {
                        rs.getString("MaNV"),
                        rs.getString("HoTenKH"),
                        rs.getString("SDT"),
                        rs.getString("LoaiNV"),
                        rs.getString("CaLamViec"),
                        rs.getString("TenCN"),
                        rs.getString("TenVaiTro"),
                        rs.getString("TrangThaiLamViec"),
                        rs.getString("GioiTinh"),
                        rs.getString("Email"),
                        rs.getDouble("LuongCoBan"),
                        rs.getString("MaCN"),
                        rs.getString("MaVaiTro"),
                        rs.getString("MaND"),
                        rs.getString("AnhDaiDien")
                    };
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("[NhanVienDAO] Lỗi tìm kiếm nhân viên: " + e.getMessage());
        }

        return list;
    }

    public boolean themNhanVien(NhanVienDTO nv, NguoiDungDTO nd, String hoTen, String maVaiTro) {
        Connection conn = getConn();
        if (conn == null) return false;

        boolean autoCommit = true;
        try {
            autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            String maND = java.util.UUID.randomUUID().toString();
            String maKH = java.util.UUID.randomUUID().toString();
            nd.setMaND(maND);
            nv.setMaND(maND);

            String sqlND = "INSERT INTO NGUOIDUNG (MaND, TenTaiKhoan, MatKhauMaHoa, SDT, Email, GioiTinh, AnhDaiDien, TrangThaiND, ThoiGianTao, CapNhatLanCuoi) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            try (PreparedStatement ps = conn.prepareStatement(sqlND)) {
                String defaultUser = (nd.getSdt() != null && !nd.getSdt().isEmpty()) ? nd.getSdt() : maND.substring(0, 8);
                ps.setString(1, maND);
                ps.setString(2, defaultUser);
                ps.setString(3, PasswordUtil.hash("123456")); 
                ps.setString(4, nd.getSdt());
                ps.setString(5, nd.getEmail());
                ps.setString(6, nd.getGioiTinh());
                ps.setString(7, nd.getAnhDaiDien());
                ps.setString(8, "Đang hoạt động");
                ps.executeUpdate();
            }

            String sqlKH = "INSERT INTO KHACHHANG (MaKH, HoTenKH, TongChiTieu, CapNhatLanCuoi, MaND) " +
                           "VALUES (?, ?, 0, CURRENT_TIMESTAMP, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlKH)) {
                ps.setString(1, maKH);
                ps.setString(2, hoTen);
                ps.setString(3, maND);
                ps.executeUpdate();
            }

            if (nv.getMaNV() == null || nv.getMaNV().isEmpty()) {
                nv.setMaNV(taoMaNVMoi(conn));
            }
            String sqlNV = "INSERT INTO NHANVIEN (MaNV, LoaiNV, NgayVaoLam, TrangThaiLamViec, PhuCap, TienThuong, CaLamViec, LuongCoBan, MaCN, MaND) " +
                           "VALUES (?, ?, CURRENT_DATE, ?, 0, 0, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlNV)) {
                ps.setString(1, nv.getMaNV());
                ps.setString(2, nv.getLoaiNV());
                ps.setString(3, nv.getTrangThaiLamViec() != null ? nv.getTrangThaiLamViec() : "Đang làm việc");
                ps.setString(4, nv.getCaLamViec());
                ps.setDouble(5, nv.getLuongCoBan());
                ps.setString(6, nv.getMaCN());
                ps.setString(7, maND);
                ps.executeUpdate();
            }

            if (maVaiTro != null && !maVaiTro.isEmpty()) {
                String sqlVT = "INSERT INTO CHITIETVAITRO (MaND, MaVaiTro) VALUES (?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlVT)) {
                    ps.setString(1, maND);
                    ps.setString(2, maVaiTro);
                    ps.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("[NhanVienDAO] Lỗi thêm nhân viên: " + e.getMessage());
            try { conn.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            try { conn.setAutoCommit(autoCommit); } catch (SQLException ex) {}
        }
    }

    public boolean capNhatNhanVien(NhanVienDTO nv, NguoiDungDTO nd, String hoTen, String maVaiTro) {
        Connection conn = getConn();
        if (conn == null) return false;

        boolean autoCommit = true;
        try {
            autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            String sqlND = "UPDATE NGUOIDUNG SET SDT = ?, Email = ?, GioiTinh = ?, AnhDaiDien = ?, CapNhatLanCuoi = CURRENT_TIMESTAMP WHERE MaND = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlND)) {
                ps.setString(1, nd.getSdt());
                ps.setString(2, nd.getEmail());
                ps.setString(3, nd.getGioiTinh());
                ps.setString(4, nd.getAnhDaiDien());
                ps.setString(5, nv.getMaND());
                ps.executeUpdate();
            }

            String sqlKH = "UPDATE KHACHHANG SET HoTenKH = ?, CapNhatLanCuoi = CURRENT_TIMESTAMP WHERE MaND = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlKH)) {
                ps.setString(1, hoTen);
                ps.setString(2, nv.getMaND());
                ps.executeUpdate();
            }

            String sqlNV = "UPDATE NHANVIEN SET LoaiNV = ?, CaLamViec = ?, LuongCoBan = ?, MaCN = ?, TrangThaiLamViec = ? WHERE MaNV = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlNV)) {
                ps.setString(1, nv.getLoaiNV());
                ps.setString(2, nv.getCaLamViec());
                ps.setDouble(3, nv.getLuongCoBan());
                ps.setString(4, nv.getMaCN());
                ps.setString(5, nv.getTrangThaiLamViec());
                ps.setString(6, nv.getMaNV());
                ps.executeUpdate();
            }

            if (maVaiTro != null && !maVaiTro.isEmpty()) {
                String sqlDelVT = "DELETE FROM CHITIETVAITRO WHERE MaND = ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlDelVT)) {
                    ps.setString(1, nv.getMaND());
                    ps.executeUpdate();
                }
                String sqlVT = "INSERT INTO CHITIETVAITRO (MaND, MaVaiTro) VALUES (?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlVT)) {
                    ps.setString(1, nv.getMaND());
                    ps.setString(2, maVaiTro);
                    ps.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("[NhanVienDAO] Lỗi cập nhật nhân viên: " + e.getMessage());
            try { conn.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            try { conn.setAutoCommit(autoCommit); } catch (SQLException ex) {}
        }
    }

    public boolean xoaNhanVien(String maNV, String maND) {
        Connection conn = getConn();
        if (conn == null) return false;

        boolean autoCommit = true;
        try {
            autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            String sqlVT = "DELETE FROM CHITIETVAITRO WHERE MaND = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlVT)) {
                ps.setString(1, maND);
                ps.executeUpdate();
            }

            String sqlNV = "DELETE FROM NHANVIEN WHERE MaNV = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlNV)) {
                ps.setString(1, maNV);
                ps.executeUpdate();
            }

            String sqlKH = "DELETE FROM KHACHHANG WHERE MaND = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlKH)) {
                ps.setString(1, maND);
                ps.executeUpdate();
            }

            String sqlND = "DELETE FROM NGUOIDUNG WHERE MaND = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlND)) {
                ps.setString(1, maND);
                ps.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("[NhanVienDAO] Lỗi xóa nhân viên: " + e.getMessage());
            try { conn.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            try { conn.setAutoCommit(autoCommit); } catch (SQLException ex) {}
        }
    }

    public List<String[]> layDanhSachChiNhanh() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT MaCN, TenCN FROM CHINHANH WHERE TrangThai = 'Đang hoạt động'";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new String[]{rs.getString("MaCN"), rs.getString("TenCN")});
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<String[]> layDanhSachVaiTro() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT MaVaiTro, TenVaiTro FROM VAITRO ORDER BY TenVaiTro";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new String[]{rs.getString("MaVaiTro"), rs.getString("TenVaiTro")});
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<String[]> layDanhSachQuanLy() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT nv.MaNV, kh.HoTenKH " +
                     "FROM NHANVIEN nv " +
                     "JOIN KHACHHANG kh ON nv.MaND = kh.MaND " +
                     "WHERE nv.TrangThaiLamViec = 'Đang làm việc'";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new String[]{rs.getString("MaNV"), rs.getString("HoTenKH")});
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private String taoMaNVMoi(Connection conn) throws SQLException {
        return "NV" + (System.currentTimeMillis() % 1000000);
    }
    
    public String layMaNVTuMaND(String maND) {
        String maNV = null;
        String sql = "SELECT MaNV FROM NHANVIEN WHERE MaND = ?";
        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maND);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    maNV = rs.getString("MaNV");
                }
            }
        } catch (SQLException e) {
            System.err.println("[NhanVienDAO] Lỗi lấy mã nhân viên từ mã người dùng: " + e.getMessage());
        }
        return maNV;
    }
}
