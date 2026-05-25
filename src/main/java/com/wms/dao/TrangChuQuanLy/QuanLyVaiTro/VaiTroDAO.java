package com.wms.dao.TrangChuQuanLy.QuanLyVaiTro;

import com.wms.config.DatabaseConnection;
import com.wms.model.TrangChuQuanLy.QuanLyVaiTro.VaiTroDTO;
import com.wms.model.TrangChuQuanLy.QuanLyVaiTro.ChucNangDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VaiTroDAO {

    public List<VaiTroDTO> layTatCaVaiTro() {
        List<VaiTroDTO> list = new ArrayList<>();
        String sql = "SELECT MaVaiTro, TenVaiTro, MoTa FROM VAITRO ORDER BY MaVaiTro";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                VaiTroDTO vt = new VaiTroDTO();
                vt.setMaVaiTro(rs.getString("MaVaiTro"));
                vt.setTenVaiTro(rs.getString("TenVaiTro"));
                vt.setMoTa(rs.getString("MoTa"));
                list.add(vt);
            }
        } catch (Exception e) {
            System.err.println("[VaiTroDAO] Lỗi lấy danh sách vai trò: " + e.getMessage());
        }
        return list;
    }

    public boolean themVaiTro(VaiTroDTO vt, List<String> danhSachMaChucNang) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            boolean autoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER SESSION DISABLE PARALLEL DML");
                } catch (Exception e) {
                    System.err.println("Cannot disable parallel DML: " + e.getMessage());
                }

                String maVT;
                String sqlVT = """
                        BEGIN
                            INSERT INTO VAITRO (TenVaiTro, MoTa)
                            VALUES (?, ?)
                            RETURNING MaVaiTro INTO ?;
                        END;
                        """;
                try (CallableStatement cs = conn.prepareCall(sqlVT)) {
                    cs.setString(1, vt.getTenVaiTro());
                    cs.setString(2, vt.getMoTa());
                    cs.registerOutParameter(3, Types.VARCHAR);
                    cs.execute();
                    maVT = cs.getString(3);
                    vt.setMaVaiTro(maVT);
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
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                }
                throw e;
            } finally {
                try {
                    conn.setAutoCommit(autoCommit);
                } catch (SQLException ex) {
                }
            }
        }
    }

    public boolean capNhatVaiTro(VaiTroDTO vt, List<String> danhSachMaChucNang) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            boolean autoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER SESSION DISABLE PARALLEL DML");
                } catch (Exception e) {
                    System.err.println("Cannot disable parallel DML: " + e.getMessage());
                }

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
                throw e;
            } finally {
                try {
                    conn.setAutoCommit(autoCommit);
                } catch (SQLException ex) {
                }
            }
        }
    }

    public boolean xoaVaiTro(String maVaiTro) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            boolean autoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER SESSION DISABLE PARALLEL DML");
                } catch (Exception e) {
                    System.err.println("Cannot disable parallel DML: " + e.getMessage());
                }

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
                throw e;
            } finally {
                try {
                    conn.setAutoCommit(autoCommit);
                } catch (SQLException ex) {
                }
            }
        }
    }

    public List<String[]> layChucNangCuaVaiTro(String maVaiTro) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT cn.MaChucNang, cn.TenChucNang FROM CHUCNANG cn " +
                "JOIN CHITIETCHUCNANG ctcn ON cn.MaChucNang = ctcn.MaChucNang " +
                "WHERE ctcn.MaNhomChucNang = ? ORDER BY cn.MaChucNang";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maVaiTro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new String[] { rs.getString("MaChucNang"), rs.getString("TenChucNang") });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ChucNangDTO> layTatCaChucNang() {
        List<ChucNangDTO> list = new ArrayList<>();
        String sql = "SELECT MaChucNang, TenChucNang, MoTa FROM CHUCNANG ORDER BY MaChucNang";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ChucNangDTO cn = new ChucNangDTO();
                cn.setMaChucNang(rs.getString("MaChucNang"));
                cn.setTenChucNang(rs.getString("TenChucNang"));
                cn.setMoTa(rs.getString("MoTa"));
                list.add(cn);
            }
        } catch (Exception e) {
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

    public String sinhMaVT() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            return sinhMaVT(conn);
        } catch (Exception e) {
            System.err.println("[VaiTroDAO] Lỗi tự động sinh mã vai trò: " + e.getMessage());
            return "";
        }
    }

    public String sinhMaVT(Connection conn) throws SQLException {
        String sql = "SELECT NVL(MAX(TO_NUMBER(REGEXP_SUBSTR(MaVaiTro, '[0-9]+$'))), -1) FROM VAITRO WHERE REGEXP_LIKE(MaVaiTro, '^VT[0-9]+$')";
        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            int max = rs.next() ? rs.getInt(1) : -1;
            return String.format("VT%02d", max + 1);
        }
    }

    public boolean capNhatChucNangCuaVaiTro(String maVaiTro, List<String> dsMaCN) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            capNhatChucNangInternal(conn, maVaiTro, dsMaCN);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void khoiTaoDuLieuChucNang() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            com.wms.util.DataInitializer.khoiTaoChucNang(conn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
