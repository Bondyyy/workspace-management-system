package com.wms.dao.TrangChuQuanLy.QuanLyNguoiDung;

import com.wms.config.DatabaseConnection;
import com.wms.model.TrangChuQuanLy.QuanLyNguoiDung.NguoiDungDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NguoiDungDAO {

    public NguoiDungDTO timTheoTenTaiKhoan(String identifier) throws SQLException {
        String sql = """
                    SELECT n.MaND, n.HoTen, n.TenTaiKhoan, n.MatKhauMaHoa, n.AnhDaiDien,
                           n.GioiTinh, n.Email, n.SDT, n.NgaySinh,
                           n.ThoiGianTao, n.CapNhatLanCuoi, n.LanCuoiDangNhap, n.TrangThaiND,
                           v.TenVaiTro, v.MaVaiTro, nv_table.MaNV, nv_table.MaCN, cn.TenCN
                    FROM NGUOIDUNG n
                    LEFT JOIN CHITIETVAITRO cvt ON n.MaND = cvt.MaND
                    LEFT JOIN VAITRO v ON cvt.MaVaiTro = v.MaVaiTro
                    LEFT JOIN NHANVIEN nv_table ON n.MaND = nv_table.MaND
                    LEFT JOIN CHINHANH cn ON cn.MaCN = nv_table.MaCN
                    WHERE n.TenTaiKhoan = ? OR n.Email = ? OR n.SDT = ?
                """;

        NguoiDungDTO user = null;
        List<String> vaiTros = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.setString(2, identifier);
            ps.setString(3, identifier);
            try (ResultSet rs = ps.executeQuery()) {
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
                            hoTen = "Admin";
                        }
                        user.setHoTen(hoTen);
                        user.setMaNV(rs.getString("MaNV"));
                        user.setMaCN(rs.getString("MaCN"));
                        user.setTenCN(rs.getString("TenCN"));
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
        }

        if (user != null) {
            user.setVaiTro(vaiTros);
            user.setChucNang(layDanhSachChucNangCuaNguoiDung(user.getMaND()));
        }
        return user;
    }

    public boolean kiemTraTaiKhoanTonTai(String tenTaiKhoan) throws SQLException {
        String sql = "SELECT 1 FROM NGUOIDUNG WHERE TenTaiKhoan = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenTaiKhoan);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean kiemTraEmailTonTai(String email) throws SQLException {
        String sql = "SELECT 1 FROM NGUOIDUNG WHERE Email = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean kiemTraSdtTonTai(String sdt) throws SQLException {
        String sql = "SELECT 1 FROM NGUOIDUNG WHERE SDT = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sdt);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void themNguoiDung(NguoiDungDTO user, String hoTen) throws SQLException {
        String sqlND = """
                BEGIN
                    INSERT INTO NGUOIDUNG (
                        HoTen, TenTaiKhoan, MatKhauMaHoa, Email, SDT, GioiTinh, NgaySinh, AnhDaiDien,
                        TrangThaiND, ThoiGianTao, CapNhatLanCuoi
                    ) VALUES (
                        ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
                    )
                    RETURNING MaND INTO ?;
                END;
                """;

        String sqlKH = """
                BEGIN
                    INSERT INTO KHACHHANG (
                        MaHangThanhVien, TongChiTieu, CapNhatLanCuoi, MaND, LoaiKH
                    ) VALUES (
                        'HTV01', 0, CURRENT_TIMESTAMP, ?, 'Hội viên'
                    )
                    RETURNING MaKH INTO ?;
                END;
                """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            boolean autoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER SESSION DISABLE PARALLEL DML");
                } catch (Exception e) {
                    System.err.println("Cannot disable parallel DML: " + e.getMessage());
                }

                String maND;
                try (CallableStatement cs = conn.prepareCall(sqlND)) {
                    cs.setString(1, hoTen);
                    cs.setString(2, user.getTenTaiKhoan());
                    cs.setString(3, user.getMatKhauMaHoa());
                    cs.setString(4, user.getEmail());
                    cs.setString(5, user.getSdt());
                    cs.setString(6, user.getGioiTinh());
                    cs.setDate(7, user.getNgaySinh());
                    cs.setBytes(8, user.getAnhDaiDien());
                    cs.setString(9, user.getTrangThaiND() != null ? user.getTrangThaiND() : "Đang hoạt động");
                    cs.registerOutParameter(10, Types.VARCHAR);
                    cs.execute();
                    maND = cs.getString(10);
                    user.setMaND(maND);
                }

                try (CallableStatement csKH = conn.prepareCall(sqlKH)) {
                    csKH.setString(1, maND);
                    csKH.registerOutParameter(2, Types.VARCHAR);
                    csKH.execute();
                }

                String sqlCheckVT = "SELECT COUNT(*) FROM VAITRO WHERE MaVaiTro = ?";
                try (PreparedStatement psCheck = conn.prepareStatement(sqlCheckVT)) {
                    psCheck.setString(1, com.wms.config.AppConstants.ROLE_CUSTOMER_CODE);
                    try (ResultSet rsCheck = psCheck.executeQuery()) {
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
                }

                String selectedRole = com.wms.config.AppConstants.ROLE_CUSTOMER_CODE;
                if (user.getVaiTro() != null && !user.getVaiTro().isEmpty()) {
                    selectedRole = user.getVaiTro().get(0);
                }

                String sqlVaiTro = "INSERT INTO CHITIETVAITRO (MaND, MaVaiTro) VALUES (?, ?)";
                try (PreparedStatement psVT = conn.prepareStatement(sqlVaiTro)) {
                    psVT.setString(1, maND);
                    psVT.setString(2, selectedRole);
                    psVT.executeUpdate();
                }

                dongBoHoSoNhanVienTheoVaiTro(maND, conn);
                System.out.println("[NguoiDungDAO] Tao nguoi dung MaND=" + maND
                        + ", vaiTro=" + selectedRole + " va dong bo ho so nhan vien neu can.");
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autoCommit);
            }
        }
    }

    public void capNhatLanDangNhapCuoi(String maND) throws SQLException {
        String sql = "UPDATE NGUOIDUNG SET LanCuoiDangNhap = CURRENT_TIMESTAMP WHERE MaND = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maND);
            ps.executeUpdate();
        }
    }

    public void capNhatMatKhauTheoEmail(String email, String matKhauMaHoa) throws SQLException {
        String sql = "UPDATE NGUOIDUNG SET MatKhauMaHoa = ?, CapNhatLanCuoi = CURRENT_TIMESTAMP WHERE Email = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
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
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            boolean autoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER SESSION DISABLE PARALLEL DML");
                } catch (Exception e) {
                    System.err.println("Cannot disable parallel DML: " + e.getMessage());
                }

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

                dongBoHoSoNhanVienTheoVaiTro(user.getMaND(), conn);
                System.out.println("[NguoiDungDAO] Cap nhat nguoi dung MaND=" + user.getMaND()
                        + ", vaiTro=" + selectedRole + " va dong bo ho so nhan vien.");
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autoCommit);
            }
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
        return "";
    }

    public java.util.List<String> layDanhSachChucNangCuaNguoiDung(String maND) {
        java.util.List<String> list = new java.util.ArrayList<>();
        String sql = "SELECT DISTINCT ctcn.MaChucNang " +
                "FROM CHITIETVAITRO cvt " +
                "JOIN CHITIETNHOMCHUCNANG ctncn ON cvt.MaVaiTro = ctncn.MaVaiTro " +
                "JOIN CHITIETCHUCNANG ctcn ON ctncn.MaNhomChucNang = ctcn.MaNhomChucNang " +
                "WHERE cvt.MaND = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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

    private void dongBoHoSoNhanVienTheoVaiTro(String maND, Connection conn) throws SQLException {
        String sqlRoles = "SELECT vt.MaVaiTro, vt.TenVaiTro " +
                          "FROM CHITIETVAITRO ctv " +
                          "JOIN VAITRO vt ON ctv.MaVaiTro = vt.MaVaiTro " +
                          "WHERE ctv.MaND = ?";
        boolean coVaiTroNoiBo = false;
        String loaiNhanVien = null;

        try (PreparedStatement ps = conn.prepareStatement(sqlRoles)) {
            ps.setString(1, maND);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String maVaiTro = rs.getString("MaVaiTro");
                    String tenVaiTro = rs.getString("TenVaiTro");
                    if (!laVaiTroHoiVien(maVaiTro, tenVaiTro)) {
                        coVaiTroNoiBo = true;
                        loaiNhanVien = chonLoaiNhanVienUuTien(loaiNhanVien, mapLoaiNhanVien(maVaiTro, tenVaiTro));
                    }
                }
            }
        }

        String sqlCheckNV = "SELECT MaNV FROM NHANVIEN WHERE MaND = ?";
        String maNV = null;
        try (PreparedStatement ps = conn.prepareStatement(sqlCheckNV)) {
            ps.setString(1, maND);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    maNV = rs.getString("MaNV");
                }
            }
        }

        if (!coVaiTroNoiBo) {
            if (maNV != null) {
                String sqlUpdate = "UPDATE NHANVIEN SET TrangThaiLamViec = 'Ngừng làm việc' WHERE MaNV = ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                    ps.setString(1, maNV);
                    ps.executeUpdate();
                }
                System.out.println("[NguoiDungDAO] Dong bo nhan vien MaND=" + maND
                        + ": chuyen TrangThaiLamViec=Ngung lam viec.");
            }
        } else {
            String loaiNV = loaiNhanVien == null ? "Nhân viên" : loaiNhanVien;
            if (maNV == null) {
                String sqlInsert = "INSERT INTO NHANVIEN (LoaiNV, NgayVaoLam, TrangThaiLamViec, MaND) VALUES (?, CURRENT_DATE, 'Đang làm việc', ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
                    ps.setString(1, loaiNV);
                    ps.setString(2, maND);
                    ps.executeUpdate();
                }
                System.out.println("[NguoiDungDAO] Dong bo nhan vien MaND=" + maND
                        + ": tao moi NHANVIEN LoaiNV=" + loaiNV + ".");
            } else {
                String sqlUpdate = "UPDATE NHANVIEN SET LoaiNV = ?, TrangThaiLamViec = 'Đang làm việc' WHERE MaNV = ?";
                try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                    ps.setString(1, loaiNV);
                    ps.setString(2, maNV);
                    ps.executeUpdate();
                }
                System.out.println("[NguoiDungDAO] Dong bo nhan vien MaND=" + maND
                        + ": kich hoat lai MaNV=" + maNV + ", LoaiNV=" + loaiNV + ".");
            }
        }
    }

    private boolean laVaiTroHoiVien(String maVaiTro, String tenVaiTro) {
        if (maVaiTro != null && maVaiTro.equalsIgnoreCase(com.wms.config.AppConstants.ROLE_CUSTOMER_CODE)) {
            return true;
        }
        return tenVaiTro != null && tenVaiTro.toLowerCase().contains("hội viên");
    }

    private String mapLoaiNhanVien(String maVaiTro, String tenVaiTro) {
        String roleName = tenVaiTro == null ? "" : tenVaiTro.toLowerCase();
        if (maVaiTro != null && maVaiTro.equalsIgnoreCase(com.wms.config.AppConstants.ROLE_ADMIN_CODE)
                || roleName.contains("quản trị")) {
            return "Quản trị viên Hệ thống";
        }
        if (maVaiTro != null && maVaiTro.equalsIgnoreCase(com.wms.config.AppConstants.ROLE_MANAGER_CODE)
                || roleName.contains("quản lý")) {
            return "Quản lý";
        }
        return "Nhân viên";
    }

    private String chonLoaiNhanVienUuTien(String current, String candidate) {
        if ("Quản trị viên Hệ thống".equals(current) || "Quản trị viên Hệ thống".equals(candidate)) {
            return "Quản trị viên Hệ thống";
        }
        if ("Quản lý".equals(current) || "Quản lý".equals(candidate)) {
            return "Quản lý";
        }
        return candidate == null ? current : candidate;
    }
}
