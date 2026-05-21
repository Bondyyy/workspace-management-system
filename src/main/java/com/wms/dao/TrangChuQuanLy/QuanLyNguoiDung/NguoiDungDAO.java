package com.wms.dao.TrangChuQuanLy.QuanLyNguoiDung;

import com.wms.config.DatabaseConnection;
import com.wms.model.TrangChuQuanLy.QuanLyNguoiDung.NguoiDungDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NguoiDungDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public NguoiDungDTO timTheoTenTaiKhoan(String identifier) throws SQLException {
        String sql = """
                    SELECT n.MaND, n.HoTen, n.TenTaiKhoan, n.MatKhauMaHoa, n.AnhDaiDien,
                           n.GioiTinh, n.Email, n.SDT, n.NgaySinh,
                           n.ThoiGianTao, n.CapNhatLanCuoi, n.LanCuoiDangNhap, n.TrangThaiND,
                           v.TenVaiTro, v.MaVaiTro, nv_table.MaNV
                    FROM NGUOIDUNG n
                    LEFT JOIN CHITIETVAITRO cvt ON n.MaND = cvt.MaND
                    LEFT JOIN VAITRO v ON cvt.MaVaiTro = v.MaVaiTro
                    LEFT JOIN NHANVIEN nv_table ON n.MaND = nv_table.MaND
                    WHERE n.TenTaiKhoan = ? OR n.Email = ? OR n.SDT = ?
                """;

        NguoiDungDTO user = null;
        List<String> vaiTros = new ArrayList<>();

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.setString(2, identifier);
            ps.setString(3, identifier);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                if (user == null) {
                    user = new NguoiDungDTO();
                    user.setMaND(rs.getString("MaND"));
                    user.setTenTaiKhoan(rs.getString("TenTaiKhoan"));
                    user.setMatKhauMaHoa(rs.getString("MatKhauMaHoa"));
                    user.setAnhDaiDien(rs.getBytes("AnhDaiDien"));
                    user.setGioiTinh(rs.getString("GioiTinh"));
                    user.setEmail(rs.getString("Email"));
                    user.setSdt(rs.getString("SDT"));
                    user.setNgaySinh(rs.getDate("NgaySinh"));
                    user.setThoiGianTao(rs.getTimestamp("ThoiGianTao"));
                    user.setCapNhatLanCuoi(rs.getTimestamp("CapNhatLanCuoi"));
                    user.setLanCuoiDangNhap(rs.getTimestamp("LanCuoiDangNhap"));
                    user.setTrangThaiND(rs.getString("TrangThaiND"));
                    String hoTen = rs.getString("HoTen");
                    if (hoTen == null || hoTen.trim().isEmpty()) {
                        hoTen = rs.getString("TenTaiKhoan");
                    }
                    if (hoTen == null || hoTen.trim().isEmpty()) {
                        hoTen = "Admin"; // Fallback cuối cùng
                    }
                    user.setHoTen(hoTen);
                    user.setMaNV(rs.getString("MaNV"));
                }

                String tenVaiTro = rs.getString("TenVaiTro");
                if (tenVaiTro != null) {
                    vaiTros.add(tenVaiTro);
                }
                String maVaiTro = rs.getString("MaVaiTro");
                if (maVaiTro != null) {
                    vaiTros.add(maVaiTro);
                }
            }
        }

        if (user != null) {
            user.setVaiTro(vaiTros);
            // Load danh sách chức năng của người dùng
            user.setChucNang(layDanhSachChucNangCuaNguoiDung(user.getMaND()));
        }
        return user;
    }

    public boolean kiemTraTaiKhoanTonTai(String tenTaiKhoan) throws SQLException {
        Connection conn = getConn();
        if (conn == null) {
            throw new SQLException("Không thể kết nối đến Cơ sở dữ liệu!");
        }
        String sql = "SELECT 1 FROM NGUOIDUNG WHERE TenTaiKhoan = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenTaiKhoan);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean kiemTraEmailTonTai(String email) throws SQLException {
        Connection conn = getConn();
        if (conn == null) {
            throw new SQLException("Không thể kết nối đến Cơ sở dữ liệu!");
        }
        String sql = "SELECT 1 FROM NGUOIDUNG WHERE Email = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean kiemTraSdtTonTai(String sdt) throws SQLException {
        Connection conn = getConn();
        if (conn == null) {
            throw new SQLException("Không thể kết nối đến Cơ sở dữ liệu!");
        }
        String sql = "SELECT 1 FROM NGUOIDUNG WHERE SDT = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sdt);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void themNguoiDung(NguoiDungDTO user, String hoTen) throws SQLException {
        String maND = user.getMaND();
        if (maND == null || maND.isEmpty()) {
            maND = generateNextMaND();
            user.setMaND(maND);
        }

        String sqlND = "INSERT INTO NGUOIDUNG (MaND, HoTen, TenTaiKhoan, MatKhauMaHoa, Email, TrangThaiND, ThoiGianTao, CapNhatLanCuoi) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

        // Sử dụng mã HV sequential cho hội viên mới
        String sqlKH = "INSERT INTO KHACHHANG (MaKH, MaHangThanhVien, TongChiTieu, CapNhatLanCuoi, MaND, LoaiKH) " +
                "VALUES (?, 'HTV01', 0, CURRENT_TIMESTAMP, ?, 'Hội viên')";

        Connection conn = getConn();
        if (conn == null) {
            throw new SQLException("Không thể kết nối đến Cơ sở dữ liệu!");
        }

        boolean autoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sqlND)) {
                ps.setString(1, maND);
                ps.setString(2, hoTen);
                ps.setString(3, user.getTenTaiKhoan());
                ps.setString(4, user.getMatKhauMaHoa());
                ps.setString(5, user.getEmail());
                ps.setString(6, "Đang hoạt động");
                ps.executeUpdate();
            }

            try (PreparedStatement psKH = conn.prepareStatement(sqlKH)) {
                String maKH = "HV000001";
                // Tìm mã HV lớn nhất để tăng dần
                String sqlMaxKH = "SELECT MAX(MaKH) FROM KHACHHANG WHERE MaKH LIKE 'HV%'";
                try (PreparedStatement psMax = conn.prepareStatement(sqlMaxKH);
                        ResultSet rsMax = psMax.executeQuery()) {
                    if (rsMax.next() && rsMax.getString(1) != null) {
                        String max = rsMax.getString(1);
                        try {
                            int num = Integer.parseInt(max.substring(2)) + 1;
                            maKH = String.format("HV%06d", num);
                        } catch (Exception e) {
                            maKH = "HV000001";
                        }
                    }
                }

                psKH.setString(1, maKH);
                psKH.setString(2, maND);
                psKH.executeUpdate();
            }

            // Đảm bảo vai trò Khách hàng (VT00) tồn tại trong hệ thống
            String sqlCheckVT = "SELECT COUNT(*) FROM VAITRO WHERE MaVaiTro = ?";
            try (PreparedStatement psCheck = conn.prepareStatement(sqlCheckVT)) {
                psCheck.setString(1, com.wms.config.AppConstants.ROLE_CUSTOMER_CODE);
                ResultSet rsCheck = psCheck.executeQuery();
                if (rsCheck.next() && rsCheck.getInt(1) == 0) {
                    String sqlInsVT = "INSERT INTO VAITRO (MaVaiTro, TenVaiTro, MoTa) VALUES (?, ?, ?)";
                    try (PreparedStatement psIns = conn.prepareStatement(sqlInsVT)) {
                        psIns.setString(1, com.wms.config.AppConstants.ROLE_CUSTOMER_CODE);
                        psIns.setString(2, com.wms.config.AppConstants.ROLE_CUSTOMER_NAME);
                        psIns.setString(3, "Quyền hạn mặc định cho người đăng ký mới");
                        psIns.executeUpdate();
                    }
                }
            }

            String selectedRole = com.wms.config.AppConstants.ROLE_CUSTOMER_CODE; // Mặc định là Khách hàng
            if (user.getVaiTro() != null && !user.getVaiTro().isEmpty()) {
                selectedRole = user.getVaiTro().get(0);
            }

            String sqlVaiTro = "INSERT INTO CHITIETVAITRO (MaND, MaVaiTro) VALUES (?, ?)";
            try (PreparedStatement psVT = conn.prepareStatement(sqlVaiTro)) {
                psVT.setString(1, maND);
                psVT.setString(2, selectedRole);
                psVT.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(autoCommit);
        }
    }

    public void capNhatLanDangNhapCuoi(String maND) throws SQLException {
        String sql = "UPDATE NGUOIDUNG SET LanCuoiDangNhap = CURRENT_TIMESTAMP WHERE MaND = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maND);
            ps.executeUpdate();
        }
    }

    public void capNhatMatKhauTheoEmail(String email, String matKhauMaHoa) throws SQLException {
        String sql = "UPDATE NGUOIDUNG SET MatKhauMaHoa = ?, CapNhatLanCuoi = CURRENT_TIMESTAMP WHERE Email = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, matKhauMaHoa);
            ps.setString(2, email);
            ps.executeUpdate();
        }
    }

    public List<NguoiDungDTO> getAllNguoiDung() throws SQLException {
        String sql = "SELECT n.MaND, n.HoTen, n.TenTaiKhoan, n.MatKhauMaHoa, n.GioiTinh, n.Email, n.SDT, n.NgaySinh, n.TrangThaiND, n.AnhDaiDien, cvt.MaVaiTro, v.TenVaiTro " +
                     "FROM NGUOIDUNG n " +
                     "LEFT JOIN CHITIETVAITRO cvt ON n.MaND = cvt.MaND " +
                     "LEFT JOIN VAITRO v ON cvt.MaVaiTro = v.MaVaiTro " +
                     "ORDER BY n.ThoiGianTao DESC";
        List<NguoiDungDTO> list = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToDTO(rs));
            }
        }
        return list;
    }

    public List<NguoiDungDTO> searchNguoiDung(String keyword) throws SQLException {
        String sql = "SELECT n.MaND, n.HoTen, n.TenTaiKhoan, n.MatKhauMaHoa, n.GioiTinh, n.Email, n.SDT, n.NgaySinh, n.TrangThaiND, n.AnhDaiDien, cvt.MaVaiTro, v.TenVaiTro " +
                     "FROM NGUOIDUNG n " +
                     "LEFT JOIN CHITIETVAITRO cvt ON n.MaND = cvt.MaND " +
                     "LEFT JOIN VAITRO v ON cvt.MaVaiTro = v.MaVaiTro " +
                     "WHERE LOWER(n.HoTen) LIKE ? OR LOWER(n.TenTaiKhoan) LIKE ? OR n.SDT LIKE ? OR LOWER(n.Email) LIKE ? " +
                     "ORDER BY n.ThoiGianTao DESC";
        List<NguoiDungDTO> list = new ArrayList<>();
        String searchKey = "%" + keyword.toLowerCase() + "%";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, searchKey);
            ps.setString(2, searchKey);
            ps.setString(3, searchKey);
            ps.setString(4, searchKey);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToDTO(rs));
                }
            }
        }
        return list;
    }

    public void updateNguoiDung(NguoiDungDTO user) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE NGUOIDUNG SET HoTen = ?, TenTaiKhoan = ?, GioiTinh = ?, Email = ?, SDT = ?, NgaySinh = ?, TrangThaiND = ?, AnhDaiDien = ?, CapNhatLanCuoi = CURRENT_TIMESTAMP ");
        boolean updatePassword = user.getMatKhauMaHoa() != null && !user.getMatKhauMaHoa().isEmpty();
        
        if (updatePassword) {
            sql.append(", MatKhauMaHoa = ? ");
        }
        sql.append("WHERE MaND = ?");

        Connection conn = getConn();
        if (conn == null) {
            throw new SQLException("Không thể kết nối đến Cơ sở dữ liệu!");
        }

        boolean autoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int idx = 1;
                ps.setString(idx++, user.getHoTen());
                ps.setString(idx++, user.getTenTaiKhoan());
                ps.setString(idx++, user.getGioiTinh());
                ps.setString(idx++, user.getEmail());
                ps.setString(idx++, user.getSdt());
                ps.setDate(idx++, user.getNgaySinh());
                ps.setString(idx++, user.getTrangThaiND());
                ps.setBytes(idx++, user.getAnhDaiDien());
                if (updatePassword) {
                    ps.setString(idx++, user.getMatKhauMaHoa());
                }
                ps.setString(idx++, user.getMaND());
                ps.executeUpdate();
            }

            // Xoá vai trò cũ
            try (PreparedStatement psDel = conn.prepareStatement("DELETE FROM CHITIETVAITRO WHERE MaND = ?")) {
                psDel.setString(1, user.getMaND());
                psDel.executeUpdate();
            }

            // Thêm vai trò mới
            String selectedRole = com.wms.config.AppConstants.ROLE_CUSTOMER_CODE;
            if (user.getVaiTro() != null && !user.getVaiTro().isEmpty()) {
                selectedRole = user.getVaiTro().get(0);
            }

            String sqlVT = "INSERT INTO CHITIETVAITRO (MaND, MaVaiTro) VALUES (?, ?)";
            try (PreparedStatement psVT = conn.prepareStatement(sqlVT)) {
                psVT.setString(1, user.getMaND());
                psVT.setString(2, selectedRole);
                psVT.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(autoCommit);
        }
    }

    private NguoiDungDTO mapResultSetToDTO(ResultSet rs) throws SQLException {
        NguoiDungDTO user = new NguoiDungDTO();
        user.setMaND(rs.getString("MaND"));
        user.setHoTen(rs.getString("HoTen"));
        user.setTenTaiKhoan(rs.getString("TenTaiKhoan"));
        user.setMatKhauMaHoa(rs.getString("MatKhauMaHoa"));
        user.setGioiTinh(rs.getString("GioiTinh"));
        user.setEmail(rs.getString("Email"));
        user.setSdt(rs.getString("SDT"));
        user.setNgaySinh(rs.getDate("NgaySinh"));
        user.setTrangThaiND(rs.getString("TrangThaiND"));
        user.setAnhDaiDien(rs.getBytes("AnhDaiDien"));
        
        String maVaiTro = rs.getString("MaVaiTro");
        String tenVaiTro = rs.getString("TenVaiTro");
        if (maVaiTro != null) {
            java.util.List<String> vtList = new java.util.ArrayList<>();
            vtList.add(maVaiTro);
            if (tenVaiTro != null) {
                vtList.add(tenVaiTro);
            }
            user.setVaiTro(vtList);
        }
        return user;
    }

    public String generateNextMaND() throws SQLException {
        String sql = "SELECT MAX(TO_NUMBER(SUBSTR(MaND, 3))) FROM NGUOIDUNG WHERE REGEXP_LIKE(MaND, '^ND[0-9]+$')";
        try (PreparedStatement ps = getConn().prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int maxNum = rs.getInt(1);
                return String.format("ND%03d", maxNum + 1);
            }
        }
        return "ND001";
    }

    public java.util.List<String> layDanhSachChucNangCuaNguoiDung(String maND) {
        java.util.List<String> list = new java.util.ArrayList<>();
        String sql = "SELECT DISTINCT ctcn.MaChucNang " +
                "FROM CHITIETVAITRO cvt " +
                "JOIN CHITIETNHOMCHUCNANG ctncn ON cvt.MaVaiTro = ctncn.MaVaiTro " +
                "JOIN CHITIETCHUCNANG ctcn ON ctncn.MaNhomChucNang = ctcn.MaNhomChucNang " +
                "WHERE cvt.MaND = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maND);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getString("MaChucNang"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[NguoiDungDAO] Lỗi lấy chức năng người dùng: " + e.getMessage());
        }
        return list;
    }
}