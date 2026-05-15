package com.wms.dao.TrangChuQuanLy.QuanLyVaiTro;

import com.wms.config.DatabaseConnection;
import com.wms.model.TrangChuQuanLy.QuanLyVaiTro.VaiTroDTO;
import com.wms.model.TrangChuQuanLy.QuanLyVaiTro.ChucNangDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VaiTroDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<VaiTroDTO> layTatCaVaiTro() {
        List<VaiTroDTO> list = new ArrayList<>();
        String sql = "SELECT MaVaiTro, TenVaiTro, MoTa FROM VAITRO ORDER BY MaVaiTro";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                VaiTroDTO vt = new VaiTroDTO();
                vt.setMaVaiTro(rs.getString("MaVaiTro"));
                vt.setTenVaiTro(rs.getString("TenVaiTro"));
                vt.setMoTa(rs.getString("MoTa"));
                list.add(vt);
            }
        } catch (SQLException e) {
            System.err.println("[VaiTroDAO] Lỗi lấy danh sách vai trò: " + e.getMessage());
        }
        return list;
    }

    public boolean themVaiTro(VaiTroDTO vt, List<String> danhSachMaChucNang) {
        Connection conn = getConn();
        if (conn == null)
            return false;

        boolean autoCommit = true;
        try {
            autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            String maVT = vt.getMaVaiTro();
            if (maVT == null || maVT.trim().isEmpty()) {
                maVT = taoMaVaiTroMoi(conn);
                vt.setMaVaiTro(maVT);
            }

            String sqlVT = "INSERT INTO VAITRO (MaVaiTro, TenVaiTro, MoTa) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlVT)) {
                ps.setString(1, maVT);
                ps.setString(2, vt.getTenVaiTro());
                ps.setString(3, vt.getMoTa());
                ps.executeUpdate();
            }

            String sqlNCN = "INSERT INTO NHOMCHUCNANG (MaNhomChucNang, TenNhomChucNang, MoTa) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlNCN)) {
                ps.setString(1, maVT);
                ps.setString(2, vt.getTenVaiTro());
                ps.setString(3, vt.getMoTa());
                ps.executeUpdate();
            }

            String sqlCTNCN = "INSERT INTO CHITIETNHOMCHUCNANG (MaVaiTro, MaNhomChucNang, MoTa) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlCTNCN)) {
                ps.setString(1, maVT);
                ps.setString(2, maVT);
                ps.setString(3, "Liên kết vai trò với nhóm chức năng mặc định");
                ps.executeUpdate();
            }

            if (danhSachMaChucNang != null && !danhSachMaChucNang.isEmpty()) {
                String sqlCN = "INSERT INTO CHITIETCHUCNANG (MaNhomChucNang, MaChucNang, MoTa) VALUES (?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlCN)) {
                    for (String maCN : danhSachMaChucNang) {
                        ps.setString(1, maVT);
                        ps.setString(2, maCN);
                        ps.setString(3, "Cấp quyền chức năng cho nhóm");
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("[VaiTroDAO] Lỗi thêm vai trò: " + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException ex) {
            }
            return false;
        } finally {
            try {
                conn.setAutoCommit(autoCommit);
            } catch (SQLException ex) {
            }
        }
    }

    public boolean capNhatVaiTro(VaiTroDTO vt, List<String> danhSachMaChucNang) {
        Connection conn = getConn();
        if (conn == null)
            return false;

        boolean autoCommit = true;
        try {
            autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn
                    .prepareStatement("UPDATE VAITRO SET TenVaiTro = ?, MoTa = ? WHERE MaVaiTro = ?")) {
                ps.setString(1, vt.getTenVaiTro());
                ps.setString(2, vt.getMoTa());
                ps.setString(3, vt.getMaVaiTro());
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE NHOMCHUCNANG SET TenNhomChucNang = ?, MoTa = ? WHERE MaNhomChucNang = ?")) {
                ps.setString(1, vt.getTenVaiTro());
                ps.setString(2, vt.getMoTa());
                ps.setString(3, vt.getMaVaiTro());
                ps.executeUpdate();
            }

            capNhatChucNangInternal(conn, vt.getMaVaiTro(), danhSachMaChucNang);

            conn.commit();
            return true;
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
            }
            return false;
        } finally {
            try {
                conn.setAutoCommit(autoCommit);
            } catch (SQLException ex) {
            }
        }
    }

    public boolean xoaVaiTro(String maVaiTro) {
        Connection conn = getConn();
        if (conn == null)
            return false;
        boolean autoCommit = true;
        try {
            autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            String sqlCheck = "SELECT COUNT(*) FROM CHITIETVAITRO WHERE MaVaiTro = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlCheck)) {
                ps.setString(1, maVaiTro);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0)
                        return false;
                }
            }

            String[] queries = {
                    "DELETE FROM CHITIETCHUCNANG WHERE MaNhomChucNang = ?",
                    "DELETE FROM CHITIETNHOMCHUCNANG WHERE MaVaiTro = ?",
                    "DELETE FROM NHOMCHUCNANG WHERE MaNhomChucNang = ?",
                    "DELETE FROM VAITRO WHERE MaVaiTro = ?"
            };
            for (String sql : queries) {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, maVaiTro);
                    ps.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
            }
            return false;
        } finally {
            try {
                conn.setAutoCommit(autoCommit);
            } catch (SQLException ex) {
            }
        }
    }

    public List<String[]> layChucNangCuaVaiTro(String maVaiTro) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT cn.MaChucNang, cn.TenChucNang FROM CHUCNANG cn " +
                "JOIN CHITIETCHUCNANG ctcn ON cn.MaChucNang = ctcn.MaChucNang " +
                "WHERE ctcn.MaNhomChucNang = ? ORDER BY cn.MaChucNang";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maVaiTro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new String[] { rs.getString("MaChucNang"), rs.getString("TenChucNang") });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ChucNangDTO> layTatCaChucNang() {
        List<ChucNangDTO> list = new ArrayList<>();
        String sql = "SELECT MaChucNang, TenChucNang, MoTa FROM CHUCNANG ORDER BY MaChucNang";
        try (PreparedStatement ps = getConn().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ChucNangDTO cn = new ChucNangDTO();
                cn.setMaChucNang(rs.getString("MaChucNang"));
                cn.setTenChucNang(rs.getString("TenChucNang"));
                cn.setMoTa(rs.getString("MoTa"));
                list.add(cn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private void capNhatChucNangInternal(Connection conn, String maVaiTro, List<String> dsMaCN) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM CHITIETCHUCNANG WHERE MaNhomChucNang = ?")) {
            ps.setString(1, maVaiTro);
            ps.executeUpdate();
        }
        if (dsMaCN != null && !dsMaCN.isEmpty()) {
            String sql = "INSERT INTO CHITIETCHUCNANG (MaNhomChucNang, MaChucNang, MoTa) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (String maCN : dsMaCN) {
                    ps.setString(1, maVaiTro);
                    ps.setString(2, maCN);
                    ps.setString(3, "Cập nhật quyền chức năng");
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }
    }

    private String taoMaVaiTroMoi(Connection conn) throws SQLException {
        String sql = "SELECT MAX(MaVaiTro) AS MaxMa FROM VAITRO WHERE MaVaiTro LIKE 'VT%'";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String maxMa = rs.getString("MaxMa");
                if (maxMa != null) {
                    try {
                        int soThuTu = Integer.parseInt(maxMa.substring(2)) + 1;
                        return String.format("VT%02d", soThuTu);
                    } catch (NumberFormatException e) {
                    }
                }
            }
        }
        return "VT01";
    }

    public boolean capNhatChucNangCuaVaiTro(String maVaiTro, List<String> dsMaCN) {
        try {
            capNhatChucNangInternal(getConn(), maVaiTro, dsMaCN);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void khoiTaoDuLieuChucNang() {
        com.wms.util.DataInitializer.khoiTaoChucNang(getConn());
    }

}