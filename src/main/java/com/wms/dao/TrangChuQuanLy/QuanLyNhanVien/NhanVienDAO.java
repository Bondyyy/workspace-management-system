package com.wms.dao.TrangChuQuanLy.QuanLyNhanVien;

import com.wms.config.DatabaseConnection;
import com.wms.model.TrangChuQuanLy.QuanLyNhanVien.NhanVienDTO;
import com.wms.model.NguoiDungDTO;
import com.wms.dao.NguoiDungDAO;
import com.wms.dao.NguoiDungDAO;
import com.wms.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
            "       nv.MaCN, cvt.MaVaiTro, nv.MaND, nd.AnhDaiDien, nd.NgaySinh " +
            "FROM NHANVIEN nv " +
            "JOIN NGUOIDUNG nd ON nv.MaND = nd.MaND " +
            "LEFT JOIN KHACHHANG kh ON kh.MaND = nd.MaND " +
            "LEFT JOIN CHINHANH cn ON nv.MaCN = cn.MaCN " +
            "LEFT JOIN CHITIETVAITRO cvt ON nd.MaND = cvt.MaND " +
            "LEFT JOIN VAITRO vt ON cvt.MaVaiTro = vt.MaVaiTro"
        );

        sql.append(" WHERE nv.MaNV != 'NV_ADMIN'");
        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            sql.append(" AND (LOWER(kh.HoTenKH) LIKE ? OR nd.SDT LIKE ?)");
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
                    list.add(new Object[]{
                        rs.getString("MaNV"), rs.getString("HoTenKH"), rs.getString("SDT"),
                        rs.getString("LoaiNV"), rs.getString("CaLamViec"), rs.getString("TenCN"),
                        rs.getString("TenVaiTro"), rs.getString("TrangThaiLamViec"),
                        rs.getString("GioiTinh"), rs.getString("Email"), rs.getDouble("LuongCoBan"),
                        rs.getString("MaCN"), rs.getString("MaVaiTro"), rs.getString("MaND"),
                        rs.getBytes("AnhDaiDien"), rs.getDate("NgaySinh")
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("[NhanVienDAO] Lỗi tìm kiếm: " + e.getMessage());
        }
        return list;
    }

    public boolean themNhanVien(NhanVienDTO nv, NguoiDungDTO nd, String hoTen, String maVaiTro, String matKhau) {
        Connection conn = getConn();
        if (conn == null) return false;
        
        NguoiDungDAO ndDAO = new NguoiDungDAO();
        try {
            if (ndDAO.kiemTraEmailTonTai(nd.getEmail()) || ndDAO.kiemTraSdtTonTai(nd.getSdt())) return false;
        } catch (SQLException e) { return false; }

        boolean autoCommit = true;
        try {
            autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            String maND = java.util.UUID.randomUUID().toString();
            String maKH = java.util.UUID.randomUUID().toString();
            String hashedPw = (matKhau != null && !matKhau.isEmpty()) ? PasswordUtil.hash(matKhau) : PasswordUtil.hash("123456");

            String sqlND = "INSERT INTO NGUOIDUNG (MaND, TenTaiKhoan, MatKhauMaHoa, SDT, Email, GioiTinh, AnhDaiDien, NgaySinh, TrangThaiND, ThoiGianTao, CapNhatLanCuoi) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'Đang hoạt động', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            try (PreparedStatement ps = conn.prepareStatement(sqlND)) {
                ps.setString(1, maND);
                ps.setString(2, nd.getSdt());
                ps.setString(3, hashedPw); 
                ps.setString(4, nd.getSdt());
                ps.setString(5, nd.getEmail());
                ps.setString(6, nd.getGioiTinh());
                ps.setBytes(7, nd.getAnhDaiDien());
                ps.setDate(8, nd.getNgaySinh());
                ps.executeUpdate();
            }

            String sqlKH = "INSERT INTO KHACHHANG (MaKH, HoTenKH, MaHangThanhVien, TongChiTieu, CapNhatLanCuoi, MaND) VALUES (?, ?, 'HTV00', 0, CURRENT_TIMESTAMP, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlKH)) {
                ps.setString(1, maKH);
                ps.setString(2, hoTen);
                ps.setString(3, maND);
                ps.executeUpdate();
            }

            if (nv.getMaNV() == null || nv.getMaNV().isEmpty()) nv.setMaNV(taoMaNVMoi(conn));
            String sqlNV = "INSERT INTO NHANVIEN (MaNV, LoaiNV, NgayVaoLam, TrangThaiLamViec, CaLamViec, LuongCoBan, MaCN, MaND) VALUES (?, ?, CURRENT_DATE, ?, ?, ?, ?, ?)";
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
            try { conn.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            try { conn.setAutoCommit(autoCommit); } catch (SQLException ex) {}
        }
    }

    public boolean capNhatNhanVien(NhanVienDTO nv, NguoiDungDTO nd, String hoTen, String maVaiTro, String matKhau) {
        Connection conn = getConn();
        if (conn == null) return false;
        boolean autoCommit = true;
        try {
            autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            boolean hasNewPw = (matKhau != null && !matKhau.isEmpty());
            String sqlND = "UPDATE NGUOIDUNG SET SDT = ?, Email = ?, GioiTinh = ?, AnhDaiDien = ?, NgaySinh = ?, CapNhatLanCuoi = CURRENT_TIMESTAMP" + (hasNewPw ? ", MatKhauMaHoa = ?" : "") + " WHERE MaND = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlND)) {
                ps.setString(1, nd.getSdt());
                ps.setString(2, nd.getEmail());
                ps.setString(3, nd.getGioiTinh());
                ps.setBytes(4, nd.getAnhDaiDien());
                ps.setDate(5, nd.getNgaySinh());
                if (hasNewPw) {
                    ps.setString(6, PasswordUtil.hash(matKhau));
                    ps.setString(7, nv.getMaND());
                } else {
                    ps.setString(6, nv.getMaND());
                }
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

            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM CHITIETVAITRO WHERE MaND = ?")) {
                ps.setString(1, nv.getMaND());
                ps.executeUpdate();
            }

            if (maVaiTro != null && !maVaiTro.isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO CHITIETVAITRO (MaND, MaVaiTro) VALUES (?, ?)")) {
                    ps.setString(1, nv.getMaND());
                    ps.setString(2, maVaiTro);
                    ps.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
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

            String[] queries = {
                "DELETE FROM CHITIETVAITRO WHERE MaND = ?",
                "DELETE FROM NHANVIEN WHERE MaNV = ?",
                "DELETE FROM KHACHHANG WHERE MaND = ?",
                "DELETE FROM NGUOIDUNG WHERE MaND = ?"
            };
            for (String sql : queries) {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, sql.contains("MaNV") ? maNV : maND);
                    ps.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            try { conn.setAutoCommit(autoCommit); } catch (SQLException ex) {}
        }
    }

    public List<String[]> layDanhSachChiNhanh() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT MaCN, TenCN FROM CHINHANH WHERE TrangThai = 'Đang hoạt động'";
        try (PreparedStatement ps = getConn().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(new String[]{rs.getString("MaCN"), rs.getString("TenCN")});
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public String taoMaNVMoi(Connection conn) throws SQLException {
        boolean isStandalone = (conn == null);
        Connection localConn = isStandalone ? getConn() : conn;
        String sql = "SELECT MAX(MaNV) AS MaxMa FROM NHANVIEN WHERE MaNV LIKE 'NV%'";
        try (PreparedStatement ps = localConn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String maxMa = rs.getString("MaxMa");
                if (maxMa != null) {
                    try {
                        int soThuTu = Integer.parseInt(maxMa.substring(2)) + 1;
                        return String.format("NV%06d", soThuTu);
                    } catch (NumberFormatException e) {}
                }
            }
        }
        return "NV000001";
    }
    
    public String layMaNVTuMaND(String maND) {
        String sql = "SELECT MaNV FROM NHANVIEN WHERE MaND = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maND);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("MaNV");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public String layMaCNTuMaND(String maND) {
        String sql = "SELECT MaCN FROM NHANVIEN WHERE MaND = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maND);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("MaCN");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
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
}
