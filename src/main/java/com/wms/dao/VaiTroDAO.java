package com.wms.dao;

import com.wms.config.DatabaseConnection;
import com.wms.model.VaiTroDTO;
import com.wms.model.ChucNangDTO;

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
            { "CN01", "Tổng quan", "Xem dashboard thống kê và báo cáo" },
            { "CN02", "Chi nhánh", "Quản lý thông tin và trạng thái các chi nhánh" },
            { "CN03", "Không gian", "Quản lý sơ đồ, vị trí và trạng thái chỗ ngồi/phòng" },
            { "CN04", "Thông tin Dịch vụ", "Quản lý danh mục dịch vụ F&B và tiện ích" },
            { "CN05", "Kho Dịch vụ", "Quản lý nhập xuất tồn kho hàng hóa dịch vụ" },
            { "CN06", "Dịch vụ Khách đặt", "Quản lý các yêu cầu dịch vụ từ khách hàng" },
            { "CN07", "Phiên làm việc", "Theo dõi và quản lý các ca trực, phiên sử dụng" },
            { "CN08", "Hóa đơn & Thu ngân", "Xử lý thanh toán, xuất hóa đơn và báo cáo doanh thu" },
            { "CN09", "Khuyến mãi (Voucher)", "Tạo và quản lý các chương trình ưu đãi, mã giảm giá" },
            { "CN10", "Hội viên / Khách hàng", "Quản lý thông tin, hạng thành viên và lịch sử tích điểm" },
            { "CN11", "Nhân sự / Phân quyền", "Quản lý tài khoản nhân viên và thiết lập quyền hạn" }
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
        Connection conn = getConn();
        if (conn == null) return;
        try {
            // Cập nhật lại các chức năng mới (Merge)
            String mergeSql = "MERGE INTO CHUCNANG dest USING (SELECT ? AS MaChucNang, ? AS TenChucNang, ? AS MoTa FROM DUAL) src ON (dest.MaChucNang = src.MaChucNang) WHEN MATCHED THEN UPDATE SET dest.TenChucNang = src.TenChucNang, dest.MoTa = src.MoTa WHEN NOT MATCHED THEN INSERT (MaChucNang, TenChucNang, MoTa) VALUES (src.MaChucNang, src.TenChucNang, src.MoTa)";
            try (PreparedStatement ps = conn.prepareStatement(mergeSql)) {
                for (ChucNangDTO cn : getChucNangMacDinh()) {
                    ps.setString(1, cn.getMaChucNang());
                    ps.setString(2, cn.getTenChucNang());
                    ps.setString(3, cn.getMoTa());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            
            // Xóa các chức năng dư thừa cũ không còn tồn tại
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("DELETE FROM CHITIETCHUCNANG WHERE MaChucNang NOT IN ('CN01','CN02','CN03','CN04','CN05','CN06','CN07','CN08','CN09','CN10','CN11')");
                st.executeUpdate("DELETE FROM CHUCNANG WHERE MaChucNang NOT IN ('CN01','CN02','CN03','CN04','CN05','CN06','CN07','CN08','CN09','CN10','CN11')");
            }
            System.out.println("[VaiTroDAO] Đồng bộ dữ liệu bảng CHUCNANG thành công.");
        } catch (SQLException e) {
            System.err.println("[VaiTroDAO] Lỗi đồng bộ dữ liệu CHUCNANG: " + e.getMessage());
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
