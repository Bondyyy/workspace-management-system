package com.wms.dao;

import com.wms.config.DatabaseConnection;
import com.wms.model.PhanQuyen_BaoMat.VaiTroDTO;
import com.wms.model.PhanQuyen_BaoMat.ChucNangDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VaiTroDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<VaiTroDTO> layTatCaVaiTro() {
        List<VaiTroDTO> list = new ArrayList<>();
        String sql = "SELECT MaVaiTro, TenVaiTro, MoTa FROM VAITRO ORDER BY TenVaiTro";
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

            String maVT = taoMaVaiTroMoi(conn);
            vt.setMaVaiTro(maVT);

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

            String sqlCTNCN = "INSERT INTO CHITIETNHOMCHUCNANG (MaVaiTro, MaNhomChucNang) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlCTNCN)) {
                ps.setString(1, maVT);
                ps.setString(2, maVT);
                ps.executeUpdate();
            }

            if (danhSachMaChucNang != null && !danhSachMaChucNang.isEmpty()) {
                String sqlCN = "INSERT INTO CHITIETCHUCNANG (MaNhomChucNang, MaChucNang) VALUES (?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlCN)) {
                    for (String maCN : danhSachMaChucNang) {
                        ps.setString(1, maVT);
                        ps.setString(2, maCN);
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

            String sqlVT = "UPDATE VAITRO SET TenVaiTro = ?, MoTa = ? WHERE MaVaiTro = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlVT)) {
                ps.setString(1, vt.getTenVaiTro());
                ps.setString(2, vt.getMoTa());
                ps.setString(3, vt.getMaVaiTro());
                ps.executeUpdate();
            }

            String sqlNCN = "UPDATE NHOMCHUCNANG SET TenNhomChucNang = ?, MoTa = ? WHERE MaNhomChucNang = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlNCN)) {
                ps.setString(1, vt.getTenVaiTro());
                ps.setString(2, vt.getMoTa());
                ps.setString(3, vt.getMaVaiTro());
                ps.executeUpdate();
            }

            capNhatChucNangInternal(conn, vt.getMaVaiTro(), danhSachMaChucNang);

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("[VaiTroDAO] Lỗi cập nhật vai trò: " + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException ex) {
                /* ignore */ }
            return false;
        } finally {
            try {
                conn.setAutoCommit(autoCommit);
            } catch (SQLException ex) {
                /* ignore */ }
        }
    }

    public boolean capNhatChucNangCuaVaiTro(String maVaiTro, List<String> danhSachMaChucNang) {
        Connection conn = getConn();
        if (conn == null)
            return false;

        boolean autoCommit = true;
        try {
            autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            damBaoNhomChucNangProxy(conn, maVaiTro);

            capNhatChucNangInternal(conn, maVaiTro, danhSachMaChucNang);

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("[VaiTroDAO] Lỗi cập nhật chức năng vai trò: " + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException ex) {
                /* ignore */ }
            return false;
        } finally {
            try {
                conn.setAutoCommit(autoCommit);
            } catch (SQLException ex) {
                /* ignore */ }
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

            // Kiểm tra có người dùng nào đang dùng vai trò này không
            String sqlCheck = "SELECT COUNT(*) FROM CHITIETVAITRO WHERE MaVaiTro = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlCheck)) {
                ps.setString(1, maVaiTro);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.err.println("[VaiTroDAO] Không thể xóa: vai trò đang được gán cho người dùng.");
                        return false;
                    }
                }
            }

            String sqlDelCTCN = "DELETE FROM CHITIETCHUCNANG WHERE MaNhomChucNang = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlDelCTCN)) {
                ps.setString(1, maVaiTro);
                ps.executeUpdate();
            }

            String sqlDelCTNCN = "DELETE FROM CHITIETNHOMCHUCNANG WHERE MaVaiTro = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlDelCTNCN)) {
                ps.setString(1, maVaiTro);
                ps.executeUpdate();
            }

            String sqlDelNCN = "DELETE FROM NHOMCHUCNANG WHERE MaNhomChucNang = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlDelNCN)) {
                ps.setString(1, maVaiTro);
                ps.executeUpdate();
            }

            String sqlDelVT = "DELETE FROM VAITRO WHERE MaVaiTro = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlDelVT)) {
                ps.setString(1, maVaiTro);
                ps.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("[VaiTroDAO] Lỗi xóa vai trò: " + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException ex) {
                /* ignore */ }
            return false;
        } finally {
            try {
                conn.setAutoCommit(autoCommit);
            } catch (SQLException ex) {
                /* ignore */ }
        }
    }

    public List<String[]> layChucNangCuaVaiTro(String maVaiTro) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT cn.MaChucNang, cn.TenChucNang " +
                "FROM CHUCNANG cn " +
                "JOIN CHITIETCHUCNANG ctcn ON cn.MaChucNang = ctcn.MaChucNang " +
                "JOIN CHITIETNHOMCHUCNANG ctncn ON ctcn.MaNhomChucNang = ctncn.MaNhomChucNang " +
                "WHERE ctncn.MaVaiTro = ? " +
                "ORDER BY cn.MaChucNang";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maVaiTro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new String[] { rs.getString("MaChucNang"), rs.getString("TenChucNang") });
                }
            }
        } catch (SQLException e) {
            System.err.println("[VaiTroDAO] Lỗi lấy chức năng của vai trò: " + e.getMessage());
        }
        return list;
    }

    public List<ChucNangDTO> layTatCaChucNang() {
        List<ChucNangDTO> list = new ArrayList<>();
        String sql = "SELECT MaChucNang, TenChucNang, MoTa FROM CHUCNANG ORDER BY MaChucNang";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ChucNangDTO cn = new ChucNangDTO();
                cn.setMaChucNang(rs.getString("MaChucNang"));
                cn.setTenChucNang(rs.getString("TenChucNang"));
                cn.setMoTa(rs.getString("MoTa"));
                list.add(cn);
            }
        } catch (SQLException e) {
            System.err.println("[VaiTroDAO] Lỗi lấy danh sách chức năng: " + e.getMessage());
            // Fallback: dùng danh sách mặc định nếu bảng CHUCNANG chưa có dữ liệu
            list = getChucNangMacDinh();
        }
        return list;
    }

    public List<ChucNangDTO> getChucNangMacDinh() {
        List<ChucNangDTO> list = new ArrayList<>();
        String[][] data = {
                { "CN01", "Quản lý nhân viên", "Thêm, sửa, xóa thông tin nhân viên" },
                { "CN02", "Quản lý hội viên", "Xem và chỉnh sửa thông tin hội viên" },
                { "CN03", "Đặt chỗ không gian", "Tạo và duyệt phiếu đặt chỗ" },
                { "CN04", "Quản lý dịch vụ F&B", "Thêm, sửa, xóa dịch vụ ăn uống" },
                { "CN05", "Thanh toán hóa đơn", "Thực hiện thu ngân, xuất hóa đơn" },
                { "CN06", "Quản lý chi nhánh", "Thêm và chỉnh sửa thông tin chi nhánh" },
                { "CN07", "Quản lý phiếu giảm giá", "Tạo và duyệt chương trình khuyến mãi" },
                { "CN08", "Xem báo cáo doanh thu", "Xem dashboard và thống kê tài chính" },
                { "CN09", "Quản lý nhóm quyền", "Phân quyền và quản lý vai trò người dùng" },
                { "CN10", "Quản lý không gian", "Thêm, sửa phòng và khu vực" }
        };
        for (String[] row : data) {
            ChucNangDTO cn = new ChucNangDTO();
            cn.setMaChucNang(row[0]);
            cn.setTenChucNang(row[1]);
            cn.setMoTa(row[2]);
            list.add(cn);
        }
        return list;
    }

    public void khoiTaoDuLieuChucNang() {
        String sqlCheck = "SELECT COUNT(*) FROM CHUCNANG";
        try (PreparedStatement ps = getConn().prepareStatement(sqlCheck);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next() && rs.getInt(1) > 0)
                return; // Đã có dữ liệu
        } catch (SQLException e) {
            System.err.println("[VaiTroDAO] Lỗi kiểm tra bảng CHUCNANG: " + e.getMessage());
            return;
        }

        String sqlInsert = "INSERT INTO CHUCNANG (MaChucNang, TenChucNang, MoTa) VALUES (?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sqlInsert)) {
            for (ChucNangDTO cn : getChucNangMacDinh()) {
                ps.setString(1, cn.getMaChucNang());
                ps.setString(2, cn.getTenChucNang());
                ps.setString(3, cn.getMoTa());
                ps.addBatch();
            }
            ps.executeBatch();
            System.out.println("[VaiTroDAO] Khởi tạo dữ liệu bảng CHUCNANG thành công.");
        } catch (SQLException e) {
            System.err.println("[VaiTroDAO] Lỗi khởi tạo dữ liệu CHUCNANG: " + e.getMessage());
        }
    }

    private void damBaoNhomChucNangProxy(Connection conn, String maVaiTro) throws SQLException {
        String sqlCheck = "SELECT COUNT(*) FROM NHOMCHUCNANG WHERE MaNhomChucNang = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlCheck)) {
            ps.setString(1, maVaiTro);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    // Đã có → cũng đảm bảo có liên kết CHITIETNHOMCHUCNANG
                    ensureLienKetVaiTroNhom(conn, maVaiTro);
                    return;
                }
            }
        }

        String tenVaiTro = maVaiTro;
        String moTa = "";
        String sqlVT = "SELECT TenVaiTro, MoTa FROM VAITRO WHERE MaVaiTro = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlVT)) {
            ps.setString(1, maVaiTro);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tenVaiTro = rs.getString("TenVaiTro");
                    moTa = rs.getString("MoTa") != null ? rs.getString("MoTa") : "";
                }
            }
        }

        String sqlNCN = "INSERT INTO NHOMCHUCNANG (MaNhomChucNang, TenNhomChucNang, MoTa) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sqlNCN)) {
            ps.setString(1, maVaiTro);
            ps.setString(2, tenVaiTro);
            ps.setString(3, moTa);
            ps.executeUpdate();
        }

        ensureLienKetVaiTroNhom(conn, maVaiTro);
    }

    private void ensureLienKetVaiTroNhom(Connection conn, String maVaiTro) throws SQLException {
        String sqlCheck = "SELECT COUNT(*) FROM CHITIETNHOMCHUCNANG WHERE MaVaiTro = ? AND MaNhomChucNang = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlCheck)) {
            ps.setString(1, maVaiTro);
            ps.setString(2, maVaiTro);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0)
                    return; // Đã có
            }
        }
        String sqlIns = "INSERT INTO CHITIETNHOMCHUCNANG (MaVaiTro, MaNhomChucNang) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sqlIns)) {
            ps.setString(1, maVaiTro);
            ps.setString(2, maVaiTro);
            ps.executeUpdate();
        }
    }

    private void capNhatChucNangInternal(Connection conn, String maVaiTro,
            List<String> danhSachMaChucNang) throws SQLException {
        String sqlDel = "DELETE FROM CHITIETCHUCNANG WHERE MaNhomChucNang = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlDel)) {
            ps.setString(1, maVaiTro);
            ps.executeUpdate();
        }

        if (danhSachMaChucNang != null && !danhSachMaChucNang.isEmpty()) {
            String sqlCN = "INSERT INTO CHITIETCHUCNANG (MaNhomChucNang, MaChucNang) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlCN)) {
                for (String maCN : danhSachMaChucNang) {
                    ps.setString(1, maVaiTro);
                    ps.setString(2, maCN);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }
    }

    private String taoMaVaiTroMoi(Connection conn) throws SQLException {
        String sql = "SELECT MAX(MaVaiTro) AS MaxMa FROM VAITRO WHERE MaVaiTro LIKE 'VT%'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String maxMa = rs.getString("MaxMa");
                if (maxMa != null) {
                    try {
                        int soThuTu = Integer.parseInt(maxMa.substring(2)) + 1;
                        return String.format("VT%02d", soThuTu);
                    } catch (NumberFormatException e) {
                        /* ignore */ }
                }
            }
        }
        return "VT01";
    }
}
